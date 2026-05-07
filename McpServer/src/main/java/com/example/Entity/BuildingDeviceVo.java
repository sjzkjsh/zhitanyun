package com.example.Entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
public class BuildingDeviceVo {
    private int buildingId;//建筑id
    private String buildingName;//建筑名称
    private String buildingCode;//建筑编号
    private String buildingType;//建筑类型
    private String location;

    private Integer deviceId;
    private String deviceCode;//
    private String deviceType;//设备类型
    private String deviceStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;//创建时间
    private Date installDate;
}
