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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@EnableConfigurationProperties(FlowdeskMcpProperties.class)
@ConditionalOnProperty(prefix = "flowdesk.mcp", name = "enabled", havingValue = "true")
public class FlowdeskMcpAutoConfiguration {

    @Bean
    SearchKnowledgeMcpTool searchKnowledgeMcpTool(ObjectProvider<VectorStoreService> vectorStoreServiceProvider,
                                                  RagCitationMapper citationMapper) {
        return new SearchKnowledgeMcpTool(vectorStoreServiceProvider, citationMapper);
    }

    @Bean
    ListTodosMcpTool listTodosMcpTool(TodoService todoService) {
        return new ListTodosMcpTool(todoService);
    }

    @Bean
    CreateTodoMcpTool createTodoMcpTool(TodoService todoService, FlowdeskMcpProperties properties) {
        return new CreateTodoMcpTool(todoService, properties);
    }

    @Bean
    ListApprovalsMcpTool listApprovalsMcpTool(ApprovalService approvalService) {
        return new ListApprovalsMcpTool(approvalService);
    }

    @Bean
    UploadDocumentMetadataMcpTool uploadDocumentMetadataMcpTool() {
        return new UploadDocumentMetadataMcpTool();
    }

    @Bean
    FlowdeskMcpToolRegistry flowdeskMcpToolRegistry(List<FlowdeskMcpTool> tools) {
        return new FlowdeskMcpToolRegistry(tools);
    }

}
