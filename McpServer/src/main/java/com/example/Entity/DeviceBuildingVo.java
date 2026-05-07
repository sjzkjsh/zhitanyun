package com.example.Entity;

import lombok.Data;

@Data
public class DeviceBuildingVo {

    private Integer buildingId;
    private Integer deviceId;
    private String deviceCode;
    private String deviceType;
    private String deviceStatus;
}
