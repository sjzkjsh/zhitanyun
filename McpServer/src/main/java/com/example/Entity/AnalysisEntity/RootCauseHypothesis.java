package com.example.Entity.AnalysisEntity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RootCauseHypothesis {
    private String causeType;      // 环境因素/设备性能/人为因素/运行管理
    private String causeName;      // 具体原因名称
    private String description;    // 详细描述
    private double confidence;     // 置信度 0-1
    private String suggestion;     // 处理建议
    private String relatedMetric;  // 关联指标
}