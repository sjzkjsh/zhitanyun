package com.example.webapp.Entity;

import lombok.Data;

@Data
public class InstantCopResult {
    private Double cop;               // COP 值
    private Double coolingCapacity;   // 制冷量 (kW)
    private Double powerConsumption;  // 空调功耗 (kW)
    private Double deltaT;            // 供回水温差 (℃)
    private Double waterFlowRate;     // 水流量（原始单位，如 m³/h）
    private boolean valid;            // 数据是否有效
    private String message;           // 提示信息
}