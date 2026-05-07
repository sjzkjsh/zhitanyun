package com.atguigu.Config;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;

import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
public class MultiModelConfig {

    @Autowired
    private JdbcChatMemoryRepository chatMemoryRepository;

    @Autowired
    private ToolCallbackProvider localToolsProvider;


    @Value("classpath:system-prompt.md")
    private Resource systemPromptResource;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(20)
                .build();
    }





    @Bean
    public ChatClient qwenChatClient(OpenAiChatModel qwenModel) throws IOException {
        for (ToolCallback toolCallback : Arrays.stream(localToolsProvider.getToolCallbacks()).toList()) {
            System.out.println(toolCallback.toString());
        }
        ToolCallback[] callbacks = localToolsProvider.getToolCallbacks();
        for (ToolCallback cb : callbacks) {
            ToolDefinition def = cb.getToolDefinition();
            System.out.println("Tool name: '" + def.name() + "', description: " + def.description());
            if (def.name() == null || def.name().isEmpty()) {
                System.err.println("ERROR: Tool name is null/empty!");
            }
        }

        // 读取系统提示词（包含 <thinking> 和 <answer> 格式要求）
        String systemPrompt = new String(
                systemPromptResource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        return ChatClient.builder(qwenModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(chatMemory()).build()
                )
                .defaultToolCallbacks(localToolsProvider.getToolCallbacks())
                .build();
    }



}