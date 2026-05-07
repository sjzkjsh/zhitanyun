package com.example.Entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DeviceEnergyVo {

        // 分类名称（对应 SQL 别名 "分类名称"）
        private String deviceType;

        // 设备数量（对应 SQL 别名 "设备数量"）
        private Integer deviceCount;

        // 总能耗（对应 SQL 别名 "总能耗"）
        private BigDecimal totalEnergy;


}
