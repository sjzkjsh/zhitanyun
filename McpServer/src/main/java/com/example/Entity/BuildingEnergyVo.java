package com.example.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuildingEnergyVo {
    private String buildingName;
    private BigDecimal totalEnergy;  // 使用 BigDecimal 保持精度
}