package com.example.Entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AlertQueryVO {
    private Integer buildingId;
    private Integer deviceId;
    private String deviceCode;
    private String deviceType;   // 设备类型
    private String metricName;
    private BigDecimal abnormalValue;
    private String alertType;    // ABOVE_MAX / BELOW_MIN
    private Integer alertLevel;  // 1,2,3
    private Integer status;
    private LocalDateTime createdAt;
}