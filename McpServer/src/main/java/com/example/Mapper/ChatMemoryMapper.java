package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.ChatEntity.ChatMessage;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;


@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMessage> {

    // 查询特定上下文的历史消息
    @Select("""
        SELECT id, conversation_id, message_type, content, metadata_json, created_at 
        FROM AI_CHAT_MEMORY 
        WHERE conversation_id = #{conversationId} 
        ORDER BY created_at ASC
        """)
    List<ChatMessage> selectHistory(@Param("conversationId") String conversationId);

    // 删除特定上下文的所有消息
    @Delete("DELETE FROM AI_CHAT_MEMORY WHERE conversation_id = #{conversationId}")
    int deleteByConversationId(@Param("conversationId") String conversationId);
}
