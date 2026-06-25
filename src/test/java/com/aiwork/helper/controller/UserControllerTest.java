package com.aiwork.helper.controller;

import com.aiwork.helper.dto.request.LoginRequest;
import com.aiwork.helper.dto.response.LoginResponse;
import com.aiwork.helper.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private final UserService userService = mock(UserService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new UserController(userService))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void loginReturnsTokenFromUserService() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn(LoginResponse.builder()
                .id("user-123")
                .name("flowdesk-local-owner")
                .token("jwt-token")
                .accessExpire(123456789L)
                .build());

        mockMvc.perform(post("/v1/user/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(loginRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data.id").value("user-123"))
                .andExpect(jsonPath("$.data.name").value("flowdesk-local-owner"))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setName("flowdesk-local-owner");
        request.setPassword("local-only-bootstrap-password");
        return request;
    }
}
