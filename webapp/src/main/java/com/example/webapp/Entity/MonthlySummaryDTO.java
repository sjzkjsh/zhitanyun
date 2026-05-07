package com.example.webapp.Entity;

import lombok.Data;

@Data
public class MonthlySummaryDTO {
    private String deviceCode;   // 设备编号
    private Double totalPower;
    private Double totalWater;
    private Double totalAcPower;
    private Double totalTce;
}