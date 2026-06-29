package com.aiwork.helper.service;

import com.aiwork.helper.dto.websocket.ChatMessage;

import java.util.List;

/**
 * 聊天服务接口
 */
public interface ChatService {

    /**
     * 处理私聊消息
     */
    void savePrivateChat(ChatMessage message);

    /**
     * 处理群聊消息
     */
    void saveGroupChat(ChatMessage message);

    /**
     * 处理AI聊天请求
     * @param userId 用户ID
     * @param content 用户输入内容
     * @param relationId 关联ID（群聊ID等）
     * @param startTime 开始时间（Unix时间戳）
     * @param endTime 结束时间（Unix时间戳）
     * @return AI响应内容
     */
    String handleAIChat(String userId, String content, String relationId, Long startTime, Long endTime);

    /**
     * 获取聊天历史
     * @param conversationId 会话ID
     * @param limit 限制数量
     * @return 聊天消息列表
     */
    List<ChatMessage> getChatHistory(String conversationId, int limit);
}
