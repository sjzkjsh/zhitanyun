package com.example.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeviceEnergyBuildingVO {

    private int readingId;
    // 建筑字段
    private Integer buildingId;
    private String buildingCode;
    private String buildingName;
    private String buildingType;
    private String location;
    // 设备字段
    private Integer deviceId;
    private String deviceType;
    private String installTime;
    private String deviceStatus;
    private String deviceCode;
    // 能耗字段
    private Double powerConsumption;
    private Double waterConsumption;
    private Double waterFlowRate;
    private Double acPowerConsumption;
    private Double acOutletTemp;
    private Double acInletTemp;
    private Double envTemp;
    private Double humidity;
    private Double occupancyDensity;
    private Double powerLoad;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;

    private String dataSource;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}