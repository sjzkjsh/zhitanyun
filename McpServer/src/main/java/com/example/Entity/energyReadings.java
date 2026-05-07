package com.example.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("energy_readings")
public class energyReadings {
    private int readingId;//读取id
    private int deviceId;//设备id
    private int buildingId;//建筑id
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;//监控时间
    private double powerConsumption;//电力能耗
    private double waterConsumption;//水耗
    private double waterFlowRate;//水流
    private double acPower;//空调功率
    private double acPowerConsumption;//空调功耗
    private double acOutletTemp;//空调出口温度
    private double acInletTemp;//空调入口温度
    private double envTemp;//环境温度
    private double humidity;//湿度
    private double occupancyDensity;//人员密度
    private Double powerLoad;//电力负载
    private String dataSource;//数据来源
    private String rawFile;//原始文件
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;//创建时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
