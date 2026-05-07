package com.example.Entity.ChatEntity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// entity/ChatMessage.java
@Data
@TableName("AI_CHAT_MEMORY")
public class ChatMessage {
    @TableId
    private String id;
    private String conversationId;
    private String messageType;  // user/assistant/system
    private String content;
    private String metadataJson;
    private LocalDateTime createdAt;
}
