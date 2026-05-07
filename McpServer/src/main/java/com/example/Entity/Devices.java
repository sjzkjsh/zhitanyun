package com.example.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class Devices {
    @TableId(value = "device_id", type = IdType.AUTO)
    private Integer deviceId;
    private String deviceCode;
    private Integer buildingId;
    private String deviceType;
    private String deviceStatus;  // 对应数据库 device_status
    private Date installDate;
    private Date createdAt;

}
