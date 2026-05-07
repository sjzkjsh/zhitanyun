package com.example.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlertVo {

    private Long id;
    private Integer deviceId;
    private String deviceCode;
    private String buildingName;       // 来自 devices 表
    private String deviceType;       // 来自 devices 表（需确认字段存在）
    private Integer buildingId;
    private String metricName;
    private BigDecimal abnormalValue;
    private BigDecimal minVal;
    private BigDecimal maxVal;
    private String unit;
    private String alertType;
    private Integer alertLevel;
    private Integer status;
    private String handledBy;
    private LocalDateTime handledAt;
    private String remark;
    private LocalDateTime createdAt;
}
