package com.aiwork.helper.security;

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
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@CiFriendlySpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

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
    void apiDocsEndpointDoesNotRequireJwt() throws Exception {
        int status = mockMvc.perform(get("/v3/api-docs"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void swaggerUiEndpointDoesNotRequireJwt() throws Exception {
        int status = mockMvc.perform(get("/swagger-ui/index.html"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isNotIn(401, 403);
    }

    @Test
    void healthEndpointDoesNotRequireJwt() throws Exception {
        int status = mockMvc.perform(get("/actuator/health"))
                .andReturn()
                .getResponse()
                .getStatus();

        assertThat(status).isNotIn(401, 403);
    }
}
