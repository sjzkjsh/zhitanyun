package com.example.Entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
public class DeviceLatestReadingVO {
    private Integer deviceId;
    private String deviceCode;
    private Integer buildingId;
    private String buildingCode;
    private String buildingName;
    private BigDecimal powerConsumption;
    private BigDecimal acPowerConsumption;
    private BigDecimal waterConsumption;
    private BigDecimal envTemp;
    private BigDecimal humidity;
    private BigDecimal occupancyDensity;
    private LocalDateTime monitoringTime;
}