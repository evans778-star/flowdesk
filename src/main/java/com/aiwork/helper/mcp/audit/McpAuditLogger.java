package com.aiwork.helper.mcp.audit;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class McpAuditLogger {

    public void logToolCall(String toolName,
                            String caller,
                            boolean success,
                            String errorCode,
                            long durationMs,
                            Map<String, Object> arguments) {
        log.info(
                "mcp.tool.call toolName={} caller={} success={} errorCode={} durationMs={} arguments={}",
                toolName,
                caller != null ? caller : "anonymous",
                success,
                errorCode,
                durationMs,
                McpAuditSanitizer.sanitizeArguments(arguments)
        );
    }
}
