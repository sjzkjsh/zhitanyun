package com.example.Service.Opt;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.AnalysisEntity.AbnormalEnergyExportVO;
import com.example.Entity.AnalysisEntity.ThresholdRange;
import com.example.Entity.Buildings;
import com.example.Entity.Devices;
import com.example.Entity.energyReadings;
import com.example.Enum.DeviceStatusConstants;
import com.example.Mapper.BuildingsMapper;
import com.example.Mapper.EnergyReadingsMapper;
import com.example.Service.AnalysisService.ThresholdRangeService;
import com.example.Service.DevicesService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.example.Util.ThresholdCheckUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceAnomalyDetector {

    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;

    @Autowired
    private ThresholdRangeService thresholdRangeService;

    @Autowired
    private DevicesService devicesService;

    @Autowired
    private BuildingsMapper buildingsMapper;

    private static final int LATEST_RECORDS_COUNT = 7;
    private static final int CONSECUTIVE_OUT_OF_RANGE = 2;

    // ==================== 全指标列表（根据你的 energyReadings 实体定义）====================
    public static final List<String> ALL_METRICS = Arrays.asList(
            "power_consumption",      // 总用电量
            "ac_power_consumption",   // 空调用电
            "water_consumption",      // 用水量
            "ac_outlet_temp",         // 空调出水温度
            "ac_inlet_temp",          // 空调回水温度
            "env_temp",               // 环境温度
            "humidity",               // 湿度
            "occupancy_density",      // 人员密度
            "water_flow_rate"         // 水流量
    );

    // ==================== 返回结果对象 ====================

    @Data
    public static class AnomalyPoint {
        private Integer deviceId;
        private String deviceCode;
        private Integer buildingId;
        private String buildingCode;
        private String buildingName;
        private String metricName;
        private Double actualValue;
        private BigDecimal minValue;
        private BigDecimal maxValue;
        private String unit;
        private LocalDateTime monitoringTime;
        private String alertType; // BELOW_MIN or ABOVE_MAX
    }

    @Data
    public static class ScanResult {
        private int abnormalDeviceCount;
        private int anomalyPointCount;
        private List<String> checkedMetrics;  // 新增：实际检测的指标列表
        private List<AnomalyPoint> anomalyPoints = new ArrayList<>();
        private List<Map<String, Object>> statusChangeSuggestions = new ArrayList<>();

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("abnormalDeviceCount", abnormalDeviceCount);
            map.put("anomalyPointCount", anomalyPointCount);
            map.put("checkedMetrics", checkedMetrics);
            map.put("anomalyPoints", anomalyPoints);
            map.put("statusChangeSuggestions", statusChangeSuggestions);
            return map;
        }
    }

    // ==================== 对外提供的扫描接口 ====================

    /**
     * 扫描所有设备，检测所有指标（不传metrics时使用）
     */
    public ScanResult scanAllDevices() {
        return scanAllDevices(ALL_METRICS);
    }

    /**
     * 扫描所有设备，检测指定指标（只读，不写库）
     */
    public ScanResult scanAllDevices(List<String> metrics) {
        log.info("========== 设备异常扫描开始（只读模式） ==========");
        log.info("检测指标: {}", metrics);

        ScanResult result = new ScanResult();
        result.setCheckedMetrics(metrics);

        // 1. 获取最新读数
        List<energyReadings> latestReadings = energyReadingsMapper.selectLatestNByDevice(LATEST_RECORDS_COUNT);
        if (CollectionUtils.isEmpty(latestReadings)) {
            log.info("无任何设备的最新能耗数据");
            return result;
        }

        // 2. 获取生效阈值（只查询这些指标的阈值）
        LocalDateTime now = LocalDateTime.now();
        List<ThresholdRange> activeThresholds = thresholdRangeService.list(
                new LambdaQueryWrapper<ThresholdRange>()
                        .and(w -> w.isNull(ThresholdRange::getEffectiveFrom).or().le(ThresholdRange::getEffectiveFrom, now))
                        .and(w -> w.isNull(ThresholdRange::getEffectiveTo).or().ge(ThresholdRange::getEffectiveTo, now))
                        .in(ThresholdRange::getMetricName, metrics)
        );

        if (CollectionUtils.isEmpty(activeThresholds)) {
            log.warn("无生效中的阈值规则，请检查阈值配置");
            return result;
        }

        log.info("找到 {} 条生效阈值规则", activeThresholds.size());

        // 3. 构建阈值Map
        Map<String, List<ThresholdRange>> thresholdMap = activeThresholds.stream()
                .collect(Collectors.groupingBy(t ->
                        (t.getDeviceId() != null ? t.getDeviceId() : "null") + "_" +
                                (t.getBuildingId() != null ? t.getBuildingId() : "null")
                ));

        // 4. 设备分组
        Map<Integer, List<energyReadings>> deviceReadingsMap = latestReadings.stream()
                .collect(Collectors.groupingBy(energyReadings::getDeviceId));

        // 5. 获取设备信息
        Set<Integer> deviceIdSet = deviceReadingsMap.keySet();
        Map<Integer, Devices> deviceMap = devicesService.listByIds(deviceIdSet)
                .stream()
                .collect(Collectors.toMap(Devices::getDeviceId, d -> d));

        // 获取建筑信息
        Set<Integer> buildingIdSet = latestReadings.stream()
                .map(energyReadings::getBuildingId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Integer, Buildings> buildingMap = new HashMap<>();
        if (!buildingIdSet.isEmpty()) {
            buildingMap = buildingsMapper.selectBatchIds(buildingIdSet)
                    .stream()
                    .collect(Collectors.toMap(Buildings::getBuildingId, b -> b));
        }

        Set<Integer> abnormalDevices = new HashSet<>();

        // 6. 遍历检测设备
        for (Map.Entry<Integer, List<energyReadings>> entry : deviceReadingsMap.entrySet()) {
            Integer deviceId = entry.getKey();
            List<energyReadings> readings = entry.getValue();
            Devices device = deviceMap.get(deviceId);

            if (device == null || DeviceStatusConstants.STATUS_MAINTENANCE.equals(device.getDeviceStatus())) {
                continue;
            }

            Integer buildingId = readings.get(0).getBuildingId();
            Buildings building = buildingMap.get(buildingId);

            // 匹配阈值
            List<ThresholdRange> thresholds = ThresholdCheckUtil.findThresholds(thresholdMap, deviceId, buildingId);
            if (CollectionUtils.isEmpty(thresholds)) continue;

            // 排序并获取最新
            readings.sort((r1, r2) -> r2.getMonitoringTime().compareTo(r1.getMonitoringTime()));
            energyReadings latest = readings.get(0);

            boolean deviceHasAnomaly = false;

            for (ThresholdRange range : thresholds) {
                Double actualValue = ThresholdCheckUtil.getFieldValueByName(latest, range.getMetricName());
                if (actualValue == null) continue;

                boolean isAbnormal = ThresholdCheckUtil.checkMetricConsecutiveOutOfRange(readings, range, CONSECUTIVE_OUT_OF_RANGE);

                if (isAbnormal) {
                    deviceHasAnomaly = true;
                    abnormalDevices.add(deviceId);

                    AnomalyPoint point = new AnomalyPoint();
                    point.setDeviceId(deviceId);
                    point.setDeviceCode(device.getDeviceCode());
                    point.setBuildingId(buildingId);
                    point.setBuildingCode(building != null ? building.getBuildingCode() : "");
                    point.setBuildingName(building != null ? building.getBuildingName() : "");
                    point.setMetricName(range.getMetricName());
                    point.setActualValue(actualValue);
                    point.setMinValue(range.getMinValue());
                    point.setMaxValue(range.getMaxValue());
                    point.setUnit(range.getUnit());
                    point.setMonitoringTime(latest.getMonitoringTime());
                    point.setAlertType(ThresholdCheckUtil.determineAlertType(actualValue, range));

                    result.getAnomalyPoints().add(point);

                    log.warn("异常点: 设备{}-{} 指标{}={} 阈值[{}, {}]",
                            deviceId, device.getDeviceCode(), range.getMetricName(),
                            actualValue, range.getMinValue(), range.getMaxValue());
                }
            }

            // 记录建议状态变更（但不写入）
            String currentStatus = device.getDeviceStatus();
            String targetStatus = deviceHasAnomaly ? DeviceStatusConstants.STATUS_FAULT : DeviceStatusConstants.STATUS_NORMAL;
            if (!targetStatus.equals(currentStatus)) {
                Map<String, Object> suggestion = new HashMap<>();
                suggestion.put("deviceId", deviceId);
                suggestion.put("deviceCode", device.getDeviceCode());
                suggestion.put("currentStatus", currentStatus);
                suggestion.put("suggestedStatus", targetStatus);
                result.getStatusChangeSuggestions().add(suggestion);
            }
        }

        result.setAbnormalDeviceCount(abnormalDevices.size());
        result.setAnomalyPointCount(result.getAnomalyPoints().size());

        log.info("========== 扫描结束：{} 台设备异常，{} 个异常点 ==========",
                result.getAbnormalDeviceCount(), result.getAnomalyPointCount());

        return result;
    }

    /**
     * 扫描并返回导出数据格式（检测所有指标）
     */
    public List<AbnormalEnergyExportVO> scanForExport() {
        return scanForExport(ALL_METRICS);
    }

    /**
     * 扫描并返回导出数据格式（只读）
     */
    public List<AbnormalEnergyExportVO> scanForExport(List<String> metrics) {
        ScanResult scanResult = scanAllDevices(metrics);

        return scanResult.getAnomalyPoints().stream().map(point -> {
            AbnormalEnergyExportVO vo = new AbnormalEnergyExportVO();
            vo.setDeviceId(point.getDeviceId());
            vo.setDeviceCode(point.getDeviceCode());
            vo.setBuildingCode(point.getBuildingCode());
            vo.setBuildingName(point.getBuildingName());
            vo.setMetric(point.getMetricName());
            vo.setActualValue(point.getActualValue());
            vo.setMinThreshold(point.getMinValue() != null ? point.getMinValue().doubleValue() : null);
            vo.setMaxThreshold(point.getMaxValue() != null ? point.getMaxValue().doubleValue() : null);
            vo.setDeviationRate(calculateDeviationRate(point.getActualValue(), point.getMinValue(), point.getMaxValue()));
            vo.setMonitoringTime(point.getMonitoringTime());
            return vo;
        }).collect(Collectors.toList());
    }

    // ==================== 私有方法 ====================

    private String calculateDeviationRate(Double actual, BigDecimal min, BigDecimal max) {
        if (actual == null) return "N/A";

        double reference;
        if (max != null && actual > max.doubleValue()) {
            reference = max.doubleValue();
            double rate = (actual - reference) / reference * 100;
            return "+" + String.format("%.1f%%", rate);
        } else if (min != null && actual < min.doubleValue()) {
            reference = min.doubleValue();
            double rate = (reference - actual) / reference * 100;
            return "-" + String.format("%.1f%%", rate);
        }
        return "0%";
    }
}