	package com.example.webapp.Entity;
	import com.baomidou.mybatisplus.annotation.IdType;
	import com.baomidou.mybatisplus.annotation.TableId;
	import com.baomidou.mybatisplus.annotation.TableName;
	import lombok.Data;
	import java.util.Date;
	@Data
	@TableName("work_order_log")
	public class WorkOrderLog {
	    @TableId(type = IdType.AUTO)
	    private Integer id;
	    private Integer orderId;
	    private String action;      // 操作类型：创建、接单等
	    private Integer operatorId; // 操作人
	    private String content;     // 操作内容
	    private Date createdAt;
	}