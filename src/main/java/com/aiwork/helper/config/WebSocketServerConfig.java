package com.aiwork.helper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * WebSocket服务器配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "websocket.server")
public class WebSocketServerConfig {

    /**
     * WebSocket服务器端口
     */
    private int port = 9000;

    /**
     * WebSocket路径
     */
    private String path = "/ws";
}
