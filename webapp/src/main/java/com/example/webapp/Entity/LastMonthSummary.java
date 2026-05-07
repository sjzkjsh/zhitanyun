package com.example.webapp.Entity;

import lombok.Data;

@Data
public class LastMonthSummary {
    private Double totalPower;      // 电耗 (kWh)
    private Double totalWater;      // 水耗 (m³)
    private Double totalAcPower;    // 空调能耗 (kWh)
    private Double totalTce;        // 总能耗 (吨标准煤)
}