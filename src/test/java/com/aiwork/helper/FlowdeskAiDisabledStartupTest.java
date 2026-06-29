package com.aiwork.helper;

import com.aiwork.helper.ai.agent.AgentService;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.repository.UserRepository;
import com.aiwork.helper.service.ApprovalService;
import com.aiwork.helper.service.ChatService;
import com.aiwork.helper.service.DepartmentService;
import com.aiwork.helper.service.GroupService;
import com.aiwork.helper.service.TodoService;
import com.aiwork.helper.service.UserService;
import com.aiwork.helper.testsupport.CiFriendlySpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@CiFriendlySpringBootTest
@TestPropertySource(properties = {
        "flowdesk.ai.enabled=false",
        "spring.ai.dashscope.api-key=",
        "dashscope.api-key="
})
class FlowdeskAiDisabledStartupTest {

    @Autowired
    private ApplicationContext context;

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
    private ChatLogRepository chatLogRepository;

    @MockBean
    private UserRepository userRepository;

    @Test
    void contextStartsWithoutAiBeans() {
        assertThat(context.getBeansOfType(ChatClient.class)).isEmpty();
        assertThat(context.getBeansOfType(AgentService.class)).isEmpty();
        assertThat(context.getBeansOfType(VectorStoreService.class)).isEmpty();
    }
}
