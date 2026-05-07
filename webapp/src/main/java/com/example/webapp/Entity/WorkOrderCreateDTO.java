package com.example.webapp.Entity;
	import lombok.Data;
	import java.util.Date;
	@Data
	public class WorkOrderCreateDTO {
	    private String type;           // 必填：如 "设备故障"
	    private String description;    // 必填：故障详情
	    private String priority;       // 可选：默认"中"
	    private Date expectedDeadline; // 可选：期望解决时间
	}