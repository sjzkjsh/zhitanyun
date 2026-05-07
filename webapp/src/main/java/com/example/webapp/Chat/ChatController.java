package com.example.webapp.Chat;


import com.example.webapp.Entity.Result;
import com.example.webapp.Entity.Vo.MessageVO;
import com.example.webapp.Util.LoginCustomerHolder;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

import org.springframework.http.MediaType;
import reactor.core.publisher.Flux;


import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequestMapping("/api")
@RestController
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private ChatMemory chatMemory;


    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(@RequestParam String message) {
        String username = LoginCustomerHolder.getLoginCustomer().getName();  // 此处仍在线程内，可以获取

        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param("chat_memory_conversation_id", username))
                // 关键：将用户名放入 ToolContext
                .advisors(a -> a.param("tool_context", Map.of("customerName", username)))
                .stream()
                .content();
    }

    @GetMapping("/chat/history")
    public Result<List<MessageVO>> getChatHistory() {
        String conversationId = LoginCustomerHolder.getLoginCustomer().getName();
        // 从 ChatMemory 中获取该会话的所有消息（limit 设大一点）
        List<Message> messages = chatMemory.get(conversationId);

        List<MessageVO> history = messages.stream()
                .map(msg -> {
                    String role = msg.getMessageType().name().toLowerCase();
                    return new MessageVO(role, msg.getText());
                })
                .collect(Collectors.toList());

        return Result.success(history);
    }

}