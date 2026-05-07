package com.example.webapp.Entity.ExcelVo;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class CopHealthExportVO {
    @ExcelProperty("健康等级")
    private String healthLevel;
    
    @ExcelProperty("综合评分")
    private Double score;
    
    @ExcelProperty("COP值")
    private Double cop;
    
    @ExcelProperty("诊断结论")
    private String diagnosis;
    
    @ExcelProperty("异常项")
    private String abnormalItems;
    
    @ExcelProperty("改进建议")
    private String suggestions;
}