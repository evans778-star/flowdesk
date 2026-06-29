package com.aiwork.helper.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI配置类
 * 配置Spring AI Alibaba相关Bean
 */
@Slf4j
@Configuration
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class AIConfig {

    private final DashScopeProperties dashScopeProperties;

    /**
     * 配置说明：
     * Spring AI Alibaba会自动根据配置文件中的dashscope配置创建ChatClient
     *
     * 主要配置项：
     * - dashscope.api-key: DashScope API密钥
     * - dashscope.base-url: API基础URL
     * - dashscope.chat.model: 使用的模型（qwen-max, qwen-plus等）
     * - dashscope.chat.temperature: 温度参数，控制回复的随机性
     *
     * 使用方式：
     * @Autowired
     * private ChatClient chatClient;
     *
     * String response = chatClient.call("你好");
     */

    @Bean
    public String dashScopeInfo() {
        log.info("DashScope配置已加载:");
        String apiKey = dashScopeProperties.getApiKey();
        String maskedApiKey = apiKey != null && apiKey.length() >= 10
            ? apiKey.substring(0, 10) + "****"
            : "未配置";
        log.info("  - API Key: {}", maskedApiKey);
        log.info("  - Base URL: {}", dashScopeProperties.getBaseUrl());
        log.info("  - Model: {}", dashScopeProperties.getChat().getModel());
        log.info("  - Temperature: {}", dashScopeProperties.getChat().getTemperature());
        return "DashScope配置完成";
    }
}
