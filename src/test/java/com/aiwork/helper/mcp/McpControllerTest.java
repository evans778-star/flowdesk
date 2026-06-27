package com.aiwork.helper.mcp;

import com.aiwork.helper.mcp.tool.FlowdeskMcpToolRegistry;
import com.aiwork.helper.mcp.tool.FlowdeskMcpTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class McpControllerTest {

    private final FlowdeskMcpToolRegistry registry = mock(FlowdeskMcpToolRegistry.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new McpController(registry))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void listsRegisteredTools() throws Exception {
        when(registry.listTools()).thenReturn(List.of(new McpToolDefinition(
                "flowdesk_search_knowledge",
                "Search Flowdesk knowledge chunks",
                Map.of("query", "string"),
                false
        )));

        mockMvc.perform(get("/v1/mcp/tools"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("flowdesk_search_knowledge"));
    }

    @Test
    void callsToolByName() throws Exception {
        when(registry.call("flowdesk_upload_document_metadata", Map.of("fileName", "sample.pdf")))
                .thenReturn(McpToolCallResponse.success(
                        "flowdesk_upload_document_metadata",
                        Map.of("message", "Upload through /v1/upload/file")
                ));

        McpToolCallRequest request = new McpToolCallRequest();
        request.setArguments(Map.of("fileName", "sample.pdf"));

        mockMvc.perform(post("/v1/mcp/tools/flowdesk_upload_document_metadata/call")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.toolName").value("flowdesk_upload_document_metadata"))
                .andExpect(jsonPath("$.data.data.message").value("Upload through /v1/upload/file"));
    }

    @Test
    void unknownToolReturnsStructuredToolNotFoundError() throws Exception {
        FlowdeskMcpToolRegistry realRegistry = new FlowdeskMcpToolRegistry(List.of());
        MockMvc realMockMvc = MockMvcBuilders
                .standaloneSetup(new McpController(realRegistry))
                .build();

        McpToolCallRequest request = new McpToolCallRequest();
        request.setArguments(Map.of("query", "anything"));

        realMockMvc.perform(post("/v1/mcp/tools/missing_tool/call")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.toolName").value("missing_tool"))
                .andExpect(jsonPath("$.data.error.code").value("TOOL_NOT_FOUND"))
                .andExpect(jsonPath("$.data.error.retryable").value(false));
    }

    @Test
    void toolExecutionFailureDoesNotLeakStackTrace() throws Exception {
        FlowdeskMcpToolRegistry realRegistry = new FlowdeskMcpToolRegistry(List.of(new FlowdeskMcpTool() {
            @Override
            public McpToolDefinition definition() {
                return new McpToolDefinition(
                        "flowdesk_test_failure",
                        "Test failure",
                        "Failure tool",
                        true,
                        false,
                        List.of("mcp:test:read"),
                        Map.of("type", "object"),
                        "testResult"
                );
            }

            @Override
            public McpToolCallResponse call(Map<String, Object> arguments) {
                throw new IllegalStateException("boom\nat com.secret.Internal");
            }
        }));
        MockMvc realMockMvc = MockMvcBuilders
                .standaloneSetup(new McpController(realRegistry))
                .build();

        McpToolCallRequest request = new McpToolCallRequest();
        request.setArguments(Map.of("query", "anything"));

        realMockMvc.perform(post("/v1/mcp/tools/flowdesk_test_failure/call")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.success").value(false))
                .andExpect(jsonPath("$.data.error.code").value("TOOL_EXECUTION_FAILED"))
                .andExpect(jsonPath("$.data.error.message").value("Tool execution failed."))
                .andExpect(jsonPath("$.data.error.message").value(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("com.secret"))));
    }
}
