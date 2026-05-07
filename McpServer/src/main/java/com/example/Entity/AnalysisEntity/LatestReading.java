package com.example.Entity.AnalysisEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LatestReading {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;
    private BigDecimal powerConsumption;
    private BigDecimal acPowerConsumption;
    private BigDecimal envTemp;
    
    // 计算字段
    public Double getPowerConsumptionDouble() {
        return powerConsumption != null ? powerConsumption.doubleValue() : 0;
    }
    
    public Double getAcPowerConsumptionDouble() {
        return acPowerConsumption != null ? acPowerConsumption.doubleValue() : 0;
    }
    
    public Double getEnvTempDouble() {
        return envTemp != null ? envTemp.doubleValue() : 0;
    }
}