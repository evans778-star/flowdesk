package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.mcp.McpToolCallError;
import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.McpToolDefinition;
import com.aiwork.helper.mcp.audit.McpAuditLogger;
import com.aiwork.helper.security.SecurityUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlowdeskMcpToolRegistry {

    private final Map<String, FlowdeskMcpTool> toolsByName;
    private final McpAuditLogger auditLogger;

    public FlowdeskMcpToolRegistry(List<FlowdeskMcpTool> tools) {
        this(tools, new McpAuditLogger());
    }

    public FlowdeskMcpToolRegistry(List<FlowdeskMcpTool> tools, McpAuditLogger auditLogger) {
        this.toolsByName = new LinkedHashMap<>();
        this.auditLogger = auditLogger;
        for (FlowdeskMcpTool tool : tools) {
            this.toolsByName.put(tool.definition().getName(), tool);
        }
    }

    public List<McpToolDefinition> listTools() {
        return toolsByName.values().stream()
                .map(FlowdeskMcpTool::definition)
                .toList();
    }

    public McpToolCallResponse call(String toolName, Map<String, Object> arguments) {
        long startedAt = System.currentTimeMillis();
        Map<String, Object> safeArguments = arguments != null ? arguments : Map.of();
        FlowdeskMcpTool tool = toolsByName.get(toolName);
        if (tool == null) {
            McpToolCallResponse response = McpToolCallResponse.error(
                    toolName,
                    McpToolCallError.TOOL_NOT_FOUND,
                    "Unknown MCP tool: " + toolName,
                    false
            );
            finishAudit(toolName, safeArguments, response, startedAt);
            return response;
        }
        try {
            McpToolCallResponse response = tool.call(safeArguments);
            if (response.getToolName() == null) {
                response.setToolName(toolName);
            }
            finishAudit(toolName, safeArguments, response, startedAt);
            return response;
        } catch (Exception e) {
            McpToolCallResponse response = McpToolCallResponse.error(
                    toolName,
                    McpToolCallError.TOOL_EXECUTION_FAILED,
                    "Tool execution failed.",
                    true
            );
            finishAudit(toolName, safeArguments, response, startedAt);
            return response;
        }
    }

    private void finishAudit(String toolName,
                             Map<String, Object> arguments,
                             McpToolCallResponse response,
                             long startedAt) {
        long durationMs = Math.max(0, System.currentTimeMillis() - startedAt);
        String errorCode = response.getError() != null ? response.getError().getCode() : null;
        if (response.getMetadata() == null) {
            response.setMetadata(new LinkedHashMap<>());
        }
        response.getMetadata().put("durationMs", durationMs);
        auditLogger.logToolCall(
                toolName,
                SecurityUtils.getCurrentUserId(),
                response.isSuccess(),
                errorCode,
                durationMs,
                arguments
        );
    }
}
