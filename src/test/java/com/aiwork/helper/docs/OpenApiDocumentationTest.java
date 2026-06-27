package com.aiwork.helper.docs;

import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.repository.UserRepository;
import com.aiwork.helper.service.AIService;
import com.aiwork.helper.service.ApprovalService;
import com.aiwork.helper.service.ChatService;
import com.aiwork.helper.service.DepartmentService;
import com.aiwork.helper.service.GroupService;
import com.aiwork.helper.service.TodoService;
import com.aiwork.helper.service.UserService;
import com.aiwork.helper.testsupport.CiFriendlySpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@CiFriendlySpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "flowdesk.mcp.enabled=true")
class OpenApiDocumentationTest {

    @MockBean
    private VectorStoreService vectorStoreService;

    @MockBean
    private UserService userService;

    @MockBean
    private TodoService todoService;

    @MockBean
    private GroupService groupService;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private ChatService chatService;

    @MockBean
    private ApprovalService approvalService;

    @MockBean
    private AIService aiService;

    @MockBean
    private ChatLogRepository chatLogRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiDocsIncludeCoreFlowdeskPathsAndTags() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .contains("/v1/user/login")
                .contains("/v1/chat")
                .contains("/v1/knowledge/chat-with-citations")
                .contains("/v1/mcp/jsonrpc")
                .contains("/v1/upload/file")
                .contains("AI Chat")
                .contains("Knowledge RAG")
                .contains("MCP Adapter")
                .contains("File Upload");
    }
}
