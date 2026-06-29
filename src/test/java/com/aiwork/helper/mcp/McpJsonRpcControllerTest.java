package com.aiwork.helper.mcp;

import com.aiwork.helper.mcp.config.FlowdeskMcpProperties;
import com.aiwork.helper.mcp.tool.CreateTodoMcpTool;
import com.aiwork.helper.mcp.tool.FlowdeskMcpTool;
import com.aiwork.helper.mcp.tool.FlowdeskMcpToolRegistry;
import com.aiwork.helper.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class McpJsonRpcControllerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void jsonRpcInitializeReturnsServerCapabilities() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new StaticTool()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "init-1",
                                "method", "initialize",
                                "params", Map.of("protocolVersion", "2025-06-18")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("init-1"))
                .andExpect(jsonPath("$.result.protocolVersion").value("2025-06-18"))
                .andExpect(jsonPath("$.result.serverInfo.name").value("flowdesk"))
                .andExpect(jsonPath("$.result.serverInfo.version").value("1.0.0-SNAPSHOT"))
                .andExpect(jsonPath("$.result.capabilities.tools.listChanged").value(false));
    }

    @Test
    void jsonRpcPingReturnsEmptyResult() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new StaticTool()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "ping-1",
                                "method", "ping",
                                "params", Map.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("ping-1"))
                .andExpect(jsonPath("$.result").exists());
    }

    @Test
    void jsonRpcToolsListReturnsJsonRpcShape() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new StaticTool()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "1",
                                "method", "tools/list",
                                "params", Map.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.result.tools[0].name").value("flowdesk_static_tool"))
                .andExpect(jsonPath("$.result.tools[0].inputSchema.type").value("object"))
                .andExpect(jsonPath("$.result.tools[0].outputSchema.type").value("object"))
                .andExpect(jsonPath("$.result.tools[0].annotations.readOnlyHint").value(true))
                .andExpect(jsonPath("$.result.tools[0].annotations.destructiveHint").value(false));
    }

    @Test
    void jsonRpcToolsCallInvokesRegistryTool() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new StaticTool()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "2",
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", "flowdesk_static_tool",
                                        "arguments", Map.of("query", "hello")
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("2"))
                .andExpect(jsonPath("$.result.isError").value(false))
                .andExpect(jsonPath("$.result.structuredContent.success").value(true))
                .andExpect(jsonPath("$.result.structuredContent.data.echo").value("hello"))
                .andExpect(jsonPath("$.result.content[0].type").value("text"));
    }

    @Test
    void jsonRpcUnknownMethodReturnsJsonRpcError() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "3",
                                "method", "resources/list",
                                "params", Map.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("3"))
                .andExpect(jsonPath("$.error.code").value(-32601))
                .andExpect(jsonPath("$.error.message").value("Method not found"));
    }

    @Test
    void jsonRpcInvalidRequestReturnsJsonRpcError() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "bad-1",
                                "params", Map.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("bad-1"))
                .andExpect(jsonPath("$.error.code").value(-32600))
                .andExpect(jsonPath("$.error.message").value("Invalid JSON-RPC request"));
    }

    @Test
    void jsonRpcToolsCallWithoutNameReturnsInvalidParams() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new StaticTool()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "params-1",
                                "method", "tools/call",
                                "params", Map.of("arguments", Map.of())
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("params-1"))
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value("Tool name is required"));
    }

    @Test
    void jsonRpcToolsCallRejectsNonObjectArguments() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new StaticTool()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "params-2",
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", "flowdesk_static_tool",
                                        "arguments", "not-an-object"
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jsonrpc").value("2.0"))
                .andExpect(jsonPath("$.id").value("params-2"))
                .andExpect(jsonPath("$.error.code").value(-32602))
                .andExpect(jsonPath("$.error.message").value("Tool arguments must be an object"));
    }

    @Test
    void jsonRpcUnknownToolReturnsStableToolErrorResult() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "4",
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", "missing_tool",
                                        "arguments", Map.of()
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(true))
                .andExpect(jsonPath("$.result.structuredContent.error.code").value("TOOL_NOT_FOUND"));
    }

    @Test
    void jsonRpcWriteToolDisabledReturnsStructuredToolError() throws Exception {
        TodoService todoService = mock(TodoService.class);
        FlowdeskMcpProperties properties = new FlowdeskMcpProperties();
        properties.setWriteToolsEnabled(false);
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new CreateTodoMcpTool(todoService, properties)));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "5",
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", "flowdesk_create_todo",
                                        "arguments", Map.of("title", "Prepare demo")
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(true))
                .andExpect(jsonPath("$.result.structuredContent.error.code").value("WRITE_TOOLS_DISABLED"));

        verifyNoInteractions(todoService);
    }

    @Test
    void jsonRpcToolExceptionDoesNotLeakStackTrace() throws Exception {
        FlowdeskMcpToolRegistry registry = new FlowdeskMcpToolRegistry(List.of(new ThrowingTool()));
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new McpController(registry)).build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "6",
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", "flowdesk_throwing_tool",
                                        "arguments", Map.of("query", "anything")
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(true))
                .andExpect(jsonPath("$.result.structuredContent.error.code").value("TOOL_EXECUTION_FAILED"))
                .andExpect(jsonPath("$.result.structuredContent.error.message").value("Tool execution failed."))
                .andExpect(jsonPath("$.result.content[0].text").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("com.secret"))));
    }

    private static class StaticTool implements FlowdeskMcpTool {
        @Override
        public McpToolDefinition definition() {
            return new McpToolDefinition(
                    "flowdesk_static_tool",
                    "Static tool",
                    "Static test tool",
                    true,
                    false,
                    List.of("mcp:test:read"),
                    Map.of("type", "object", "properties", Map.of("query", Map.of("type", "string"))),
                    "staticResult"
            );
        }

        @Override
        public McpToolCallResponse call(Map<String, Object> arguments) {
            return McpToolCallResponse.success(definition().getName(), Map.of("echo", arguments.get("query")));
        }
    }

    private static class ThrowingTool extends StaticTool {
        @Override
        public McpToolDefinition definition() {
            return new McpToolDefinition(
                    "flowdesk_throwing_tool",
                    "Throwing tool",
                    "Throwing test tool",
                    true,
                    false,
                    List.of("mcp:test:read"),
                    Map.of("type", "object"),
                    "throwingResult"
            );
        }

        @Override
        public McpToolCallResponse call(Map<String, Object> arguments) {
            throw new IllegalStateException("boom\nat com.secret.Internal");
        }
    }
}
