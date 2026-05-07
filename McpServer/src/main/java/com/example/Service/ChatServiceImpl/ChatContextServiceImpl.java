package com.example.Service.ChatServiceImpl;

import com.example.Entity.ChatEntity.*;
import com.example.Mapper.ChatContextMapper;
import com.example.Mapper.ChatMemoryMapper;
import com.example.Service.ChatContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatContextServiceImpl implements ChatContextService {

    private final ChatContextMapper contextMapper;
    private final ChatMemoryMapper memoryMapper;

    /**
     * 创建上下文：写操作 → 使用@CacheEvict清理用户列表缓存
     * 清理对应用户的contextList缓存，保证下次查询拿到最新数据
     */
    @Override
    @Transactional
    @CacheEvict(value = "contextList", key = "#request.userId") // 核心修正
    public String createContext(ContextCreateRequest request) {
        String contextId = request.getUserId() + "_conv" + System.currentTimeMillis();

        ChatContext context = new ChatContext();
        context.setConversationId(contextId);
        context.setUserId(request.getUserId());
        context.setTitle(request.getTitle());
        context.setSystemPrompt(request.getSystemPrompt());
        context.setIsPinned(false);
        context.setCreatedAt(LocalDateTime.now());
        context.setUpdatedAt(LocalDateTime.now());

        contextMapper.insert(context);
        return contextId;
    }

    /**
     * 查询用户所有上下文：读操作 → @Cacheable
     * key = 用户ID，sync=true 防止高并发缓存击穿
     */
    @Override
    @Cacheable(value = "contextList", key = "#userId", sync = true) // 修正key+优化
    public List<ContextListVO> listUserContexts(String userId) {
        return contextMapper.selectContextList(userId);
    }

    /**
     * 查询对话历史：读操作 → @Cacheable
     * key = 对话ID，sync=true 高并发保护
     */
    @Override
    @Cacheable(value = "contextHistory", key = "#conversationId", sync = true)
    public List<ChatMessage> getContextHistory(String conversationId) {
        return memoryMapper.selectHistory(conversationId);
    }

    /**
     * 更新上下文：写操作 → @CacheEvict 清理2个缓存
     * 1. 清理用户列表缓存 2. 清理当前对话历史缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = {"contextList", "contextHistory"}, key = "#conversationId") // 核心修正
    public void updateContext(String conversationId, ContextUpdateRequest request) {
        if (request.getTitle() != null) {
            contextMapper.updateTitle(conversationId, request.getTitle());
        }
        if (request.getSystemPrompt() != null) {
            contextMapper.updateSystemPrompt(conversationId, request.getSystemPrompt());
        }
        if (request.getIsPinned() != null) {
            contextMapper.updatePinned(conversationId, request.getIsPinned());
        }
    }

    /**
     * 删除上下文：写操作 → @CacheEvict 清理全部关联缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = {"contextList", "contextHistory"}, key = "#conversationId") // 核心修正
    public void deleteContext(String conversationId) {
        // 先删消息，再删元数据
        memoryMapper.deleteByConversationId(conversationId);
        contextMapper.deleteById(conversationId);
    }

    /**
     * 确保上下文存在：存在则不操作，不存在则插入
     * 插入后清理用户列表缓存
     */
    @Override
    @CacheEvict(value = "contextList", key = "#userId", condition = "#result == null") // 条件缓存
    public void ensureContextExists(String conversationId, String userId) {
        if (contextMapper.selectById(conversationId) != null) return;

        ChatContext context = new ChatContext();
        context.setConversationId(conversationId);
        context.setUserId(userId);
        context.setTitle("新对话");
        context.setIsPinned(false);
        context.setCreatedAt(LocalDateTime.now());
        context.setUpdatedAt(LocalDateTime.now());

        contextMapper.insert(context);
    }
}