package com.aiwork.helper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * DashScope配置属性
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dashscope")
public class DashScopeProperties {

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 聊天配置
     */
    private Chat chat = new Chat();

    /**
     * 嵌入配置
     */
    private Embedding embedding = new Embedding();

    @Data
    public static class Chat {
        /**
         * 模型名称
         */
        private String model = "qwen-max";

        /**
         * 温度参数
         */
        private Double temperature = 0.7;
    }

    @Data
    public static class Embedding {
        /**
         * 嵌入模型名称
         */
        private String model = "text-embedding-v3";
    }
}