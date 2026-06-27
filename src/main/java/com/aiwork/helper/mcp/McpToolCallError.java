package com.aiwork.helper.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpToolCallError {

    public static final String TOOL_NOT_FOUND = "TOOL_NOT_FOUND";
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    public static final String WRITE_TOOLS_DISABLED = "WRITE_TOOLS_DISABLED";
    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
    public static final String TOOL_EXECUTION_FAILED = "TOOL_EXECUTION_FAILED";

    private String code;

    private String message;

    private boolean retryable;
}
