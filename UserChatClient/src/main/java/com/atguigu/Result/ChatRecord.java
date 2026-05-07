package com.atguigu.Result;
	import com.baomidou.mybatisplus.annotation.IdType;
	import com.baomidou.mybatisplus.annotation.TableId;
	import com.baomidou.mybatisplus.annotation.TableName;
	import lombok.Data;
	import java.time.LocalDateTime;
	@Data
	@TableName("chat_record") // 对应数据库表名
	public class ChatRecord {
	    @TableId(type = IdType.AUTO)
	    private Long id; // 数据库自增主键
	    // 以下字段对应 9092 传过来的 JSON 数据
	    private String senderId;   // 发送者ID (如 customer_888)
	    private String senderName; // 发送者昵称 (如 王先生)
	    private String receiverId; // 接收者ID (如 admin_001)
	    private String content;    // 消息内容
	    private Integer type;      // 消息类型：0-文本, 1-图片...
	    private LocalDateTime createTime; // 发送时间
	}