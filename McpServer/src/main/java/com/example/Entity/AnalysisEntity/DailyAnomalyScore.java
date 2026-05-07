package com.example.Entity.AnalysisEntity;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class DailyAnomalyScore {
    private LocalDate date;
    private Integer anomalyCount;
    private BigDecimal maxPower;
    private BigDecimal avgPower;
    private BigDecimal volatility;          // 标准差
    private Double score;                     // 综合评分
}
