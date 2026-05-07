package com.example.Entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkOrderStatusCountVO {
    private String statusName;      // 工单状态名称
    private LocalDate createDate;   // 创建日期（只取年月日）
    private Long count;             // 数量
}