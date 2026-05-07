package com.example.Entity.ChatEntity;

import lombok.Data;

// dto/ContextUpdateRequest.java
@Data
public class ContextUpdateRequest {
    private String title;
    private String systemPrompt;
    private Boolean isPinned;
}
