package com.example.webapp.Entity;

import lombok.Data;

// DTO 类
@Data
public class LatestEnergyDTO {
    private Double acPower;
    private Double powerConsumption;
    private Double acPowerConsumption;
    private Double acInletTemp;
    private Double acOutletTemp;
    private Double waterFlowRate;
    private Double waterConsumption;
}