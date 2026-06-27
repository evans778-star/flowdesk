package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.dto.request.TodoListRequest;
import com.aiwork.helper.dto.response.TodoListResponse;
import com.aiwork.helper.mcp.McpToolCallError;
import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.McpToolDefinition;
import com.aiwork.helper.service.TodoService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ListTodosMcpTool implements FlowdeskMcpTool {

    private final TodoService todoService;

    public ListTodosMcpTool(TodoService todoService) {
        this.todoService = todoService;
    }

    @Override
    public McpToolDefinition definition() {
        Map<String, Map<String, Object>> properties = new LinkedHashMap<>();
        properties.put("currentUser", McpToolSchemas.booleanProperty("Must be true or omitted. This tool uses the authenticated user."));
        properties.put("page", McpToolSchemas.integerProperty("Page number, default 1."));
        properties.put("count", McpToolSchemas.integerProperty("Page size, default 10."));
        properties.put("startTime", McpToolSchemas.integerProperty("Optional Unix seconds lower bound."));
        properties.put("endTime", McpToolSchemas.integerProperty("Optional Unix seconds upper bound."));
        return new McpToolDefinition(
                "flowdesk_list_todos",
                "List todos",
                "List todos for the authenticated current user.",
                true,
                false,
                List.of("mcp:todo:read"),
                McpToolSchemas.objectSchema(List.of(), properties),
                "todoListResult"
        );
    }

    @Override
    public McpToolCallResponse call(Map<String, Object> arguments) {
        if (arguments.containsKey("userId")) {
            return McpToolCallResponse.error(
                    definition().getName(),
                    McpToolCallError.PERMISSION_DENIED,
                    "userId is not accepted; this tool uses the authenticated current user.",
                    false
            );
        }

        TodoListRequest request = new TodoListRequest();
        request.setPage(McpArgumentUtils.intValue(arguments, "page", 1));
        request.setCount(McpArgumentUtils.intValue(arguments, "count", 10));
        request.setStartTime(McpArgumentUtils.longValue(arguments, "startTime"));
        request.setEndTime(McpArgumentUtils.longValue(arguments, "endTime"));

        TodoListResponse response = todoService.list(request);
        return McpToolCallResponse.success(definition().getName(), Map.of(
                "count", response.getCount(),
                "todos", response.getData() != null ? response.getData() : java.util.List.of()
        ));
    }
}
