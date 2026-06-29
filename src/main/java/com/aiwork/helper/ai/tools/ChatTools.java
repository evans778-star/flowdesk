package com.aiwork.helper.ai.tools;

import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import com.aiwork.helper.entity.ChatLog;
import com.aiwork.helper.entity.User;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聊天记录工具
 * 提供获取和总结聊天记录的功能
 */
@Slf4j
@Component
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class ChatTools {

    private final ChatLogRepository chatLogRepository;
    private final UserRepository userRepository;

    /**
     * 当前会话的上下文参数（通过ThreadLocal传递）
     */
    private static final ThreadLocal<ChatContext> chatContext = new ThreadLocal<>();

    /**
     * 聊天上下文
     */
    public static class ChatContext {
        private String relationId;
        private Long startTime;
        private Long endTime;

        public String getRelationId() {
            return relationId;
        }

        public void setRelationId(String relationId) {
            this.relationId = relationId;
        }

        public Long getStartTime() {
            return startTime;
        }

        public void setStartTime(Long startTime) {
            this.startTime = startTime;
        }

        public Long getEndTime() {
            return endTime;
        }

        public void setEndTime(Long endTime) {
            this.endTime = endTime;
        }
    }

    /**
     * 设置聊天上下文
     */
    public static void setChatContext(String relationId, Long startTime, Long endTime) {
        ChatContext ctx = new ChatContext();
        ctx.setRelationId(relationId);
        ctx.setStartTime(startTime);
        ctx.setEndTime(endTime);
        chatContext.set(ctx);
    }

    /**
     * 获取聊天上下文
     */
    public static ChatContext getChatContext() {
        return chatContext.get();
    }

    /**
     * 清除聊天上下文
     */
    public static void clearChatContext() {
        chatContext.remove();
    }

    @Tool(description = "获取群聊或私聊的聊天记录。当用户要求总结群聊消息、查看聊天记录时使用。会自动获取当前会话的聊天记录。")
    public String getChatLogs(
            @ToolParam(description = "要获取的聊天记录条数，默认50条", required = false) Integer limit
    ) {
        log.info("Tool调用 - getChatLogs: limit={}", limit);

        try {
            ChatContext ctx = getChatContext();
            if (ctx == null || ctx.getRelationId() == null || ctx.getRelationId().isEmpty()) {
                return "无法获取聊天记录：未找到当前会话ID。请确认您在正确的群聊或私聊窗口中。";
            }

            String conversationId = ctx.getRelationId();
            Long startTime = ctx.getStartTime();
            Long endTime = ctx.getEndTime();

            // 如果没有指定时间范围，默认最近24小时
            if ((startTime == null || startTime == 0) && (endTime == null || endTime == 0)) {
                long currentTime = System.currentTimeMillis() / 1000;
                startTime = currentTime - 24 * 3600;
                endTime = currentTime;
            }

            int recordLimit = (limit != null && limit > 0) ? limit : 50;

            log.info("查询聊天记录: conversationId={}, startTime={}, endTime={}, limit={}",
                    conversationId, startTime, endTime, recordLimit);

            // 查询聊天记录
            PageRequest pageRequest = PageRequest.of(0, recordLimit,
                    Sort.by(Sort.Direction.ASC, "sendTime"));

            Page<ChatLog> chatLogs;
            if (startTime != null && endTime != null) {
                chatLogs = chatLogRepository.findByConversationIdAndTimeRange(
                        conversationId, startTime, endTime, pageRequest);
            } else {
                chatLogs = chatLogRepository.findByConversationId(conversationId, pageRequest);
            }

            List<ChatLog> logs = chatLogs.getContent();

            if (logs.isEmpty()) {
                return "当前会话没有聊天记录。";
            }

            // 缓存用户信息，避免重复查询
            Map<String, User> userCache = new HashMap<>();

            // 格式化聊天记录
            StringBuilder result = new StringBuilder();
            result.append("聊天记录（共").append(logs.size()).append("条）:\n\n");

            for (ChatLog chatLog : logs) {
                String senderId = chatLog.getSendId();
                User user = userCache.get(senderId);

                if (user == null) {
                    user = userRepository.findById(senderId).orElse(null);
                    if (user != null) {
                        userCache.put(senderId, user);
                    }
                }

                String userName = (user != null) ? user.getName() : "未知用户";
                result.append(String.format("%s: %s\n", userName, chatLog.getMsgContent()));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("获取聊天记录失败", e);
            return "获取聊天记录失败: " + e.getMessage();
        }
    }

    @Tool(description = "总结群聊消息内容。当用户要求总结群聊、归纳聊天内容时使用。会自动获取聊天记录并进行总结分析。")
    public String summarizeChatLogs() {
        log.info("Tool调用 - summarizeChatLogs");

        // 先获取聊天记录
        String chatLogs = getChatLogs(100);

        if (chatLogs.startsWith("无法获取") || chatLogs.startsWith("当前会话没有") || chatLogs.startsWith("获取聊天记录失败")) {
            return chatLogs;
        }

        // 返回聊天记录，让LLM自己总结
        return "以下是需要总结的聊天记录:\n\n" + chatLogs + "\n\n请根据以上聊天记录进行总结，提取出重要事项（如待办任务、审批事项等）。";
    }
}
