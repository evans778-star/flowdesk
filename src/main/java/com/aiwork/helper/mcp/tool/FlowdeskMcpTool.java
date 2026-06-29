package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.McpToolDefinition;

import java.util.Map;

public interface FlowdeskMcpTool {

    McpToolDefinition definition();

    McpToolCallResponse call(Map<String, Object> arguments);
}
