package com.example.webapp.Config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.example.webapp.Tool.*;
import org.springframework.ai.chat.client.ChatClient;

import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
@Configuration
public class ChatConfig {

    @Autowired
    private McpBailianKnowledgeService mcpBailianKnowledgeService;
    @Autowired
    private BuildingOptimizationMcpTool buildingOptimizationMcpTool;
    @Autowired
    private McpBaikeService mcpBaikeService;
    @Autowired
    private McpSystemService mcpSystemService;
    @Autowired
    private DeviceAnalysisMcpTool deviceAnalysisMcpTool;
    @Autowired
    private  EnergyQueryTool energyQueryTool;
    @Autowired
    private EnergyDiagnosisMcpTool energyDiagnosisMcpTool;

    @Autowired
    private CopTool copTool;
    @Autowired
    private ComprehensiveAnalysisMcpTool comprehensiveAnalysisMcpTool;


    @Autowired
    private JdbcChatMemoryRepository chatMemoryRepository;

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)  // 只保留最近 10 条消息
                .build();
    }


    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        return MessageChatMemoryAdvisor.builder(chatMemory).build();

    }
    @Bean
    public ChatClient qwen(DashScopeChatModel qwen, MessageChatMemoryAdvisor memoryAdvisor) {

        return ChatClient.builder(qwen)
                .defaultAdvisors(memoryAdvisor)
                .defaultSystem("""
        你是一个建筑能源管理专家助手。你必须遵守以下规则：
        
        1. **工具优先原则**：当用户询问任何与能耗、环境、设备状态、天气、知识规范相关的问题时，你必须首先调用相应的工具获取真实数据，严禁凭空编造。
        2. **工具调用后处理**：获取工具返回的数据后，用自然语言向用户解释，并结合知识库给出专业建议。
        """)
                .defaultTools(mcpBailianKnowledgeService,
                        buildingOptimizationMcpTool,
                        mcpBaikeService, mcpSystemService,
                        deviceAnalysisMcpTool, energyQueryTool,
                        energyDiagnosisMcpTool, copTool, comprehensiveAnalysisMcpTool)
                .build();
    }
}
