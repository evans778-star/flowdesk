package com.aiwork.helper.ai.config;

import com.aiwork.helper.ai.memory.MultiSessionChatMemory;
import com.aiwork.helper.ai.memory.SummaryBufferChatMemory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * ChatMemory配置类
 * 配置会话摘要缓冲记忆机制
 */
@Slf4j
@Configuration
@Conditional(DashScopeAiEnabledCondition.class)
public class ChatMemoryConfig {

    /**
     * 最大Token限制（字符数估算）
     * 默认2000，约等于3000 tokens
     */
    @Value("${ai.memory.max-token-limit:2000}")
    private int maxTokenLimit;

    /**
     * 创建多会话ChatMemory
     * 每个用户会话有独立的SummaryBufferChatMemory实例
     * 直接使用ChatModel来避免循环依赖
     */
    @Bean
    public ChatMemory chatMemory(ChatModel chatModel) {
        log.info("创建MultiSessionChatMemory，maxTokenLimit={}", maxTokenLimit);

        return new MultiSessionChatMemory(() ->
                new SummaryBufferChatMemory(chatModel, maxTokenLimit)
        );
    }

    /**
     * 创建MessageChatMemoryAdvisor
     * 用于将历史对话注入到ChatClient请求中
     */
    @Bean
    public MessageChatMemoryAdvisor messageChatMemoryAdvisor(ChatMemory chatMemory) {
        log.info("创建MessageChatMemoryAdvisor");
        return MessageChatMemoryAdvisor.builder(chatMemory)
                .build();
    }
}
