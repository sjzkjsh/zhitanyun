package com.example.Entity.ChatEntity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

// entity/ChatContext.java
@Data
@TableName("AI_CHAT_CONTEXT")
public class ChatContext {
    @TableId
    private String conversationId;
    
    private String userId;
    private String title;
    private String systemPrompt;
    private Boolean isPinned;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

