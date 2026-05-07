package com.example.Entity.AnalysisEntity;

import com.example.Entity.AnalysisEntity.DailyAnomalyScore;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class SelectedPeriod {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime start;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime end;
    private String reason;                    // 为什么选择这个时段
    private Integer expectedAnomalyCount;     // 预估异常数
    private List<DailyAnomalyScore> dailyBreakdown;  // 每日详情
}

