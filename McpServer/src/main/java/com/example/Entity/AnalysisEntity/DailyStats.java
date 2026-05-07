package com.example.Entity.AnalysisEntity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DailyStats {
    private LocalDate date;
    private Integer recordCount;
    private BigDecimal avgPower;
    private BigDecimal maxPower;
    private BigDecimal powerStd;              // 标准差
    private Integer thresholdExceedCount;     // 超标次数
}
