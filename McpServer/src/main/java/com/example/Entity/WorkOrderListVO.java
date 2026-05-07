package com.example.Entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WorkOrderListVO {
    private Long id;               // 工单ID
    private String orderNo;        // 工单编号
    private String type;           // 工单类型
    private String description;    // 故障描述
    private String location;       // 位置
    private String buildingName;   // 建筑名称（来自关联表）
    private String deviceCode;     // 设备编号（来自关联表）
    private String priority;       // 优先级
    private String status;         // 状态
    private LocalDateTime submitTime;      // 提交时间
    private LocalDateTime expectedDeadline; // 期望完成时间
    private LocalDateTime completedTime;    // 实际完成时间
    private String handlerName;    // 处理人姓名（来自关联表）
    private String remark;         // 备注
}