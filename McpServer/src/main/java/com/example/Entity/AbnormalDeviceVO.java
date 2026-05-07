package com.example.Entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AbnormalDeviceVO {
    private Integer buildingId;          // 建筑ID
    private Integer deviceId;            // 设备ID
    private String metricName;           // 【核心】超标指标名称（如：power_consumption 用电量）
    private String metricCnName;        // 指标中文名称（方便前端展示）
    private BigDecimal abnormalValue;   // 超标数值
    private BigDecimal minVal;        // 阈值最小值
    private BigDecimal maxVal;        // 阈值最大值
    private String unit;                // 单位
    private String description;         // 异常描述（超出上限/低于下限）
    private LocalDateTime monitoringTime;// 监控时间
}