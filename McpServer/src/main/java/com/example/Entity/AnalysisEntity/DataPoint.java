package com.example.Entity.AnalysisEntity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DataPoint {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
    private double value;
    private boolean anomaly;  // true 表示异常点
    // getters/setters 略
}