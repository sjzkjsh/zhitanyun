package com.atguigu;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import reactor.core.publisher.Hooks;


@EnableFeignClients
@EnableCaching
@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(ChatApplication.class, args);
        
    }
}
