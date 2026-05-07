package com.example.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class energyDeviceVo {
    // 设备字段
    private String deviceCode;
    private String deviceType;
    private String deviceStatus;

    // 建筑字段
    private String buildingName;
    private String buildingCode;
    private String buildingType;

    // 能耗字段（来自 energy_readings 表）
    private BigDecimal powerConsumption;      // 总用电
    private BigDecimal waterFlowRate;         // 水流量
    private BigDecimal waterConsumption;      // 水耗
    private BigDecimal acPowerConsumption;    // 空调用电


}
