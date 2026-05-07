package com.example.Entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class BuildingEnergyPower {

    /** 建筑名称 */
    private String buildingName;

    /** 建筑类型 */
    private String buildingType;

    /** 设备编码 */
    private String deviceCode;

    /** 设备类型 */
    private String deviceType;

    /** 设备状态 */
    private String deviceStatus;

    /** 能耗读数 */
    private Double acPowerConsumption;

    /** 监测时间 */
    private LocalDateTime monitoringTime;
}
