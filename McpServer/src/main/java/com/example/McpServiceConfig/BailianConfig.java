package com.example.McpServiceConfig;

import com.aliyun.bailian20231229.Client;
import com.aliyun.teaopenapi.models.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BailianConfig {

    // 必须匹配你的 YAML 结构：alibaba.cloud.xxx
    @Value("${alibaba.cloud.access-key-id}")
    private String accessKeyId;

    @Value("${alibaba.cloud.access-key-secret}")
    private String accessKeySecret;

    @Value("${alibaba.cloud.bailian.endpoint}")
    private String endpoint;

    @Bean
    public Client bailianClient() throws Exception {
        Config config = new Config();
        config.setAccessKeyId(accessKeyId);
        config.setAccessKeySecret(accessKeySecret);
        config.setEndpoint(endpoint);
        return new Client(config);
    }
}