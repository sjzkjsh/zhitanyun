package com.example.webapp.Entity.ExcelVo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class EnergyExportVO {
    @ExcelProperty("总电力能耗(kWh)")
    private Double powerConsumption;
    
    @ExcelProperty("空调功耗(kW)")
    private Double acPowerConsumption;
    
    @ExcelProperty("空调入口温度(℃)")
    private Double acInletTemp;
    
    @ExcelProperty("空调出口温度(℃)")
    private Double acOutletTemp;
    
    @ExcelProperty("水流量(m³/h)")
    private Double waterFlowRate;
    
    @ExcelProperty("水耗(m³)")
    private Double waterConsumption;
}