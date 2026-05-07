package org.webSocketDemo.Entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@TableName("chat_record") // 假设表名为 chat_message，请根据实际修改
public class ChatMessage {
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	@TableField("sender_type")
	private String senderType;
	@TableField("sender_id")
	private String senderId;
	@TableField("sender_name")
	private String senderName;
	@TableField("receiver_id")
	private String receiverId;
	@TableField("content") private String content;
	@TableField("type")
	private Integer type;
	@TableField("create_time")
	private LocalDateTime createTime;
}