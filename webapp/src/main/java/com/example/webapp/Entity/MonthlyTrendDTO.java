package com.example.webapp.Entity;

import lombok.Data;

@Data
public class MonthlyTrendDTO {
    private String month;       // 格式 yyyy-MM，例如 "2026-03"
    private Double totalPower;  // 当月电耗总和 (kWh)
    private Double totalWater;  // 当月水耗总和 (m³)
    private Double totalAcPower;// 当月空调能耗总和 (kWh)
    private Double totalTce;    // 当月总能耗 (折算为吨标准煤)
}