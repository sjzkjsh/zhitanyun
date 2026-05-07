package com.example.Entity.PieEntity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
public class EnergyPieResult implements Serializable {
    private String level;           // building / device / detail
    private String title;
    private List<PieItem> pieData;    // 扇形图数据
    
    @Data
    public static class PieItem {
        private String id;
        private String name;
        private BigDecimal value;     // 统一用BigDecimal
        private Double percent;
    }
}