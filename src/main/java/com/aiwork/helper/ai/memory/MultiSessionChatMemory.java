package com.aiwork.helper.ai.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.memory.ChatMemory;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * 多会话聊天记忆管理器
 * 为不同的用户/会话维护独立的ChatMemory实例
 */
@Slf4j
public class MultiSessionChatMemory implements ChatMemory {

    /**
     * 会话ID到ChatMemory实例的映射
     */
    private final ConcurrentHashMap<String, ChatMemory> memories = new ConcurrentHashMap<>();

    /**
     * ChatMemory创建函数
     */
    private final Supplier<ChatMemory> memoryFactory;

    /**
     * 默认ChatMemory实例（用于没有指定会话ID的情况）
     */
    private final ChatMemory defaultMemory;

    /**
     * 构造函数
     *
     * @param memoryFactory ChatMemory创建函数
     */
    public MultiSessionChatMemory(Supplier<ChatMemory> memoryFactory) {
        this.memoryFactory = memoryFactory;
        this.defaultMemory = memoryFactory.get();
        log.info("MultiSessionChatMemory初始化完成");
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        ChatMemory memory = getOrCreateMemory(conversationId);
        memory.add(conversationId, messages);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        ChatMemory memory = getOrCreateMemory(conversationId);
        return memory.get(conversationId, lastN);
    }

    @Override
    public void clear(String conversationId) {
        ChatMemory memory = memories.get(conversationId);
        if (memory != null) {
            memory.clear(conversationId);
            memories.remove(conversationId);
            log.info("会话记忆已清除: conversationId={}", conversationId);
        }
    }

    /**
     * 获取或创建指定会话的ChatMemory实例
     */
    private ChatMemory getOrCreateMemory(String conversationId) {
        if (conversationId == null || conversationId.isEmpty()) {
            return defaultMemory;
        }

        return memories.computeIfAbsent(conversationId, k -> {
            log.info("为会话创建新的ChatMemory: conversationId={}", conversationId);
            return memoryFactory.get();
        });
    }

    /**
     * 获取当前管理的会话数量
     */
    public int getSessionCount() {
        return memories.size();
    }

    /**
     * 清除所有会话记忆
     */
    public void clearAll() {
        for (String conversationId : memories.keySet()) {
            clear(conversationId);
        }
        defaultMemory.clear("default");
        log.info("所有会话记忆已清除");
    }

    /**
     * 获取指定会话的调试信息
     *
     * @param conversationId 会话ID
     * @return 调试信息Map
     */
    public java.util.Map<String, Object> getDebugInfo(String conversationId) {
        java.util.Map<String, Object> info = new java.util.HashMap<>();
        info.put("conversationId", conversationId);
        info.put("exists", memories.containsKey(conversationId));

        ChatMemory memory = memories.get(conversationId);
        if (memory instanceof SummaryBufferChatMemory summaryMemory) {
            info.put("summary", summaryMemory.getSummary(conversationId));
            info.put("historySize", summaryMemory.getHistorySize(conversationId));
        }

        return info;
    }

    /**
     * 获取所有会话的调试信息
     *
     * @return 所有会话的调试信息列表
     */
    public java.util.List<java.util.Map<String, Object>> getAllDebugInfo() {
        java.util.List<java.util.Map<String, Object>> list = new java.util.ArrayList<>();
        for (String conversationId : memories.keySet()) {
            list.add(getDebugInfo(conversationId));
        }
        return list;
    }
}
