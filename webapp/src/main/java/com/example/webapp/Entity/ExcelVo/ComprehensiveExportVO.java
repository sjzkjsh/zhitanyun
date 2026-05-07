package com.example.webapp.Entity.ExcelVo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class ComprehensiveExportVO {
    @ExcelProperty("总体评价")
    private String overallAssessment;
    
    @ExcelProperty("预警级别")
    private String warningLevel;
    
    @ExcelProperty("优先行动建议")
    private String priorityActions;
    
    @ExcelProperty("COP值")
    private Double cop;
    
    @ExcelProperty("环境温度(℃)")
    private String envTemp;
    
    @ExcelProperty("湿度(%)")
    private String humidity;
    
    @ExcelProperty("人员密度(人/m²)")
    private String occupancyDensity;
    
    @ExcelProperty("总电耗(kWh)")
    private Double totalPower;
    
    @ExcelProperty("空调功耗(kW)")
    private Double acPower;
}