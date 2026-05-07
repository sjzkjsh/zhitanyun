package com.example.Service;

import com.example.Entity.ChatEntity.ChatMessage;
import com.example.Entity.ChatEntity.ContextCreateRequest;
import com.example.Entity.ChatEntity.ContextListVO;
import com.example.Entity.ChatEntity.ContextUpdateRequest;
import reactor.core.publisher.Flux;

import java.util.List;

// service/ChatContextService.java
public interface ChatContextService {
    
    // 新建上下文
    String createContext(ContextCreateRequest request);
    
    // 获取用户会话列表
    List<ContextListVO> listUserContexts(String userId);
    
    // 获取历史消息
    List<ChatMessage> getContextHistory(String conversationId);
    
    // 更新上下文信息
    void updateContext(String conversationId, ContextUpdateRequest request);
    
    // 删除上下文（连同消息）
    void deleteContext(String conversationId);
    
    // 确保上下文存在（不存在则自动创建）
    void ensureContextExists(String conversationId, String userId);
}

