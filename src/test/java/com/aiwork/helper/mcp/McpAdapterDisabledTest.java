package com.aiwork.helper.mcp;

import com.aiwork.helper.ai.knowledge.RagCitationMapper;
import com.aiwork.helper.mcp.tool.FlowdeskMcpToolRegistry;
import com.aiwork.helper.service.ApprovalService;
import com.aiwork.helper.service.TodoService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class McpAdapterDisabledTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(TodoService.class, () -> Mockito.mock(TodoService.class))
            .withBean(ApprovalService.class, () -> Mockito.mock(ApprovalService.class))
            .withBean(RagCitationMapper.class, RagCitationMapper::new)
            .withUserConfiguration(FlowdeskMcpAutoConfiguration.class);

    @Test
    void mcpBeansAreNotLoadedWhenDisabled() {
        contextRunner
                .withPropertyValues("flowdesk.mcp.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(FlowdeskMcpToolRegistry.class);
                    assertThat(context).doesNotHaveBean(McpController.class);
                });
    }
}
