package com.example.Entity.ChatEntity;

import lombok.Data;

// dto/ContextCreateRequest.java
@Data
public class ContextCreateRequest {
    private String userId;
    private String title = "新对话";
    private String systemPrompt;
}
