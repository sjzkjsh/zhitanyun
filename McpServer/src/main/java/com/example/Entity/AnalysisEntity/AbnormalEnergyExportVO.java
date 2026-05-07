package com.example.Entity.AnalysisEntity;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AbnormalEnergyExportVO {
    @ExcelProperty("建筑编号")
    private String buildingCode;

    @ExcelProperty("建筑名称")
    private String buildingName;

    @ExcelProperty("设备编号")
    private String deviceCode;

    @ExcelProperty("设备ID")
    private Integer deviceId;

    @ExcelProperty("异常指标")
    private String metric;          // power_consumption, ac_power_consumption 等

    @ExcelProperty("实际值")
    private Double actualValue;

    @ExcelProperty("正常范围下限")
    private Double minThreshold;

    @ExcelProperty("正常范围上限")
    private Double maxThreshold;

    @ExcelProperty("偏差率")
    private String deviationRate;   // 例如 "+25%"

    @ExcelProperty("监测时间")
    private LocalDateTime monitoringTime;

    @ExcelProperty("可能异常原因")
    private String possibleReason;
}