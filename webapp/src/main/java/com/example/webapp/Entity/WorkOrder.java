	package com.example.webapp.Entity;
	import com.baomidou.mybatisplus.annotation.IdType;
	import com.baomidou.mybatisplus.annotation.TableId;
	import com.baomidou.mybatisplus.annotation.TableName;
	import jakarta.persistence.Column;
	import lombok.Data;

	import java.time.LocalDateTime;
	import java.util.Date;
	@Data
	@TableName("work_order")
	public class WorkOrder {
	    @TableId(type = IdType.AUTO)
	    private Integer id;
	    private String orderNo;        // 工单编号
	    private String type;           // 工单类型
	    private String description;    // 故障描述
	    private String location;       // 位置
	    private Integer buildingId;    // 建筑ID
	    private Integer equipmentId;   // 设备ID
	    private String priority;       // 优先级 (高, 中, 低)
	    private String status;         // 状态 (待处理, 处理中...)
	    private Date submitTime;       // 提交时间
	    private LocalDateTime expectedDeadline; // 期望完成时间
	    private Date completedTime;
	    private Integer handlerId;     // 处理人ID
	    private String remark;
	    private Date createdAt;
	    private Date updatedAt;
		@Column(name = "version")
		private Integer version = 0;
	}