package com.aiwork.helper.ai.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 摘要缓冲区聊天记忆
 * 当对话Token超过限制时，自动调用LLM生成摘要，保留关键信息的同时控制Token消耗
 */
@Slf4j
public class SummaryBufferChatMemory implements ChatMemory {

    /**
     * 摘要生成的提示词模板
     */
    private static final String SUMMARY_PROMPT_TEMPLATE = """
            请根据以下对话内容生成一个简洁的摘要，保留关键信息。

            当前摘要:
            %s

            新的对话内容:
            %s

            请生成新的摘要（使用中文，保持简洁，突出关键信息）:
            """;

    /**
     * 对话历史存储（conversationId -> 消息列表）
     */
    private final ConcurrentHashMap<String, List<Message>> conversationHistory = new ConcurrentHashMap<>();

    /**
     * 摘要缓存（conversationId -> 摘要内容）
     */
    private final ConcurrentHashMap<String, String> summaryBuffer = new ConcurrentHashMap<>();

    /**
     * 最大Token限制（估算：1个中文字符约2个token，1个英文单词约1.5个token）
     * 这里使用字符数作为简单估算
     */
    private final int maxTokenLimit;

    /**
     * 用于生成摘要的ChatModel
     */
    private final ChatModel chatModel;

    /**
     * 构造函数
     *
     * @param chatModel 用于生成摘要的ChatModel
     * @param maxTokenLimit 最大Token限制（字符数估算）
     */
    public SummaryBufferChatMemory(ChatModel chatModel, int maxTokenLimit) {
        this.chatModel = chatModel;
        this.maxTokenLimit = maxTokenLimit;
        log.info("SummaryBufferChatMemory初始化: maxTokenLimit={}", maxTokenLimit);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        log.debug("添加消息到会话: conversationId={}, messageCount={}", conversationId, messages.size());

        List<Message> history = conversationHistory.computeIfAbsent(conversationId, k -> new ArrayList<>());
        history.addAll(messages);

        // 检查是否超过Token限制
        int tokenCount = estimateTokenCount(conversationId);
        log.debug("当前Token估算: conversationId={}, tokenCount={}, maxLimit={}",
                conversationId, tokenCount, maxTokenLimit);

        if (tokenCount > maxTokenLimit) {
            log.info("Token超过限制，开始生成摘要: conversationId={}, tokenCount={}", conversationId, tokenCount);
            generateSummary(conversationId);
        }
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        List<Message> result = new ArrayList<>();

        // 1. 先添加摘要（如果有）
        String summary = summaryBuffer.get(conversationId);
        if (summary != null && !summary.isEmpty()) {
            result.add(new SystemMessage("之前的对话摘要: " + summary));
        }

        // 2. 添加最近的对话历史
        List<Message> history = conversationHistory.get(conversationId);
        if (history != null && !history.isEmpty()) {
            int startIndex = Math.max(0, history.size() - lastN);
            result.addAll(history.subList(startIndex, history.size()));
        }

        log.debug("获取会话记忆: conversationId={}, lastN={}, resultSize={}",
                conversationId, lastN, result.size());

        return result;
    }

    @Override
    public void clear(String conversationId) {
        log.info("清除会话记忆: conversationId={}", conversationId);
        conversationHistory.remove(conversationId);
        summaryBuffer.remove(conversationId);
    }

    /**
     * 估算Token数量（简单估算：字符数 * 系数）
     */
    private int estimateTokenCount(String conversationId) {
        List<Message> history = conversationHistory.get(conversationId);
        if (history == null || history.isEmpty()) {
            return 0;
        }

        int charCount = 0;
        for (Message msg : history) {
            charCount += msg.getText().length();
        }

        // 加上摘要的长度
        String summary = summaryBuffer.get(conversationId);
        if (summary != null) {
            charCount += summary.length();
        }

        // 简单估算：中文1字符约2token，这里用1.5作为平均系数
        return (int) (charCount * 1.5);
    }

    /**
     * 生成对话摘要
     */
    private void generateSummary(String conversationId) {
        try {
            List<Message> history = conversationHistory.get(conversationId);
            if (history == null || history.isEmpty()) {
                return;
            }

            // 构建对话文本
            StringBuilder dialogText = new StringBuilder();
            for (Message msg : history) {
                String role = getMessageRole(msg);
                dialogText.append(role).append(": ").append(msg.getText()).append("\n");
            }

            // 获取当前摘要
            String currentSummary = summaryBuffer.getOrDefault(conversationId, "（无）");

            // 调用LLM生成新摘要
            String prompt = String.format(SUMMARY_PROMPT_TEMPLATE, currentSummary, dialogText.toString());

            // 使用ChatModel直接调用
            Prompt chatPrompt = new Prompt(prompt);
            ChatResponse response = chatModel.call(chatPrompt);
            String newSummary = response.getResult().getOutput().getText();

            if (newSummary != null && !newSummary.isEmpty()) {
                // 保存新摘要
                summaryBuffer.put(conversationId, newSummary);

                // 清空对话历史（摘要已包含关键信息）
                conversationHistory.put(conversationId, new ArrayList<>());

                log.info("摘要生成成功: conversationId={}, summaryLength={}", conversationId, newSummary.length());
                log.debug("新摘要内容: {}", newSummary);
            }

        } catch (Exception e) {
            log.error("生成摘要失败: conversationId={}", conversationId, e);
            // 摘要生成失败时，保留最近的消息，删除较早的消息
            fallbackTruncate(conversationId);
        }
    }

    /**
     * 降级处理：当摘要生成失败时，简单截断旧消息
     */
    private void fallbackTruncate(String conversationId) {
        List<Message> history = conversationHistory.get(conversationId);
        if (history != null && history.size() > 10) {
            // 保留最近10条消息
            List<Message> recentMessages = new ArrayList<>(history.subList(history.size() - 10, history.size()));
            conversationHistory.put(conversationId, recentMessages);
            log.warn("降级处理：截断旧消息，保留最近10条: conversationId={}", conversationId);
        }
    }

    /**
     * 获取消息角色标识
     */
    private String getMessageRole(Message message) {
        if (message instanceof UserMessage) {
            return "用户";
        } else if (message instanceof AssistantMessage) {
            return "AI助手";
        } else if (message instanceof SystemMessage) {
            return "系统";
        }
        return "未知";
    }

    /**
     * 获取会话的摘要内容（用于调试）
     */
    public String getSummary(String conversationId) {
        return summaryBuffer.get(conversationId);
    }

    /**
     * 获取会话的历史消息数量（用于调试）
     */
    public int getHistorySize(String conversationId) {
        List<Message> history = conversationHistory.get(conversationId);
        return history != null ? history.size() : 0;
    }
}
