package com.example.Entity.PieEntity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceEnergy {

    private String deviceCode;
    private String deviceType;
    private BigDecimal value;
}
