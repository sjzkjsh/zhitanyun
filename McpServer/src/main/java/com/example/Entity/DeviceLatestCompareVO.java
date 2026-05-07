package com.example.Entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;




@Data
public class DeviceLatestCompareVO {
    private String buildingCode;        // 建筑编号
    private String buildingName;        // 建筑名称
    private String deviceCode;          // 设备编号
    private LocalDateTime latestTime;   // 最新监测时间（可选）

    // 电耗
    private BigDecimal latestPower;
    private BigDecimal powerChangePercent;

    // 水耗
    private BigDecimal latestWater;
    private BigDecimal waterChangePercent;

    // 空调系统能耗
    private BigDecimal latestAcPower;
    private BigDecimal acPowerChangePercent;

}