package com.aiwork.helper.service.impl;

import com.aiwork.helper.config.DashScopeProperties;
import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import com.aiwork.helper.dto.websocket.ChatMessage;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.service.AIService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * AI服务实现
 * 使用阿里云DashScope API (通义千问)
 */
@Slf4j
@Service
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class AIServiceImpl implements AIService {

    private final DashScopeProperties dashScopeProperties;
    private final ChatLogRepository chatLogRepository;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    // 会话历史缓存（简单实现，生产环境应使用Redis）
    private final Map<String, ArrayNode> conversationHistory = new ConcurrentHashMap<>();

    @Override
    public String chat(String userId, String message, String conversationId) {
        try {
            log.info("AI聊天请求: userId={}, conversationId={}, message={}",
                    userId, conversationId, message);

            // 构建请求消息列表
            ArrayNode messages = getOrCreateHistory(conversationId);

            // 添加用户消息
            ObjectNode userMessage = objectMapper.createObjectNode();
            userMessage.put("role", "user");
            userMessage.put("content", message);
            messages.add(userMessage);

            // 构建请求体
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", dashScopeProperties.getChat().getModel());
            requestBody.set("messages", messages);
            requestBody.put("temperature", dashScopeProperties.getChat().getTemperature());

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + dashScopeProperties.getApiKey());

            HttpEntity<String> request = new HttpEntity<>(
                    objectMapper.writeValueAsString(requestBody), headers);

            // 调用DashScope API
            String url = dashScopeProperties.getBaseUrl() + "/chat/completions";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            // 解析响应
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            String aiResponse = responseJson
                    .path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText();

            // 将AI响应添加到历史记录
            ObjectNode assistantMessage = objectMapper.createObjectNode();
            assistantMessage.put("role", "assistant");
            assistantMessage.put("content", aiResponse);
            messages.add(assistantMessage);

            // 保持历史记录在合理范围内（最多保留最近10轮对话）
            if (messages.size() > 20) {
                ArrayNode trimmedMessages = objectMapper.createArrayNode();
                for (int i = messages.size() - 20; i < messages.size(); i++) {
                    trimmedMessages.add(messages.get(i));
                }
                conversationHistory.put(conversationId, trimmedMessages);
            }

            log.info("AI响应成功: conversationId={}, response={}", conversationId, aiResponse);
            return aiResponse;

        } catch (Exception e) {
            log.error("AI聊天失败: userId={}, conversationId={}",
                    userId, conversationId, e);
            return "抱歉，AI服务暂时不可用，请稍后再试。";
        }
    }

    @Override
    public void clearHistory(String conversationId) {
        conversationHistory.remove(conversationId);
        log.info("已清除会话历史: conversationId={}", conversationId);
    }

    @Override
    public void addMessageToHistory(String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isEmpty()) {
            log.warn("conversationId为空，无法添加消息到历史");
            return;
        }

        ArrayNode messages = getOrCreateHistory(conversationId);
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);
        message.put("content", content);
        messages.add(message);

        log.debug("已添加消息到会话历史: conversationId={}, role={}, content={}",
                conversationId, role, content);
    }

    /**
     * 获取或创建会话历史
     */
    private ArrayNode getOrCreateHistory(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            return objectMapper.createArrayNode();
        }

        return conversationHistory.computeIfAbsent(conversationId, k -> {
            // 尝试从数据库加载历史记录
            ArrayNode history = objectMapper.createArrayNode();

            try {
                // 获取最近10条聊天记录
                PageRequest pageRequest = PageRequest.of(0, 10,
                        Sort.by(Sort.Direction.DESC, "sendTime"));

                var chatLogs = chatLogRepository
                        .findByConversationId(conversationId, pageRequest)
                        .getContent();

                // 反转顺序（从旧到新）
                for (int i = chatLogs.size() - 1; i >= 0; i--) {
                    var log = chatLogs.get(i);

                    ObjectNode message = objectMapper.createObjectNode();
                    message.put("role", "user");
                    message.put("content", log.getMsgContent());
                    history.add(message);

                    // 注意：这里只添加了用户消息，AI响应需要从其他地方获取
                    // 生产环境应该在ChatLog中区分用户消息和AI响应
                }

                log.debug("从数据库加载了{}条历史记录", chatLogs.size());
            } catch (Exception e) {
                log.warn("加载历史记录失败: conversationId={}", conversationId, e);
            }

            return history;
        });
    }
}
