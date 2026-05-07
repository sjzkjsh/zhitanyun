package com.example.webapp.Entity;

import lombok.Data;

import java.util.Date;

@Data
public class Devices {
    private Integer deviceId;
    private String deviceCode;
    private Integer buildingId;
    private String deviceType;
    private String deviceStatus;  // 对应数据库 device_status
    private Date installDate;
    private Date createdAt;
}
