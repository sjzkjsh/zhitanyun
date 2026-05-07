package com.example.Entity.ExcelEntity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Excel导入模板DTO - 对应前端展示的VO结构
 */
@Data
public class DeviceEnergyImportDTO {

    // ========== 建筑信息 ==========
    @ExcelProperty("建筑编号")
    @ColumnWidth(15)
    private String buildingCode;

    @ExcelProperty("建筑名称")
    @ColumnWidth(20)
    private String buildingName;

    @ExcelProperty("地址")
    @ColumnWidth(25)
    private String location;

    // ========== 设备信息 ==========
    @ExcelProperty("设备编号")
    @ColumnWidth(15)
    private String deviceCode;

    @ExcelProperty("设备类型")
    @ColumnWidth(12)
    private String deviceType;

    @ExcelProperty("安装时间")
    @DateTimeFormat("yyyy-MM-dd")
    @ColumnWidth(12)
    private String installTime;

    @ExcelProperty("设备状态")
    @ColumnWidth(10)
    private String deviceStatus;

    // ========== 能耗数据 ==========
    @ExcelProperty("电耗(kWh)")
    @ColumnWidth(12)
    private Double powerConsumption;

    @ExcelProperty("水耗(m³)")
    @ColumnWidth(12)
    private Double waterConsumption;

    @ExcelProperty("水流量")
    @ColumnWidth(10)
    private Double waterFlowRate;

    @ExcelProperty("空调能耗(kWh)")
    @ColumnWidth(14)
    private Double acPowerConsumption;

    @ExcelProperty("空调出风温度(°C)")
    @ColumnWidth(16)
    private Double acOutletTemp;

    @ExcelProperty("空调回风温度(°C)")
    @ColumnWidth(16)
    private Double acInletTemp;

    @ExcelProperty("环境温度(°C)")
    @ColumnWidth(14)
    private Double envTemp;

    @ExcelProperty("湿度(%)")
    @ColumnWidth(10)
    private Double humidity;

    @ExcelProperty("人员密度(人/m²)")
    @ColumnWidth(16)
    private Double occupancyDensity;

    @ExcelProperty("监控时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @ColumnWidth(18)
    private LocalDateTime monitoringTime;

    // ========== 非Excel字段（用于错误记录） ==========
    private Integer rowIndex;      // 行号
    private String errorMsg;       // 错误信息
}