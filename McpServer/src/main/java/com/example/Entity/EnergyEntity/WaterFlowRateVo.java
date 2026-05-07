package com.example.Entity.EnergyEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WaterFlowRateVo {


    private Double waterFlowRate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;// 监控时间monitoring_time
}
