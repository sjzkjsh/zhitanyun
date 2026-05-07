package com.example.Entity.CopEntity;

import com.example.Enum.CopHealthStatus;
import lombok.Data;

import java.util.List;

@Data
public class CopDiagnosisResult {
    private double currentCop;           // 当前COP
    private double lastMonthCop;         // 上月COP
    private CopHealthStatus status;      // 健康状态
    private String diagnosis;            // 诊断结论
    private List<String> suggestions;    // 建议列表
    private String efficiencyLevel;      // 效率等级

    // 新增字段（用于详细诊断）
    private Integer totalDataPoints;     // 总数据点数
    private Integer validDataPoints;     // 有效数据点数
    private String queryTarget;          // 查询目标（建筑/设备）
    private String analysisPeriod;       // 分析时间段
    private String deviceDistribution;   // 设备分布信息（建筑查询时）

}

