package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.dto.request.TodoRequest;
import com.aiwork.helper.mcp.McpToolCallError;
import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.McpToolDefinition;
import com.aiwork.helper.mcp.config.FlowdeskMcpProperties;
import com.aiwork.helper.service.TodoService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CreateTodoMcpTool implements FlowdeskMcpTool {

    private final TodoService todoService;
    private final FlowdeskMcpProperties properties;

    public CreateTodoMcpTool(TodoService todoService, FlowdeskMcpProperties properties) {
        this.todoService = todoService;
        this.properties = properties;
    }

    @Override
    public McpToolDefinition definition() {
        Map<String, Map<String, Object>> properties = new LinkedHashMap<>();
        properties.put("title", McpToolSchemas.stringProperty("Todo title."));
        properties.put("description", McpToolSchemas.stringProperty("Optional todo description."));
        properties.put("assignee", McpToolSchemas.stringProperty("Optional single assignee user id."));
        properties.put("assigneeIds", McpToolSchemas.stringArrayProperty("Optional assignee user ids."));
        properties.put("dueDate", McpToolSchemas.integerProperty("Optional due date as Unix seconds."));
        return new McpToolDefinition(
                "flowdesk_create_todo",
                "Create todo",
                "Create a todo for the authenticated current user. Disabled unless write tools are enabled.",
                false,
                true,
                List.of("mcp:todo:write"),
                McpToolSchemas.objectSchema(List.of("title"), properties),
                "todoCreateResult"
        );
    }

    @Override
    public McpToolCallResponse call(Map<String, Object> arguments) {
        if (!properties.isWriteToolsEnabled()) {
            return McpToolCallResponse.error(
                    definition().getName(),
                    McpToolCallError.WRITE_TOOLS_DISABLED,
                    "MCP write tools are disabled. Set FLOWDESK_MCP_WRITE_TOOLS_ENABLED=true to enable this tool.",
                    false
            );
        }

        String title = McpArgumentUtils.stringValue(arguments, "title");
        if (title == null || title.isBlank()) {
            return McpToolCallResponse.error(definition().getName(), McpToolCallError.VALIDATION_ERROR, "title is required", false);
        }

        TodoRequest request = new TodoRequest();
        request.setTitle(title);
        request.setDesc(McpArgumentUtils.stringValue(arguments, "description"));
        request.setDeadlineAt(McpArgumentUtils.longValue(arguments, "dueDate"));

        List<String> assigneeIds = McpArgumentUtils.stringListValue(arguments, "assigneeIds");
        String assignee = McpArgumentUtils.stringValue(arguments, "assignee");
        if (assigneeIds.isEmpty() && assignee != null && !assignee.isBlank()) {
            assigneeIds = List.of(assignee);
        }
        request.setExecuteIds(assigneeIds);

        String id = todoService.create(request);
        return McpToolCallResponse.success(definition().getName(), Map.of("todoId", id));
    }
}
