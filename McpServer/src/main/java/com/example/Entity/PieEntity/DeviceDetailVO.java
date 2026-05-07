package com.example.Entity.PieEntity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceDetailVO {
    private Integer deviceId;
    private String deviceCode;
    private String deviceType;
    private BigDecimal waterConsumption;
    private BigDecimal powerConsumption;
    private BigDecimal acPowerConsumption;
}