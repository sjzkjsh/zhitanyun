package com.example.Service.AnalysisService;

import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Entity.AnalysisEntity.errorEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionService {

    private final ThresholdRangeService thresholdService;

    /**
     * 检测异常点（优化版：批量预加载阈值）
     */
    public List<errorEntity> detect(List<DeviceEnergyBuildingVO> dataList,
                                    Integer buildingId, Integer deviceId,
                                    List<String> metrics) {
        // 空数据快速返回
        if (dataList == null || dataList.isEmpty() || metrics == null || metrics.isEmpty()) {
            return List.of();
        }

        // 1. 参考时间校验
        LocalDateTime referenceTime = dataList.get(0).getMonitoringTime();
        if (referenceTime == null) {
            log.warn("监测时间为 null，无法加载阈值配置");
            return List.of();
        }

        // 2. 【核心改动】一次性批量查询所有阈值
        Map<String, ThresholdRangeService.Range> thresholdCache =
                thresholdService.getRangesBatch(buildingId, deviceId, metrics, referenceTime);

        if (thresholdCache.isEmpty()) {
            log.info("所有指标均无阈值配置，跳过异常检测");
            return List.of();
        }

        // 3. 批量检测
        List<errorEntity> result = new ArrayList<>();
        for (DeviceEnergyBuildingVO data : dataList) {
            for (Map.Entry<String, ThresholdRangeService.Range> entry : thresholdCache.entrySet()) {
                errorEntity anomaly = checkSingleMetric(data, entry.getKey(), entry.getValue(), deviceId);
                if (anomaly != null) {
                    result.add(anomaly);
                }
            }
        }

        log.info("异常检测完成: 总数据{}条, 异常{}个", dataList.size() * thresholdCache.size(), result.size());
        return result;
    }
    private errorEntity checkSingleMetric(DeviceEnergyBuildingVO data, String metric,
                                          ThresholdRangeService.Range range, Integer deviceId) {
        // 跳过未配置阈值的指标
        if (range == null) {
            return null;
        }

        // 提取值（处理NaN和null）
        double rawValue = extractValue(data, metric);
        if (Double.isNaN(rawValue)) {
            return null;
        }

        BigDecimal value = BigDecimal.valueOf(rawValue);

        // 检查是否在范围内
        if (range.contains(value)) {
            return null;
        }

        // 构造异常实体
        errorEntity point = new errorEntity();
        point.setDeviceId(deviceId);
        point.setTime(data.getMonitoringTime());
        point.setMetric(metric);
        point.setValue(value);
        point.setMin(range.getMin());
        point.setMax(range.getMax());
        point.setUnit(range.getUnit());
        point.setConfigId(range.getConfigId());  // 用于追溯使用了哪条配置

        // 判定方向并设置描述
        if (range.isAboveMax(value)) {
            point.setDescription(String.format("超出上限%.2f%%", (range.getDeviationRatio(value) - 1) * 100));
            point.setSeverity(calculateSeverity(range.getDeviationRatio(value)));
        } else {
            point.setDescription(String.format("低于下限%.2f%%", (1 - range.getDeviationRatio(value)) * 100));
            point.setSeverity(calculateSeverity(range.getDeviationRatio(value)));
        }

        return point;
    }

    /**
     * 计算严重程度
     */
    private String calculateSeverity(double deviationRatio) {
        if (deviationRatio > 2.0) return "CRITICAL";  // 超出一倍以上
        if (deviationRatio > 1.5) return "HIGH";      // 超出50%
        if (deviationRatio > 1.2) return "MEDIUM";    // 超出20%
        return "LOW";
    }

    /**
     * 提取指标值（返回NaN表示无效）
     */
    private double extractValue(DeviceEnergyBuildingVO data, String metric) {
        if (data == null || metric == null) {
            return Double.NaN;
        }

        Double value = switch (metric) {
            case "power_consumption" -> data.getPowerConsumption();
            case "water_consumption" -> data.getWaterConsumption();
            case "ac_power_consumption" -> data.getAcPowerConsumption();
            case "ac_outlet_temp" -> data.getAcOutletTemp();
            case "ac_inlet_temp" -> data.getAcInletTemp();
            case "env_temp" -> data.getEnvTemp();
            case "humidity" -> data.getHumidity();
            case "occupancy_density" -> data.getOccupancyDensity();
            default -> null;
        };

        return value != null ? value : Double.NaN;
    }
}