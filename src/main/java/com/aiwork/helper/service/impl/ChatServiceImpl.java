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
 * 鑱婂ぉ鏈嶅姟瀹炵幇
 * 浣跨敤AgentService澶勭悊AI鑱婂ぉ璇锋眰锛堝熀浜嶴pring AI Function Calling锛?
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
        log.info("淇濆瓨绉佽亰娑堟伅: from={}, to={}", message.getSendId(), message.getRecvId());
        saveChatLog(message);
    }

    @Override
    public void saveGroupChat(ChatMessage message) {
        log.info("淇濆瓨缇よ亰娑堟伅: from={}, conversationId={}",
                message.getSendId(), message.getConversationId());
        saveChatLog(message);
    }

    @Override
    public String handleAIChat(String userId, String content, String relationId, Long startTime, Long endTime) {
        log.info("澶勭悊AI鑱婂ぉ: userId={}, content={}, relationId={}, startTime={}, endTime={}",
                userId, content, relationId, startTime, endTime);

        try {
            // 浣跨敤AgentService澶勭悊AI鑱婂ぉ (Spring AI Function Calling)
            // Agent宸ヤ綔娴佺▼:
            // 1. 鍒嗘瀽鐢ㄦ埛杈撳叆鎰忓浘
            // 2. 鑷姩閫夋嫨鍚堥€傜殑Tool璋冪敤
            // 3. 鎵цTool骞惰幏鍙栫粨鏋?
            // 4. 缁х画鎺ㄧ悊锛屽彲鑳借皟鐢ㄦ洿澶歍ool
            // 5. 鐢熸垚鏈€缁堝洖澶?
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
            log.error("AI鑱婂ぉ澶勭悊澶辫触: userId={}, relationId={}",
                    userId, relationId, e);
            return "AI service is temporarily unavailable. Please try again later.";
        }
    }

    @Override
    public List<ChatMessage> getChatHistory(String conversationId, int limit) {
        log.info("鑾峰彇鑱婂ぉ鍘嗗彶: conversationId={}, limit={}", conversationId, limit);

        if (conversationId == null || conversationId.isEmpty()) {
            return new ArrayList<>();
        }

        // 鏌ヨ鑱婂ぉ璁板綍锛屾寜鍙戦€佹椂闂村€掑簭
        PageRequest pageRequest = PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "sendTime"));

        List<ChatLog> chatLogs = chatLogRepository
                .findByConversationId(conversationId, pageRequest)
                .getContent();

        // 杞崲涓篧ebSocketMessage
        return chatLogs.stream()
                .map(this::convertToWebSocketMessage)
                .collect(Collectors.toList());
    }

    /**
     * 淇濆瓨鑱婂ぉ娑堟伅鍒版暟鎹簱
     */
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
            log.debug("鑱婂ぉ娑堟伅宸蹭繚瀛? id={}", chatLog.getId());
        } catch (Exception e) {
            log.error("淇濆瓨鑱婂ぉ娑堟伅澶辫触", e);
        }
    }

    /**
     * 灏咰hatLog杞崲涓篊hatMessage
     */
    private ChatMessage convertToWebSocketMessage(ChatLog chatLog) {
        return ChatMessage.builder()
                .conversationId(chatLog.getConversationId())
                .sendId(chatLog.getSendId())
                .recvId(chatLog.getRecvId())
                .chatType(chatLog.getChatType())
                .content(chatLog.getMsgContent())
                .contentType(1) // 榛樿涓烘枃瀛楃被鍨?
                .build();
    }

    /**
     * 鐢熸垚浼氳瘽ID锛堢鑱婂満鏅級
     * 鏍规嵁涓や釜鐢ㄦ埛ID鐢熸垚鍞竴鐨勪細璇滻D
     */
    private String generateConversationId(String userId1, String userId2) {
        // 瀵圭敤鎴稩D鎺掑簭锛岀‘淇濈浉鍚岀殑涓や釜鐢ㄦ埛鐢熸垚鐩稿悓鐨勪細璇滻D
        List<String> userIds = new ArrayList<>();
        userIds.add(userId1);
        userIds.add(userId2);
        userIds.sort(String::compareTo);

        return String.join("_", userIds);
    }
}
