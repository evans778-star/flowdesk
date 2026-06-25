package com.aiwork.helper.service;

/**
 * AI服务接口
 * 负责与阿里云DashScope (通义千问) 交互
 */
public interface AIService {

    /**
     * 发送消息给AI并获取响应
     *
     * @param userId 用户ID（用于会话隔离）
     * @param message 用户消息
     * @param conversationId 会话ID（用于保持对话上下文）
     * @return AI响应内容
     */
    String chat(String userId, String message, String conversationId);

    /**
     * 清除指定会话的历史记录
     *
     * @param conversationId 会话ID
     */
    void clearHistory(String conversationId);

    /**
     * 将消息添加到会话历史记录中（用于文件上传等场景）
     *
     * @param conversationId 会话ID
     * @param role 角色（user/assistant）
     * @param content 消息内容
     */
    void addMessageToHistory(String conversationId, String role, String content);
}