package com.aiwork.helper.service.impl;

import com.aiwork.helper.ai.agent.AgentService;
import com.aiwork.helper.dto.websocket.ChatMessage;
import com.aiwork.helper.entity.ChatLog;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.service.AIService;
import com.aiwork.helper.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stores chat messages and routes AI chat requests to the configured AI service.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatLogRepository chatLogRepository;
    private final ObjectProvider<AgentService> agentServiceProvider;
    private final ObjectProvider<AIService> aiServiceProvider;

    @Override
    public void savePrivateChat(ChatMessage message) {
        log.info("Saving private chat message: from={}, to={}", message.getSendId(), message.getRecvId());
        saveChatLog(message);
    }

    @Override
    public void saveGroupChat(ChatMessage message) {
        log.info("Saving group chat message: from={}, conversationId={}",
                message.getSendId(), message.getConversationId());
        saveChatLog(message);
    }

    @Override
    public String handleAIChat(String userId, String content, String relationId, Long startTime, Long endTime) {
        log.info("Handling AI chat: userId={}, relationId={}, startTime={}, endTime={}",
                userId, relationId, startTime, endTime);

        try {
            AgentService agentService = agentServiceProvider.getIfAvailable();
            if (agentService != null) {
                String aiResponse = agentService.chat(userId, content, relationId, startTime, endTime);

                log.info("AI response succeeded: relationId={}, responseLength={}",
                        relationId, aiResponse != null ? aiResponse.length() : 0);

                return aiResponse;
            }

            AIService aiService = aiServiceProvider.getIfAvailable();
            if (aiService == null) {
                return "AI is disabled. Set FLOWDESK_AI_ENABLED=true and configure FLOWDESK_AI_PROVIDER to enable AI chat.";
            }

            String aiResponse = aiService.chat(userId, content, relationId);
            log.info("AI response succeeded: relationId={}, responseLength={}",
                    relationId, aiResponse != null ? aiResponse.length() : 0);

            return aiResponse;

        } catch (Exception e) {
            log.error("AI chat failed: userId={}, relationId={}", userId, relationId, e);
            return "AI service is temporarily unavailable. Please try again later.";
        }
    }

    @Override
    public List<ChatMessage> getChatHistory(String conversationId, int limit) {
        log.info("Loading chat history: conversationId={}, limit={}", conversationId, limit);

        if (conversationId == null || conversationId.isEmpty()) {
            return new ArrayList<>();
        }

        PageRequest pageRequest = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "sendTime"));

        List<ChatLog> chatLogs = chatLogRepository
                .findByConversationId(conversationId, pageRequest)
                .getContent();

        return chatLogs.stream()
                .map(this::convertToWebSocketMessage)
                .collect(Collectors.toList());
    }

    private void saveChatLog(ChatMessage message) {
        long currentTime = System.currentTimeMillis() / 1000;

        ChatLog chatLog = new ChatLog();
        chatLog.setConversationId(message.getConversationId());
        chatLog.setSendId(message.getSendId());
        chatLog.setRecvId(message.getRecvId());
        chatLog.setChatType(message.getChatType());
        chatLog.setMsgContent(message.getContent());
        chatLog.setSendTime(currentTime);

        try {
            chatLogRepository.save(chatLog);
            log.debug("Saved chat log: id={}", chatLog.getId());
        } catch (Exception e) {
            log.error("Failed to save chat log", e);
        }
    }

    private ChatMessage convertToWebSocketMessage(ChatLog chatLog) {
        return ChatMessage.builder()
                .conversationId(chatLog.getConversationId())
                .sendId(chatLog.getSendId())
                .recvId(chatLog.getRecvId())
                .chatType(chatLog.getChatType())
                .content(chatLog.getMsgContent())
                .contentType(1)
                .build();
    }

    private String generateConversationId(String userId1, String userId2) {
        List<String> userIds = new ArrayList<>();
        userIds.add(userId1);
        userIds.add(userId2);
        userIds.sort(String::compareTo);

        return String.join("_", userIds);
    }
}
