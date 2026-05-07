package com.example.McpServices;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class TestTool {
    @Tool(name = "test_tool", description = "测试工具")
    public String testTool() {
        System.out.println("test_tool被调用");
        return "测试成功";
    }
}
