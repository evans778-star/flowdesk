package com.aiwork.helper.controller;

import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import com.aiwork.helper.ai.memory.MultiSessionChatMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 会话记忆调试接口
 * 用于验证和调试SummaryBufferChatMemory功能
 */
@Slf4j
@Profile("dev")
@RestController
@RequestMapping("/api/debug/memory")
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class MemoryDebugController {

    private final ChatMemory chatMemory;

    /**
     * 获取指定会话的记忆调试信息
     *
     * @param conversationId 会话ID（通常是用户ID）
     * @return 调试信息
     */
    @GetMapping("/session/{conversationId}")
    public Map<String, Object> getSessionInfo(@PathVariable String conversationId) {
        log.info("获取会话调试信息: conversationId={}", conversationId);

        if (chatMemory instanceof MultiSessionChatMemory multiMemory) {
            return multiMemory.getDebugInfo(conversationId);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("error", "ChatMemory不是MultiSessionChatMemory类型");
        return result;
    }

    /**
     * 获取所有会话的调试信息
     *
     * @return 所有会话的调试信息列表
     */
    @GetMapping("/sessions")
    public Map<String, Object> getAllSessions() {
        log.info("获取所有会话调试信息");

        Map<String, Object> result = new HashMap<>();

        if (chatMemory instanceof MultiSessionChatMemory multiMemory) {
            result.put("sessionCount", multiMemory.getSessionCount());
            result.put("sessions", multiMemory.getAllDebugInfo());
        } else {
            result.put("error", "ChatMemory不是MultiSessionChatMemory类型");
        }

        return result;
    }

    /**
     * 清除指定会话的记忆
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/session/{conversationId}")
    public Map<String, Object> clearSession(@PathVariable String conversationId) {
        log.info("清除会话记忆: conversationId={}", conversationId);

        chatMemory.clear(conversationId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "会话记忆已清除: " + conversationId);
        return result;
    }

    /**
     * 清除所有会话的记忆
     *
     * @return 操作结果
     */
    @DeleteMapping("/sessions")
    public Map<String, Object> clearAllSessions() {
        log.info("清除所有会话记忆");

        if (chatMemory instanceof MultiSessionChatMemory multiMemory) {
            multiMemory.clearAll();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "所有会话记忆已清除");
        return result;
    }
}
