package com.example.webapp.Entity.ExcelVo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CopExportVO {
    @ExcelProperty("COP值")
    private Double cop;
    
    @ExcelProperty("制冷量(kW)")
    private Double coolingCapacity;
    
    @ExcelProperty("空调功耗(kW)")
    private Double powerConsumption;
    
    @ExcelProperty("供回水温差(℃)")
    private Double deltaT;
    
    @ExcelProperty("水流量(m³/h)")
    private Double waterFlowRate;
    
    @ExcelProperty("数据有效性")
    private String valid;
    
    @ExcelProperty("提示信息")
    private String message;
}