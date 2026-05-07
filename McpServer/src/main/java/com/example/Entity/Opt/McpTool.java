package com.example.Entity.Opt;

import org.springframework.ai.tool.annotation.Tool;

import java.lang.annotation.*;

/**
 * MCP工具注解，用于描述工具功能
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tool
public @interface McpTool {

    String name();
    String description();
    Parameter[] parameters() default {};
    
    @interface Parameter {
        String name();
        String type();
        String description();
        boolean required() default true;
    }
}
