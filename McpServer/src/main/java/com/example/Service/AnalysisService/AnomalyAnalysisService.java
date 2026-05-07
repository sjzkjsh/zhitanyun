package com.example.Service.AnalysisService;

import com.example.Entity.AnalysisEntity.*;
import com.example.Entity.DeviceEnergyBuildingVO;
import com.example.Mapper.EnergyReadingsMapper;  // 新增
import com.example.Repository.AnomalyFeatureExtractor;
import com.example.Repository.SmartTimeSelector;
import com.example.Service.ServiceImpl.RagflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AnomalyAnalysisService {

    @Autowired
    private EnergyReadingsMapper energyReadingsMapper;

    @Autowired
    private AnomalyDetectionService detectionService;
    @Autowired
    private AnomalyFeatureExtractor featureExtractor;
    @Autowired
    private RagflowService ragflowService;
    @Autowired
    SmartTimeSelector smartTimeSelector;

    /**
     * 指定时间分析
     */
    public AnomalyReportData analyze(Integer buildingId, Integer deviceId,
                                     LocalDateTime start, LocalDateTime end,
                                     List<String> metrics) {
        // 1. 查询数据 - 改为按时间范围查询
        List<DeviceEnergyBuildingVO> dataList = energyReadingsMapper.selectByTimeRange(buildingId,deviceId, start, end);

        // 空数据处理
        if (dataList == null || dataList.isEmpty()) {
            AnomalyReportData empty = new AnomalyReportData();
            empty.setDeviceId(deviceId);
            empty.setStartTime(start);
            empty.setEndTime(end);
            empty.setAnomalyCount(0);
            empty.setTotalRecords(0);
            empty.setAnomalyFeatures("该时段无数据");
            empty.setAbnormalPoints(List.of());
            empty.setHourlyStats(Map.of());
            empty.setChartData(List.of());
            return empty;
        }

        // 2. 检测异常点
        List<errorEntity> abnormalPoints = detectionService.detect(dataList, buildingId, deviceId, metrics);

        // 3. 提取特征
        String features = featureExtractor.extractFeatures(abnormalPoints, dataList);

        // 4. 检索知识库
        String searchQuery = String.format("设备%d %s 异常原因", deviceId,
                abnormalPoints.stream().map(errorEntity::getMetric).distinct().collect(Collectors.joining(",")));
        String possibleCauses = ragflowService.searchKnowledgeBase(searchQuery);

        // 时段汇总统计
        Map<String, Long> hourlyStats = abnormalPoints.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:00")),
                        Collectors.counting()
                ));

        // 构建图表数据
        String primaryMetric = metrics != null && !metrics.isEmpty() ? metrics.get(0) : "power_consumption";
        List<DataPoint> chartData = buildChartData(dataList, abnormalPoints, primaryMetric);

        // 5. 封装返回数据
        AnomalyReportData reportData = new AnomalyReportData();
        reportData.setDeviceId(deviceId);
        reportData.setStartTime(start);
        reportData.setEndTime(end);
        reportData.setAnomalyFeatures(features);
        reportData.setAbnormalPoints(abnormalPoints);
        reportData.setPossibleCauses(possibleCauses);
        reportData.setHourlyStats(hourlyStats);
        reportData.setChartData(chartData);
        reportData.setTotalRecords(dataList.size());
        reportData.setAnomalyCount(abnormalPoints.size());
        return reportData;
    }

    /**
     * 真正的自动探索分析（智能选择时段）
     */
    public AnomalyReportData analyzeAuto(Integer buildingId, Integer deviceId, List<String> metrics) {

        // 1. 探索数据边界
        TimeBoundary boundary = energyReadingsMapper.selectTimeBoundary(deviceId);

        if (boundary == null || boundary.getTotal() == 0) {
            return createEmptyReport(deviceId, "数据库中无该设备数据");
        }

        // 2. 【关键修改】智能选择最佳分析时段！
        SelectedPeriod period = smartTimeSelector.selectBestPeriod(deviceId, boundary);

        // 3. 使用智能选定的时段进行分析
        AnomalyReportData report = analyze(buildingId, deviceId,
                period.getStart(), period.getEnd(), metrics);

        // 4. 添加智能选择信息到报告
        report.setAnalysisStrategy(period.getReason());
        report.setExpectedAnomalyCount(period.getExpectedAnomalyCount());

        return report;
    }

    private AnomalyReportData createEmptyReport(Integer deviceId, String message) {
        AnomalyReportData empty = new AnomalyReportData();
        empty.setDeviceId(deviceId);
        empty.setAnomalyCount(0);
        empty.setTotalRecords(0);
        empty.setAnomalyFeatures(message);
        return empty;
    }

    private List<DataPoint> buildChartData(List<DeviceEnergyBuildingVO> dataList,
                                           List<errorEntity> abnormalPoints,
                                           String metric) {
        Set<LocalDateTime> anomalyTimes = abnormalPoints.stream()
                .map(errorEntity::getTime)
                .collect(Collectors.toSet());

        return dataList.stream()
                .map(reading -> {
                    DataPoint point = new DataPoint();
                    point.setTime(reading.getMonitoringTime());
                    point.setValue(extractValue(reading, metric));
                    point.setAnomaly(anomalyTimes.contains(reading.getMonitoringTime()));
                    return point;
                })
                .collect(Collectors.toList());
    }

    private double extractValue(DeviceEnergyBuildingVO data, String metric) {
        if (data == null) return 0;
        switch (metric) {
            case "power_consumption":
                return data.getPowerConsumption() != null ? data.getPowerConsumption().doubleValue() : 0;
            case "water_consumption":
                return data.getWaterConsumption() != null ? data.getWaterConsumption().doubleValue() : 0;
            case "ac_power_consumption":
                return data.getAcPowerConsumption() != null ? data.getAcPowerConsumption().doubleValue() : 0;
            case "ac_outlet_temp":
                return data.getAcOutletTemp() != null ? data.getAcOutletTemp().doubleValue() : 0;
            case "ac_inlet_temp":
                return data.getAcInletTemp() != null ? data.getAcInletTemp().doubleValue() : 0;
            case "env_temp":
                return data.getEnvTemp() != null ? data.getEnvTemp().doubleValue() : 0;
            case "humidity":
                return data.getHumidity() != null ? data.getHumidity().doubleValue() : 0;
            case "occupancy_density":
                return data.getOccupancyDensity() != null ? data.getOccupancyDensity().doubleValue() : 0;
            default: return 0;
        }
    }
}