package com.aiwork.helper;

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
import org.springframework.boot.test.mock.mockito.MockBean;

@CiFriendlySpringBootTest
class FlowdeskApplicationSmokeTest {

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

    @Test
    void contextLoads() {
    }
}
