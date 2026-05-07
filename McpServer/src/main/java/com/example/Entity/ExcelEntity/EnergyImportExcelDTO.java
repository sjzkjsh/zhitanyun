package com.example.Entity.ExcelEntity;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import lombok.Data;
import java.time.LocalDateTime;
@Data
public class EnergyImportExcelDTO {
    // 建筑信息
    @ExcelProperty("建筑编号")
    private String buildingCode;
    @ExcelProperty("建筑名称")
    private String buildingName;
    @ExcelProperty("建筑类型")
    private String buildingType;
    @ExcelProperty("位置")
    private String location;
    // 设备信息
    @ExcelProperty("设备编号")
    private String deviceCode;
    @ExcelProperty("设备类型")
    private String deviceType;
    @ExcelProperty("安装时间")
    private String installTime;  // 保持String类型，因为Excel可能是文本格式
    @ExcelProperty("设备状态")
    private String deviceStatus;
    // 能耗数据
    @ExcelProperty("监控时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime monitoringTime;
    @ExcelProperty("电力能耗")
    private Double powerConsumption;
    @ExcelProperty("水消耗")
    private Double waterConsumption;
    @ExcelProperty("水流量")
    private Double waterFlowRate;
    @ExcelProperty("空调功耗")
    private Double acPowerConsumption;
    @ExcelProperty("空调出口温度")
    private Double acOutletTemp;
    @ExcelProperty("空调入口温度")
    private Double acInletTemp;
    @ExcelProperty("环境温度")
    private Double envTemp;
    @ExcelProperty("湿度")
    private Double humidity;
    @ExcelProperty("人员密度")
    private Double occupancyDensity;
    // 新增字段（对应 energyReadings 实体）
    @ExcelProperty("空调功率")
    private Double acPower;
    @ExcelProperty("电力负载")
    private Double powerLoad;
    @ExcelProperty("数据来源")
    private String dataSource;
    @ExcelProperty("原始文件")
    private String rawFile;
    @ExcelProperty("结束时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    // 非Excel字段
    private Integer rowNum;
    private String errorMsg;
    private Integer buildingId;
    private Integer deviceId;
}