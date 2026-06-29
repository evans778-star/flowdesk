package com.aiwork.helper.mcp;

import com.aiwork.helper.ai.knowledge.RagCitationMapper;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.mcp.config.FlowdeskMcpProperties;
import com.aiwork.helper.mcp.tool.CreateTodoMcpTool;
import com.aiwork.helper.mcp.tool.FlowdeskMcpTool;
import com.aiwork.helper.mcp.tool.FlowdeskMcpToolRegistry;
import com.aiwork.helper.mcp.tool.ListApprovalsMcpTool;
import com.aiwork.helper.mcp.tool.ListTodosMcpTool;
import com.aiwork.helper.mcp.tool.SearchKnowledgeMcpTool;
import com.aiwork.helper.mcp.tool.UploadDocumentMetadataMcpTool;
import com.aiwork.helper.service.ApprovalService;
import com.aiwork.helper.service.TodoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class McpDemoSmokeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void jsonRpcPreviewSupportsDemoHandshakeAndSafeToolCalls() throws Exception {
        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new McpController(new FlowdeskMcpToolRegistry(tools())))
                .build();

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "init",
                                "method", "initialize",
                                "params", Map.of("protocolVersion", "2025-06-18")
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.serverInfo.name").value("flowdesk"))
                .andExpect(jsonPath("$.result.capabilities.tools.listChanged").value(false));

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "ping",
                                "method", "ping",
                                "params", Map.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "tools",
                                "method", "tools/list",
                                "params", Map.of()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.tools.length()").value(5))
                .andExpect(jsonPath("$.result.tools[0].inputSchema.type").exists());

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "metadata",
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", "flowdesk_upload_document_metadata",
                                        "arguments", Map.of("fileName", "demo.pdf", "contentType", "application/pdf")
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(false))
                .andExpect(jsonPath("$.result.structuredContent.success").value(true));

        mockMvc.perform(post("/v1/mcp/jsonrpc")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "jsonrpc", "2.0",
                                "id", "write-disabled",
                                "method", "tools/call",
                                "params", Map.of(
                                        "name", "flowdesk_create_todo",
                                        "arguments", Map.of("title", "Prepare release demo")
                                )
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.isError").value(true))
                .andExpect(jsonPath("$.result.structuredContent.error.code").value("WRITE_TOOLS_DISABLED"));
    }

    @SuppressWarnings("unchecked")
    private List<FlowdeskMcpTool> tools() {
        FlowdeskMcpProperties properties = new FlowdeskMcpProperties();
        properties.setWriteToolsEnabled(false);
        TodoService todoService = Mockito.mock(TodoService.class);
        ApprovalService approvalService = Mockito.mock(ApprovalService.class);
        ObjectProvider<VectorStoreService> missingVectorStore = Mockito.mock(ObjectProvider.class);
        Mockito.when(missingVectorStore.getIfAvailable()).thenReturn(null);

        return List.of(
                new SearchKnowledgeMcpTool(missingVectorStore, new RagCitationMapper()),
                new ListTodosMcpTool(todoService),
                new CreateTodoMcpTool(todoService, properties),
                new ListApprovalsMcpTool(approvalService),
                new UploadDocumentMetadataMcpTool()
        );
    }
}
