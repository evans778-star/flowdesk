package com.aiwork.helper.mcp;

import com.aiwork.helper.dto.response.ApiResponse;
import com.aiwork.helper.mcp.tool.FlowdeskMcpToolRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/mcp")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "flowdesk.mcp", name = "enabled", havingValue = "true")
@Tag(name = "MCP Adapter", description = "HTTP and JSON-RPC preview adapter for Flowdesk MCP tools")
public class McpController {

    private static final String SUPPORTED_PROTOCOL_VERSION = "2025-06-18";
    private static final String SERVER_NAME = "flowdesk";
    private static final String SERVER_VERSION = "1.0.0-SNAPSHOT";

    private final FlowdeskMcpToolRegistry toolRegistry;

    @GetMapping("/tools")
    @Operation(summary = "List MCP tools", description = "Returns available Flowdesk tool definitions, input schemas, and permission metadata.")
    public ApiResponse<List<McpToolDefinition>> listTools() {
        return ApiResponse.success(toolRegistry.listTools());
    }

    @PostMapping("/tools/{toolName}/call")
    @Operation(summary = "Call MCP tool over HTTP", description = "Executes one MCP adapter preview tool and returns a structured tool response.")
    public ApiResponse<McpToolCallResponse> callTool(
            @PathVariable String toolName,
            @RequestBody McpToolCallRequest request
    ) {
        return ApiResponse.success(toolRegistry.call(toolName, request.getArguments()));
    }

    @PostMapping("/jsonrpc")
    @Operation(summary = "Call MCP JSON-RPC preview", description = "Supports initialize, ping, tools/list, and tools/call for bridge clients.")
    public McpJsonRpcResponse jsonRpc(@RequestBody McpJsonRpcRequest request) {
        if (request == null || !"2.0".equals(request.getJsonrpc()) || request.getMethod() == null) {
            Object id = request != null ? request.getId() : null;
            return McpJsonRpcResponse.error(id, McpJsonRpcError.INVALID_REQUEST, "Invalid JSON-RPC request");
        }

        return switch (request.getMethod()) {
            case "initialize" -> initialize(request);
            case "ping" -> McpJsonRpcResponse.result(request.getId(), Map.of());
            case "tools/list" -> listJsonRpcTools(request);
            case "tools/call" -> callJsonRpcTool(request);
            default -> McpJsonRpcResponse.error(
                    request.getId(),
                    McpJsonRpcError.METHOD_NOT_FOUND,
                    "Method not found"
            );
        };
    }

    private McpJsonRpcResponse initialize(McpJsonRpcRequest request) {
        Map<String, Object> params = paramsMap(request.getParams());
        if (params == null) {
            return McpJsonRpcResponse.error(request.getId(), McpJsonRpcError.INVALID_PARAMS, "Params must be an object");
        }

        Map<String, Object> serverInfo = new LinkedHashMap<>();
        serverInfo.put("name", SERVER_NAME);
        serverInfo.put("version", SERVER_VERSION);

        Map<String, Object> tools = new LinkedHashMap<>();
        tools.put("listChanged", false);

        Map<String, Object> capabilities = new LinkedHashMap<>();
        capabilities.put("tools", tools);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("protocolVersion", SUPPORTED_PROTOCOL_VERSION);
        result.put("serverInfo", serverInfo);
        result.put("capabilities", capabilities);
        return McpJsonRpcResponse.result(request.getId(), result);
    }

    private McpJsonRpcResponse listJsonRpcTools(McpJsonRpcRequest request) {
        Map<String, Object> params = paramsMap(request.getParams());
        if (params == null) {
            return McpJsonRpcResponse.error(request.getId(), McpJsonRpcError.INVALID_PARAMS, "Params must be an object");
        }
        return McpJsonRpcResponse.result(request.getId(), Map.of("tools", toolRegistry.listTools()));
    }

    private McpJsonRpcResponse callJsonRpcTool(McpJsonRpcRequest request) {
        Map<String, Object> params = paramsMap(request.getParams());
        if (params == null) {
            return McpJsonRpcResponse.error(request.getId(), McpJsonRpcError.INVALID_PARAMS, "Params must be an object");
        }
        Object name = params.get("name");
        if (name == null || String.valueOf(name).isBlank()) {
            return McpJsonRpcResponse.error(request.getId(), McpJsonRpcError.INVALID_PARAMS, "Tool name is required");
        }

        Object argumentsValue = params.get("arguments");
        Map<String, Object> arguments = argumentsValue == null ? Map.of() : mapValue(argumentsValue);
        if (arguments == null) {
            return McpJsonRpcResponse.error(request.getId(), McpJsonRpcError.INVALID_PARAMS, "Tool arguments must be an object");
        }
        McpToolCallResponse toolResponse = toolRegistry.call(String.valueOf(name), arguments);
        return McpJsonRpcResponse.result(request.getId(), toToolResult(toolResponse));
    }

    private Map<String, Object> paramsMap(Object value) {
        if (value == null) {
            return Map.of();
        }
        return mapValue(value);
    }

    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> source) {
            Map<String, Object> result = new LinkedHashMap<>();
            source.forEach((key, item) -> result.put(String.valueOf(key), item));
            return result;
        }
        return null;
    }

    private Map<String, Object> toToolResult(McpToolCallResponse toolResponse) {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean isError = !toolResponse.isSuccess();
        result.put("content", List.of(Map.of(
                "type", "text",
                "text", summarizeToolResponse(toolResponse)
        )));
        result.put("structuredContent", toolResponse);
        result.put("isError", isError);
        return result;
    }

    private String summarizeToolResponse(McpToolCallResponse toolResponse) {
        if (toolResponse.isSuccess()) {
            return "Tool " + toolResponse.getToolName() + " returned structured content.";
        }
        String message = toolResponse.getError() != null ? toolResponse.getError().getMessage() : "Tool execution failed.";
        return message != null ? message : "Tool execution failed.";
    }
}
