-- src/main/resources/schema.sql
-- 只加这一张表，存会话列表和元数据
CREATE TABLE AI_CHAT_CONTEXT (
conversation_id VARCHAR(255) PRIMARY KEY,
user_id VARCHAR(255) NOT NULL,
title VARCHAR(255) DEFAULT '新对话',
system_prompt TEXT,           -- 该上下文专属的系统提示词（可选）
is_pinned BOOLEAN DEFAULT FALSE,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
-- 注意：具体的表结构可能因 Spring AI 版本不同而略有差异
-- 如果不确定，建议先使用方案一禁用初始化，然后查看官方文档对应版本的 DDL
