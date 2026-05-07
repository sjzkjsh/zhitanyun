package com.example.Entity.PieEntity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BuildingEnergy {

    private String buildingCode;
    private String buildingName;
    private BigDecimal value;
}
