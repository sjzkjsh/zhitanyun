package com.example.Entity;



import com.example.Enum.Priority;
import com.example.Enum.WorkOrderStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.Id;


import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", columnDefinition = "INT UNSIGNED")
    private Long id;

    @Column(name = "order_no", length = 50, nullable = false, unique = true)
    private String orderNo;

    @Column(length = 20, nullable = false)
    private String type;   // 工单类型，可以设计为枚举

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(length = 100)
    private String location;

    @ManyToOne
    @JoinColumn(name = "building_id", referencedColumnName = "building_id")
    private Long buildingId;

    @ManyToOne
    @JoinColumn(name = "equipment_id", referencedColumnName = "device_id")
    private Long equipmentId;   // 注意这里映射的是 device_id 字段

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('高','中','低')")
    private Priority priority;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "ENUM('待处理','处理中','已完成','已关闭')")
    private WorkOrderStatus status;

    @Column(name = "submit_time", nullable = false)
    private LocalDateTime submitTime;

    @Column(name = "expected_deadline")
    private LocalDateTime expectedDeadline;// 预计完成时间

    @Column(name = "completed_time")
    private LocalDateTime completedTime;// 实际完成时间

    @ManyToOne
    @JoinColumn(name = "handler_id", referencedColumnName = "id")
    private Long handlerId;   // 处理人

    @Column(columnDefinition = "TEXT")
    private String remark;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "version")
    private Integer version = 0;
}