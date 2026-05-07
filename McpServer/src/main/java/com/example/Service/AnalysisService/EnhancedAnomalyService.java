package com.example.Service.AnalysisService;

import com.example.Entity.AnalysisEntity.ActionStep;
import com.example.Entity.AnalysisEntity.AnomalyReportData;
import com.example.Entity.AnalysisEntity.EnhancedAnomalyReport;
import com.example.Entity.AnalysisEntity.RootCauseHypothesis;
import com.example.Entity.CopEntity.CopDiagnosisResult;
import com.example.Entity.energyReadings;
import com.example.Enum.CopHealthStatus;
import com.example.Enum.PriorityLevel;
import com.example.Service.CopServiceImpl.CopAnalysisService;
import com.example.Service.EnergyReadingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnhancedAnomalyService {

    @Autowired
    private AnomalyAnalysisService anomalyService;

    @Autowired
    private EnergyReadingsService readingsService;

    @Autowired
    private CopAnalysisService copService;

    /**
     * 自动深度分析（无需指定时间）
     */
    public EnhancedAnomalyReport analyzeWithRootCauseAuto(Integer buildingId, Integer deviceId,
                                                          List<String> metrics) {
        // 1. 自动获取基础分析
        AnomalyReportData basicReport = anomalyService.analyzeAuto(buildingId, deviceId, metrics);

        // 如果无数据，返回空报告
        if (basicReport.getTotalRecords() == 0) {
            EnhancedAnomalyReport empty = new EnhancedAnomalyReport();
            empty.setBasicInfo(basicReport);
            empty.setRootCauses(List.of());
            empty.setActionPlan(List.of());
            empty.setPriority(PriorityLevel.LOW);
            empty.setAnalysisTime(LocalDateTime.now());
            return empty;
        }

        // 2-5. 深度分析逻辑
        List<RootCauseHypothesis> hypotheses = analyzeRootCause(basicReport, buildingId);
        List<ActionStep> actionPlan = generateActionPlan(hypotheses, basicReport);
        PriorityLevel priority = calculatePriority(basicReport, hypotheses);

        EnhancedAnomalyReport report = new EnhancedAnomalyReport();
        report.setBasicInfo(basicReport);
        report.setRootCauses(hypotheses);
        report.setActionPlan(actionPlan);
        report.setPriority(priority);
        report.setAnalysisTime(LocalDateTime.now());
        if (basicReport.getPossibleCauses() != null && !basicReport.getPossibleCauses().isEmpty()) {
            report.setPossibleCausesFromRag(basicReport.getPossibleCauses());
        }

        return report;
    }

    private List<RootCauseHypothesis> analyzeRootCause(AnomalyReportData report, Integer buildingId) {
        List<RootCauseHypothesis> hypotheses = new ArrayList<>();

        if (report.getAnomalyCount() == 0 || report.getAbnormalPoints() == null || report.getAbnormalPoints().isEmpty()) {
            return hypotheses;
        }

        // 一次性批量查询：获取时间范围内的所有读数，避免 N+1 查询
        LocalDateTime minTime = report.getAbnormalPoints().stream()
                .map(e -> e.getTime())
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusDays(7));
        LocalDateTime maxTime = LocalDateTime.now();

        List<energyReadings> allReadings = readingsService.selectByTime(
                report.getDeviceId() != null ? report.getDeviceId() : 0, minTime, maxTime);

        // 按时间排序，构建 TreeMap 用于快速查找最近的读数
        TreeMap<Long, energyReadings> readingMap = new TreeMap<>();
        for (energyReadings r : allReadings) {
            if (r.getMonitoringTime() != null) {
                long key = r.getMonitoringTime().toEpochSecond(ZoneOffset.UTC);
                readingMap.put(key, r);
            }
        }

        // 批量分析（传入预加载的数据）
        analyzeTemperatureImpact(report, hypotheses, readingMap);
        analyzeCopDegradation(report, buildingId, hypotheses);
        analyzeOccupancyImpact(report, hypotheses, readingMap);
        analyzeOperationTime(report, hypotheses);

        return hypotheses;
    }

    /**
     * 根据时间点查找最接近的读数（使用 TreeMap 避免逐条查询）
     */
    private energyReadings findClosestReading(TreeMap<Long, energyReadings> readingMap, LocalDateTime time) {
        if (readingMap.isEmpty() || time == null) return null;
        Long key = time.toEpochSecond(ZoneOffset.UTC);
        Map.Entry<Long, energyReadings> entry = readingMap.floorEntry(key);
        return entry != null ? entry.getValue() : null;
    }

    private void analyzeTemperatureImpact(AnomalyReportData report, List<RootCauseHypothesis> hypotheses,
                                           TreeMap<Long, energyReadings> readingMap) {
        if (report.getAbnormalPoints() == null || report.getAbnormalPoints().isEmpty()) {
            return;
        }

        long highTempAnomalies = report.getAbnormalPoints().stream()
                .filter(e -> {
                    energyReadings reading = findClosestReading(readingMap, e.getTime());
                    // envTemp 是 primitive double，默认 0.0，用 != 0 判断是否有数据
                    return reading != null && reading.getEnvTemp() != 0 && reading.getEnvTemp() > 35;
                }).count();

        double ratio = report.getAnomalyCount() > 0
                ? highTempAnomalies * 100.0 / report.getAnomalyCount()
                : 0;

        if (ratio > 50) {
            hypotheses.add(RootCauseHypothesis.builder()
                    .causeType("环境因素")
                    .causeName("高温环境影响")
                    .description(String.format("%.0f%%异常发生在高温时段 (>35℃)", ratio))
                    .confidence(Math.min(0.5 + ratio / 100, 0.95))
                    .suggestion("建议：优化高温时段运行策略，增加夜间预冷，或检查空调制冷能力")
                    .relatedMetric("env_temp")
                    .build());
        }
    }

    private void analyzeCopDegradation(AnomalyReportData report, Integer buildingId,
                                       List<RootCauseHypothesis> hypotheses) {
        // 如果 buildingId 为 null，跳过 COP 分析 (避免不必要的数据库查询)
        if (buildingId == null || report.getDeviceId() == null) {
            return;
        }
            
        try {
            LocalDateTime now = LocalDateTime.now();
            CopDiagnosisResult recentCop = copService.diagnoseCOP(
                    buildingId, report.getDeviceId(), now.minusDays(7), now);
    
            if (recentCop != null && 
                (recentCop.getStatus() == CopHealthStatus.CRITICAL ||
                 recentCop.getStatus() == CopHealthStatus.WARNING)) {
                hypotheses.add(RootCauseHypothesis.builder()
                        .causeType("设备性能")
                        .causeName("空调效率下降")
                        .description(String.format("近期 COP 为%.2f，评级：%s",
                                recentCop.getCurrentCop(), recentCop.getEfficiencyLevel()))
                        .confidence(0.75)
                        .suggestion(String.join("；", recentCop.getSuggestions()))
                        .relatedMetric("COP")
                        .build());
            }
        } catch (Exception e) {
            // 忽略 COP 分析失败，不影响其他根因分析
        }
    }

    private void analyzeOccupancyImpact(AnomalyReportData report,
                                        List<RootCauseHypothesis> hypotheses,
                                        TreeMap<Long, energyReadings> readingMap) {
        long highOccupancyAnomalies = report.getAbnormalPoints().stream()
                .filter(e -> {
                    energyReadings reading = findClosestReading(readingMap, e.getTime());
                    // occupancyDensity 是 primitive double，默认 0.0
                    return reading != null && reading.getOccupancyDensity() != 0 && reading.getOccupancyDensity() > 80;
                }).count();

        double ratio = report.getAnomalyCount() > 0
                ? highOccupancyAnomalies * 100.0 / report.getAnomalyCount()
                : 0;

        if (ratio > 40) {
            hypotheses.add(RootCauseHypothesis.builder()
                    .causeType("人为因素")
                    .causeName("人员密度过高")
                    .description(String.format("%.0f%%异常发生在人员密集时段", ratio))
                    .confidence(0.7)
                    .suggestion("建议：优化人员调度，或增加新风量以满足需求")
                    .relatedMetric("occupancy_density")
                    .build());
        }
    }

    private void analyzeOperationTime(AnomalyReportData report,
                                      List<RootCauseHypothesis> hypotheses) {
        if (report.getAbnormalPoints() == null || report.getAbnormalPoints().isEmpty()) {
            return;
        }
            
        long nightAnomalies = report.getAbnormalPoints().stream()
                .filter(e -> {
                    int hour = e.getTime().getHour();
                    return hour >= 22 || hour <= 6;
                }).count();
    
        double ratio = report.getAnomalyCount() > 0
                ? nightAnomalies * 100.0 / report.getAnomalyCount()
                : 0;
    
        if (ratio > 60) {
            hypotheses.add(RootCauseHypothesis.builder()
                    .causeType("运行管理")
                    .causeName("非必要时段运行")
                    .description(String.format("%.0f%%异常发生在夜间 (22:00-06:00)", ratio))
                    .confidence(0.85)
                    .suggestion("建议：检查定时开关设置，避免无人时段设备空转")
                    .relatedMetric("operation_time")
                    .build());
        }
    }

    private List<ActionStep> generateActionPlan(List<RootCauseHypothesis> hypotheses,
                                                AnomalyReportData report) {
        List<ActionStep> steps = new ArrayList<>();
        int stepNum = 1;

        hypotheses.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));

        for (RootCauseHypothesis h : hypotheses) {
            steps.add(ActionStep.builder()
                    .stepNumber(stepNum++)
                    .title("处理：" + h.getCauseName())
                    .description(h.getSuggestion())
                    .priority(h.getConfidence() > 0.8 ? "高" : "中")
                    .estimatedTime("30分钟")
                    .build());
        }

        steps.add(ActionStep.builder()
                .stepNumber(stepNum)
                .title("验证处理效果")
                .description("观察后续2小时能耗数据，确认异常消除")
                .priority("必做")
                .estimatedTime("2小时")
                .build());

        return steps;
    }

    private PriorityLevel calculatePriority(AnomalyReportData report,
                                            List<RootCauseHypothesis> hypotheses) {
        long criticalCauses = hypotheses.stream()
                .filter(h -> h.getConfidence() > 0.8)
                .count();

        if (report.getAnomalyCount() > 10 || criticalCauses > 0) {
            return PriorityLevel.HIGH;
        } else if (report.getAnomalyCount() > 5) {
            return PriorityLevel.MEDIUM;
        }
        return PriorityLevel.LOW;
    }
}