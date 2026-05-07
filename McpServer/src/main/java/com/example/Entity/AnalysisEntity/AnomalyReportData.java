package com.example.Entity.AnalysisEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;




@Data
public class AnomalyReportData {
    private Integer deviceId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    private String anomalyFeatures;
    private List<errorEntity> abnormalPoints;
    private String possibleCauses;

    // 原有字段
    private int totalRecords;
    private int anomalyCount;
    private Map<String, Long> hourlyStats;
    private List<DataPoint> chartData;

    // 新增：自动分析相关字段
    private String analysisStrategy;        // 智能选择策略说明
    private Integer expectedAnomalyCount;   // 预估异常数
}