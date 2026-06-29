package com.aiwork.helper.service.impl;

import com.aiwork.helper.ai.ollama.OllamaClient;
import com.aiwork.helper.ai.ollama.OllamaUnavailableException;
import com.aiwork.helper.config.OllamaAiEnabledCondition;
import com.aiwork.helper.service.AIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Conditional(OllamaAiEnabledCondition.class)
public class OllamaAIService implements AIService {

    private final OllamaClient ollamaClient;
    private final Map<String, List<OllamaClient.Message>> conversationHistory = new ConcurrentHashMap<>();

    @Override
    public String chat(String userId, String message, String conversationId) {
        try {
            List<OllamaClient.Message> messages = getOrCreateHistory(conversationId);
            messages.add(new OllamaClient.Message("user", message));
            String response = ollamaClient.chat(messages);
            messages.add(new OllamaClient.Message("assistant", response));
            trimHistory(conversationId, messages);
            return response;
        } catch (OllamaUnavailableException e) {
            log.warn("Ollama chat unavailable: userId={}, conversationId={}, error={}",
                    userId, conversationId, e.getMessage());
            return e.getMessage();
        } catch (Exception e) {
            log.error("Ollama chat failed: userId={}, conversationId={}", userId, conversationId, e);
            return "Ollama request failed. Check the local Ollama runtime and configured model.";
        }
    }

    @Override
    public void clearHistory(String conversationId) {
        conversationHistory.remove(conversationId);
    }

    @Override
    public void addMessageToHistory(String conversationId, String role, String content) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        getOrCreateHistory(conversationId).add(new OllamaClient.Message(role, content));
    }

    private List<OllamaClient.Message> getOrCreateHistory(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return new java.util.ArrayList<>();
        }
        return conversationHistory.computeIfAbsent(conversationId, key -> new java.util.ArrayList<>());
    }

    private void trimHistory(String conversationId, List<OllamaClient.Message> messages) {
        if (conversationId == null || conversationId.isBlank() || messages.size() <= 20) {
            return;
        }
        conversationHistory.put(conversationId, new java.util.ArrayList<>(messages.subList(messages.size() - 20, messages.size())));
    }
}
