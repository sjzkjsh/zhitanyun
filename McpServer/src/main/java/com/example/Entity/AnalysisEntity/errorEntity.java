package com.example.Entity.AnalysisEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class errorEntity {
        private Integer deviceId;
        private LocalDateTime time;
        private String metric;
        private BigDecimal value;
        private BigDecimal min;
        private BigDecimal max;
        private String unit;
        private String description;
        private String severity;        // 严重程度：LOW/MEDIUM/HIGH/CRITICAL
        private Long configId;          // 使用的阈值配置ID
}