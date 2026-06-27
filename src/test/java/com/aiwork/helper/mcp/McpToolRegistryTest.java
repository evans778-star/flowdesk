package com.aiwork.helper.mcp;

import com.aiwork.helper.ai.knowledge.RagCitationMapper;
import com.aiwork.helper.mcp.tool.FlowdeskMcpToolRegistry;
import com.aiwork.helper.service.ApprovalService;
import com.aiwork.helper.service.TodoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class McpToolRegistryTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(TodoService.class, () -> Mockito.mock(TodoService.class))
            .withBean(ApprovalService.class, () -> Mockito.mock(ApprovalService.class))
            .withBean(RagCitationMapper.class, RagCitationMapper::new)
            .withUserConfiguration(FlowdeskMcpAutoConfiguration.class);

    @Test
    void registryContainsExpectedToolsWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "flowdesk.mcp.enabled=true",
                        "flowdesk.mcp.write-tools-enabled=false"
                )
                .run(context -> {
                    FlowdeskMcpToolRegistry registry = context.getBean(FlowdeskMcpToolRegistry.class);

                    assertThat(registry.listTools())
                            .extracting(McpToolDefinition::getName)
                            .containsExactlyInAnyOrder(
                                    "flowdesk_search_knowledge",
                                    "flowdesk_list_todos",
                                    "flowdesk_create_todo",
                                    "flowdesk_list_approvals",
                                    "flowdesk_upload_document_metadata"
                            );
                });
    }

    @Test
    void toolDefinitionsExposeStableSchemaAndPermissions() {
        contextRunner
                .withPropertyValues(
                        "flowdesk.mcp.enabled=true",
                        "flowdesk.mcp.write-tools-enabled=false"
                )
                .run(context -> {
                    FlowdeskMcpToolRegistry registry = context.getBean(FlowdeskMcpToolRegistry.class);

                    McpToolDefinition searchTool = registry.listTools().stream()
                            .filter(tool -> "flowdesk_search_knowledge".equals(tool.getName()))
                            .findFirst()
                            .orElseThrow();

                    assertThat(searchTool.getTitle()).isEqualTo("Search knowledge");
                    assertThat(searchTool.isReadOnly()).isTrue();
                    assertThat(searchTool.isWrite()).isFalse();
                    assertThat(searchTool.getRequiredPermissions()).containsExactly("mcp:knowledge:read");
                    assertThat(searchTool.getResultType()).isEqualTo("knowledgeSearchResult");
                    assertThat(searchTool.getInputSchema())
                            .containsEntry("type", "object")
                            .containsKey("properties")
                            .containsKey("required");
                    assertThat(searchTool.getOutputSchema())
                            .containsEntry("type", "object")
                            .containsKey("properties");
                    assertThat(searchTool.getAnnotations())
                            .containsEntry("readOnlyHint", true)
                            .containsEntry("destructiveHint", false)
                            .containsEntry("openWorldHint", false);

                    McpToolDefinition createTodoTool = registry.listTools().stream()
                            .filter(tool -> "flowdesk_create_todo".equals(tool.getName()))
                            .findFirst()
                            .orElseThrow();

                    assertThat(createTodoTool.isWrite()).isTrue();
                    assertThat(createTodoTool.getRequiredPermissions()).containsExactly("mcp:todo:write");
                    assertThat(createTodoTool.getAnnotations())
                            .containsEntry("readOnlyHint", false)
                            .containsEntry("destructiveHint", true);
                });
    }
}
