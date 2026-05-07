package com.atguigu.RagFlowService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class RagflowService {

    private final RestClient restClient;
    private final String chatId;
    private final String model;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RagflowService(
            @Value("${ragflow.base-url}") String baseUrl,
            @Value("${ragflow.api-key}") String apiKey,
            @Value("${ragflow.chat-id}") String chatId,
            @Value("${ragflow.model}") String model) {
        this.chatId = chatId;
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public String chat(String userMessage) {
        // 构建请求体，确保 stream=false 以获得完整 JSON
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(Map.of("role", "user", "content", userMessage)),
                "stream", false
        );

        // 发送请求，以 String 接收原始响应
        String responseBody = restClient.post()
                .uri("/api/v1/chats_openai/{chat_id}/chat/completions", chatId)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        // 解析 JSON，提取回答内容
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            // 根据你提供的 JSON 结构，choices 是一个数组，取第一个元素
            JsonNode firstChoice = root.path("choices").get(0);
            // 提取 message.content（注意你的 JSON 中 message 对象在 choices[0] 内）
            String content = firstChoice.path("message").path("content").asText();
            return content;  // 此时 content 已经是解码后的中文字符串
        } catch (Exception e) {
            throw new RuntimeException("解析 RAGFlow 响应失败: " + responseBody, e);
        }


    }
    // 新增检索方法
    public String searchKnowledgeBase(String query) {
        Map<String, Object> requestBody = Map.of(
                "question", query,
                "knowledge_base_id", "your_kb_id",
                "top_k", 5
        );
        String responseBody = restClient.post()
                .uri("/api/v1/retrieval")
                .body(requestBody)
                .retrieve()
                .body(String.class);
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode chunks = root.path("data").path("chunks");
            StringBuilder result = new StringBuilder();
            for (JsonNode chunk : chunks) {
                result.append(chunk.path("content").asText()).append("\n---\n");
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException("检索失败", e);
        }
    }
}