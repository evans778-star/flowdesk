package com.aiwork.helper.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpToolCallResponse {

    private boolean success;

    private String toolName;

    private Map<String, Object> data;

    private McpToolCallError error;

    private Map<String, Object> metadata;

    public static McpToolCallResponse success(Map<String, Object> data) {
        return success(null, data);
    }

    public static McpToolCallResponse success(String toolName, Map<String, Object> data) {
        return new McpToolCallResponse(true, toolName, data, null, new LinkedHashMap<>());
    }

    public static McpToolCallResponse error(String error) {
        return error(null, McpToolCallError.VALIDATION_ERROR, error, false);
    }

    public static McpToolCallResponse error(String toolName, String code, String message, boolean retryable) {
        return new McpToolCallResponse(
                false,
                toolName,
                Map.of(),
                new McpToolCallError(code, message, retryable),
                new LinkedHashMap<>()
        );
    }
}
