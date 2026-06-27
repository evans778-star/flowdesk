package com.aiwork.helper.mcp;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class McpToolCallRequest {

    private Map<String, Object> arguments = new HashMap<>();
}
