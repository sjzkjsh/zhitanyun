package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.ChatEntity.ChatContext;
import com.example.Entity.ChatEntity.ChatMessage;
import com.example.Entity.ChatEntity.ContextListVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

// mapper/ChatContextMapper.java
@Mapper
public interface ChatContextMapper extends BaseMapper<ChatContext> {

    // 查询用户的所有会话，包含消息统计
    @Select("""
        SELECT 
            c.conversation_id,
            c.user_id,
            c.title,
            c.system_prompt,
            c.is_pinned,
            c.created_at,
            c.updated_at,
            COUNT(m.id) as messageCount,
            MAX(m.created_at) as lastMessageTime
        FROM AI_CHAT_CONTEXT c
        LEFT JOIN AI_CHAT_MEMORY m ON c.conversation_id = m.conversation_id
        WHERE c.user_id = #{userId}
        GROUP BY c.conversation_id, c.user_id, c.title, c.system_prompt, c.is_pinned, c.created_at, c.updated_at
        ORDER BY c.is_pinned DESC, MAX(m.created_at) DESC
        """)
    List<ContextListVO> selectContextList(@Param("userId") String userId);

    // 更新标题
    @Update("UPDATE AI_CHAT_CONTEXT SET title = #{title}, updated_at = NOW() WHERE conversation_id = #{conversationId}")
    int updateTitle(@Param("conversationId") String conversationId, @Param("title") String title);

    // 更新系统提示词
    @Update("UPDATE AI_CHAT_CONTEXT SET system_prompt = #{systemPrompt}, updated_at = NOW() WHERE conversation_id = #{conversationId}")
    int updateSystemPrompt(@Param("conversationId") String conversationId, @Param("systemPrompt") String systemPrompt);

    // 置顶/取消置顶
    @Update("UPDATE AI_CHAT_CONTEXT SET is_pinned = #{pinned}, updated_at = NOW() WHERE conversation_id = #{conversationId}")
    int updatePinned(@Param("conversationId") String conversationId, @Param("pinned") boolean pinned);
}

