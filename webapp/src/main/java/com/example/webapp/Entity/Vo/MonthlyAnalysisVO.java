package com.example.webapp.Entity.Vo;

import lombok.Data;

@Data
public class MonthlyAnalysisVO {
    private String month;        // 月份 (如 "2026-01")
    private Double power;        // 当月总电耗
    private Double water;        // 当月总水耗
    
    // --- 分析字段 ---
    private Double changeRate;   // 环比变化率 (%)
    private String trend;        // 趋势描述：上升 / 下降 / 持平
    private String status;       // 状态：正常 / 警告 / 良好
    private String suggestion;   // 节能建议
}