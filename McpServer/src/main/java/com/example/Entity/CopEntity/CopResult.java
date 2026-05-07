package com.example.Entity.CopEntity;

import lombok.Data;

@Data
public class CopResult {
    private Double averageCop;      // 平均 COP
    private Double totalCooling;    // 总制冷量（kWh）
    private Double totalPower;       // 总输入电能（kWh）
    private int validPoints;         // 有效数据点数
}