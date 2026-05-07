package com.example.webapp.Entity;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class ComprehensiveAnalysisResult {
    private CopHealthResult copHealth;           // COP 健康子项
    private Map<String, Object> energyMetrics;   // 能耗指标
    private Map<String, Object> envMetrics;      // 环境指标
    private Map<String, Object> efficiencyIndicators; // 效率指标
    private String overallAssessment;            // 总体评价
    private List<String> priorityActions;        // 优先行动建议
    private String warningLevel;                 // NORMAL / WARNING / CRITICAL
    private boolean dataComplete;                // 数据是否完整
}