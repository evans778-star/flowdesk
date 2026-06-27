package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.config.FlowdeskMcpProperties;
import com.aiwork.helper.service.TodoService;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class CreateTodoMcpToolTest {

    private final TodoService todoService = mock(TodoService.class);

    @Test
    void rejectsWritesWhenWriteToolsAreDisabled() {
        FlowdeskMcpProperties properties = new FlowdeskMcpProperties();
        properties.setWriteToolsEnabled(false);
        CreateTodoMcpTool tool = new CreateTodoMcpTool(todoService, properties);

        McpToolCallResponse response = tool.call(Map.of(
                "title", "Prepare release notes",
                "description", "Draft public-safe release notes"
        ));

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getError().getCode()).isEqualTo("WRITE_TOOLS_DISABLED");
        assertThat(response.getError().getMessage()).contains("disabled");
        verifyNoInteractions(todoService);
    }
}
