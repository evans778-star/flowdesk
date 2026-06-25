package com.aiwork.helper.ai.tools;

import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import com.aiwork.helper.dto.request.TodoListRequest;
import com.aiwork.helper.dto.request.TodoRequest;
import com.aiwork.helper.dto.response.TodoListResponse;
import com.aiwork.helper.entity.enums.TodoStatus;
import com.aiwork.helper.service.TodoService;
import com.aiwork.helper.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 待办工具集
 * 提供待办事项的创建、查询等功能
 */
@Slf4j
@Component
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class TodoTools {

    private final TodoService todoService;
    private final UserService userService;

    /**
     * 当前操作的用户ID（通过ThreadLocal传递）
     */
    private static final ThreadLocal<String> currentUserId = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     */
    public static void setCurrentUserId(String userId) {
        currentUserId.set(userId);
    }

    /**
     * 获取当前用户ID
     */
    public static String getCurrentUserId() {
        return currentUserId.get();
    }

    /**
     * 清除当前用户ID
     */
    public static void clearCurrentUserId() {
        currentUserId.remove();
    }

    @Tool(description = "创建待办事项。当用户想要创建、添加新的待办任务时使用。必须提供desc描述参数，如果用户没有明确说明描述，请根据标题自动生成合适的描述。")
    public String createTodo(
            @ToolParam(description = "待办标题，必填") String title,
            @ToolParam(description = "待办描述，必填，如果用户没有明确提供，请根据标题生成合适的描述") String desc,
            @ToolParam(description = "截止时间，Unix时间戳(秒)，必填") Long deadlineAt,
            @ToolParam(description = "执行人用户名列表，可选，如果为空则默认为当前用户", required = false) List<String> executorNames
    ) {
        log.info("Tool调用 - createTodo: title={}, desc={}, deadlineAt={}, executorNames={}",
                title, desc, deadlineAt, executorNames);

        try {
            TodoRequest request = new TodoRequest();
            String finalTitle = title != null ? title : "未命名待办";
            request.setTitle(finalTitle);

            // 如果描述为空，根据标题自动生成描述
            String finalDesc = desc;
            if (finalDesc == null || finalDesc.trim().isEmpty()) {
                finalDesc = "待办事项: " + finalTitle;
            }
            request.setDesc(finalDesc);

            // 处理截止时间
            if (deadlineAt == null || deadlineAt == 0) {
                // 默认设置为明天
                deadlineAt = System.currentTimeMillis() / 1000 + 86400;
            }
            request.setDeadlineAt(deadlineAt);

            // 处理执行人ID列表
            List<String> executeIds = new ArrayList<>();
            if (executorNames != null && !executorNames.isEmpty()) {
                for (String name : executorNames) {
                    // 判断是用户名还是用户ID
                    if (name.matches(".*[\\u4e00-\\u9fa5]+.*") || name.length() != 24) {
                        // 是用户名，需要转换为ID
                        String userId = userService.getUserIdByName(name);
                        if (userId != null) {
                            executeIds.add(userId);
                            log.info("用户名 '{}' 转换为ID: {}", name, userId);
                        } else {
                            log.warn("找不到用户名 '{}' 对应的ID，跳过", name);
                        }
                    } else {
                        // 已经是ID，直接使用
                        executeIds.add(name);
                    }
                }
            }

            // 如果没有指定执行人，使用当前用户
            if (executeIds.isEmpty()) {
                String userId = getCurrentUserId();
                if (userId != null) {
                    executeIds.add(userId);
                }
            }

            request.setExecuteIds(executeIds);
            request.setStatus(TodoStatus.PENDING.getValue());

            String todoId = todoService.create(request);

            String deadlineStr = formatTimestamp(deadlineAt);
            return String.format("待办创建成功!\n标题: %s\n截止时间: %s\n待办ID: %s",
                    request.getTitle(), deadlineStr, todoId);

        } catch (Exception e) {
            log.error("创建待办失败", e);
            return "创建待办失败: " + e.getMessage();
        }
    }

    @Tool(description = "查询待办列表。当用户想要查看、搜索待办事项时使用。")
    public String findTodos(
            @ToolParam(description = "开始时间，Unix时间戳(秒)，可选，不填则不限制开始时间", required = false) Long startTime,
            @ToolParam(description = "结束时间，Unix时间戳(秒)，可选，不填则不限制结束时间", required = false) Long endTime
    ) {
        log.info("Tool调用 - findTodos: startTime={}, endTime={}", startTime, endTime);

        try {
            // 构建查询请求
            TodoListRequest listRequest = new TodoListRequest();
            listRequest.setStartTime(startTime != null && startTime > 0 ? startTime : null);
            listRequest.setEndTime(endTime != null && endTime > 0 ? endTime : null);
            listRequest.setPage(1);
            listRequest.setCount(10);

            // 调用Service查询待办列表
            TodoListResponse listResponse = todoService.list(listRequest);

            if (listResponse.getData() == null || listResponse.getData().isEmpty()) {
                return "您当前没有待办事项。";
            }

            var todos = listResponse.getData();

            // 格式化输出待办列表
            StringBuilder result = new StringBuilder("您的待办事项:\n\n");
            int index = 1;
            for (var todo : todos) {
                result.append(String.format("%d. %s\n", index++, todo.getTitle()));
                result.append(String.format("   状态: %s\n",
                        TodoStatus.fromValue(todo.getTodoStatus()).getDescription()));
                result.append(String.format("   截止时间: %s\n",
                        formatTimestamp(todo.getDeadlineAt())));
                if (todo.getDesc() != null && !todo.getDesc().isEmpty()) {
                    result.append(String.format("   描述: %s\n", todo.getDesc()));
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("查询待办失败", e);
            return "查询待办失败: " + e.getMessage();
        }
    }

    /**
     * 格式化时间戳为可读字符串
     */
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return "未设置";
        }
        long millis = timestamp * 1000L;
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
