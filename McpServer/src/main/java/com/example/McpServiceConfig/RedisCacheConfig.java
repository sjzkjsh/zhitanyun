package com.example.McpServiceConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        return builder -> {
            // 创建 ObjectMapper 并启用默认类型记录
            ObjectMapper mapper = new ObjectMapper();
            mapper.activateDefaultTyping(
                    mapper.getPolymorphicTypeValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL
            );

            // 使用带类型信息的序列化器
            GenericJackson2JsonRedisSerializer serializer =
                    new GenericJackson2JsonRedisSerializer(mapper);

            RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                    .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(serializer)
                    );

            builder.cacheDefaults(config);
        };
    }
    @Bean
    @Primary
    public ObjectMapper redisObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    @Primary   // 使这个 Bean 成为首选，覆盖自动配置的 CacheManager
    public CacheManager redisCacheManager(RedisConnectionFactory factory, ObjectMapper redisObjectMapper) {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(redisObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> configMap = new HashMap<>();
        configMap.put("mcpEnergy", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("buildings", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("devices", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("energyTrend", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("energyStats", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("contextHistory",defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("contextList",defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configMap.put("abnormalScanCache",defaultConfig.entryTtl(Duration.ofMinutes(10)));
        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig.entryTtl(Duration.ofMinutes(10)))
                .withInitialCacheConfigurations(configMap)
                .transactionAware()
                .build();
    }
}