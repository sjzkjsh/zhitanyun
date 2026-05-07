package com.example.Entity.AnalysisEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//阈值范围实体类，用于存储设备的阈值范围
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThresholdRange {
    private Long id;//主键ID
    private Integer buildingId;//建筑ID
    private Integer deviceId;//设备ID
    private String metricName;//指标名称
    private BigDecimal minValue;//最小值
    private BigDecimal maxValue;//最大值
    private String unit;// 单位
    private String description;//描述
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveFrom;//生效时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime effectiveTo;//失效时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;//创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;//更新时间
}