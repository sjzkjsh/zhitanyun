package com.example.webapp.Entity.Vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageVO {
    private String role;    // "user" 或 "assistant"
    private String content; // 消息内容
}