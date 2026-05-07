package com.example.Entity.AnalysisEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActionStep {
    private int stepNumber;         // 步骤序号
    private String title;           // 标题
    private String description;     // 详细描述
    private String priority;        // 高/中/低/必做
    private String estimatedTime;   // 预计耗时
}