package com.example.Entity.AnalysisEntity;

import com.example.Enum.PriorityLevel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data

@NoArgsConstructor
public class EnhancedAnomalyReport {
    private AnomalyReportData basicInfo;        // 基础异常数据
    private List<RootCauseHypothesis> rootCauses; // 根因假设列表
    private List<ActionStep> actionPlan;        // 处理步骤
    private PriorityLevel priority;              // 优先级
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime analysisTime;          // 分析时间

    private String possibleCausesFromRag;  // 来自知识库的可能原因
}