package com.example.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 能耗读数查询结果实体
 * 对应 SQL 中的字段：
 * b.building_name, b.building_type, d.device_code, d.device_type,
 * d.device_status, er.power_consumption, er.monitoring_time
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyReadingVO {

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

    /** 能耗读数（用电量等） */
    private Double powerConsumption;

    /** 监测时间 */
    private LocalDateTime monitoringTime;
}