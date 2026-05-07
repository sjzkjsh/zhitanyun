package com.example.Entity.EnergyEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AcPowerTrendVO {

    private Double acPowerConsumption;// 空调能耗
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;// 监控时间monitoring_time
}
