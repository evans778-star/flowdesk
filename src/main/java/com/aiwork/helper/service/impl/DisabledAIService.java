package com.aiwork.helper.service.impl;

import com.aiwork.helper.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "flowdesk.ai.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAIService implements AIService {

    static final String DISABLED_MESSAGE =
            "AI is disabled. Set FLOWDESK_AI_ENABLED=true and choose FLOWDESK_AI_PROVIDER=ollama or dashscope.";

    @Override
    public String chat(String userId, String message, String conversationId) {
        log.info("AI chat skipped because AI is disabled: userId={}, conversationId={}", userId, conversationId);
        return DISABLED_MESSAGE;
    }

    @Override
    public void clearHistory(String conversationId) {
        log.debug("AI history clear skipped because AI is disabled: conversationId={}", conversationId);
    }

    @Override
    public void addMessageToHistory(String conversationId, String role, String content) {
        log.debug("AI history append skipped because AI is disabled: conversationId={}, role={}", conversationId, role);
    }
}
