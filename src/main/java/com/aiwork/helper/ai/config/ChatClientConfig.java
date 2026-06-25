package com.aiwork.helper.ai.config;

import com.aiwork.helper.ai.tools.*;
import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * ChatClient配置类
 * 配置Spring AI的ChatClient并注册所有Tool工具和记忆Advisor
 */
@Slf4j
@Configuration
@Conditional(DashScopeAiEnabledCondition.class)
public class ChatClientConfig {

    /**
     * 配置主ChatClient，注册所有Tool工具和MessageChatMemoryAdvisor
     *
     * @param chatModel Spring AI自动配置的ChatModel
     * @param todoTools 待办工具集
     * @param approvalTools 审批工具集
     * @param knowledgeTools 知识库工具
     * @param timeParserTool 时间解析工具
     * @param userQueryTool 用户查询工具
     * @param chatTools 聊天记录工具
     * @param fileTools 文件记忆工具
     * @param messageChatMemoryAdvisor 会话记忆Advisor
     * @return 配置好的ChatClient
     */
    @Bean
    @Primary
    public ChatClient chatClient(
            ChatModel chatModel,
            TodoTools todoTools,
            ApprovalTools approvalTools,
            KnowledgeTools knowledgeTools,
            TimeParserTool timeParserTool,
            UserQueryTool userQueryTool,
            ChatTools chatTools,
            FileTools fileTools,
            MessageChatMemoryAdvisor messageChatMemoryAdvisor
    ) {
        log.info("开始配置ChatClient，注册Tool工具和Memory Advisor...");

        ChatClient client = ChatClient.builder(chatModel)
                .defaultTools(
                        todoTools,
                        approvalTools,
                        knowledgeTools,
                        timeParserTool,
                        userQueryTool,
                        chatTools,
                        fileTools
                )
                .defaultAdvisors(messageChatMemoryAdvisor)
                .build();

        log.info("ChatClient配置完成");
        log.info("  - 已注册的Tools: TodoTools, ApprovalTools, KnowledgeTools, TimeParserTool, UserQueryTool, ChatTools, FileTools");
        log.info("  - 已注册的Advisors: MessageChatMemoryAdvisor (SummaryBuffer)");

        return client;
    }
}
