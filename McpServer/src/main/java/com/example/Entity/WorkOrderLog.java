package com.example.Entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("work_order_log")   // 指定表名
public class WorkOrderLog {

    @TableId(type = IdType.AUTO)   // MyBatis-Plus 主键注解
    private Long id;

    @TableField("order_id")        // 映射数据库列 order_id
    private Long orderId;           // 存储工单ID，而非关联对象

    private String action;

    @TableField("operator_id")
    private Long operatorId;        // 存储操作人ID

    private String content;

    @TableField(value = "created_at", fill = FieldFill.INSERT)   // 自动填充创建时间
    private LocalDateTime createdAt;
}