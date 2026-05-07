package com.example.webapp.Entity.Vo;

import lombok.Data;

@Data
public class WorkerOrderVo {
    private String orderNo;//工单编号
    private String type;//工单类型
    private String description;//工单描述
    private String priority;//工单优先级
    private String status;//工单状态
    private String submitTime;//工单提交时间
    private String expectedDeadline;//工单期望完成时间
}
