	package com.example.webapp.Entity.Vo;
	import com.alibaba.excel.annotation.ExcelProperty;
	import com.alibaba.excel.annotation.write.style.ColumnWidth;
	import lombok.Data;
	@Data
	public class MonthlyAnalysisExportVO {
	    @ExcelProperty("月份")
	    @ColumnWidth(15)
	    private String month;
	    @ExcelProperty("总用电量")
	    @ColumnWidth(15)
	    private Double power;
	    @ExcelProperty("总用水量(m³)")
	    @ColumnWidth(15)
	    private Double water;
	    @ExcelProperty("环比变化率(%)")
	    @ColumnWidth(15)
	    private String changeRateStr; // 转成字符串方便加百分号
	    @ExcelProperty("趋势判定")
	    @ColumnWidth(12)
	    private String trend;
	    @ExcelProperty("状态")
	    @ColumnWidth(10)
	    private String status;
	    @ExcelProperty("智能分析建议")
	    @ColumnWidth(40)
	    private String suggestion;
	}