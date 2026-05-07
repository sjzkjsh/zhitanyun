package org.webSocketDemo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
private final org.webSocketDemo.config.ChatWebSocketHandler chatWebSocketHandler;
public WebSocketConfig(org.webSocketDemo.config.ChatWebSocketHandler chatWebSocketHandler) {
    this.chatWebSocketHandler = chatWebSocketHandler;
}
@Override
public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(chatWebSocketHandler, "/ws/chat/{senderType}/{senderId}")
            // 重点：允许 9090(App) 和 8080(Web) 跨域连接
            .setAllowedOrigins("http://localhost:9090", "http://localhost:8080", "*");
    }
}