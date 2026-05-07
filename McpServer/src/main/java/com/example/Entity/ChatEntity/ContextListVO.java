package com.example.Entity.ChatEntity;

import lombok.Data;

import java.time.LocalDateTime;

// vo/ContextListVO.java - 给前端展示的会话列表项
@Data
public class ContextListVO {
    private String conversationId;
    private String userId;
    private String title;
    private String systemPrompt;
    private Boolean isPinned;
    private Long messageCount;      // 消息数量
    private LocalDateTime lastMessageTime;  // 最后消息时间
    private LocalDateTime createdAt;
}

