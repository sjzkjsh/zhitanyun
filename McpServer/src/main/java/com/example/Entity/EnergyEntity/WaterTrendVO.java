package com.example.Entity.EnergyEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WaterTrendVO {

    private Double waterConsumption;// 水能耗
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;// 监控时间monitoring_time
}
