package com.example.webapp.Entity;

import lombok.Data;

@Data
public class MonthComparisonDTO {
    private Double lastMonthPower;
    private Double lastMonthWater;
    private Double lastMonthAcPower;
    private Double lastMonthTce;
    private Double prevMonthPower;
    private Double prevMonthWater;
    private Double prevMonthAcPower;
    private Double prevMonthTce;
}