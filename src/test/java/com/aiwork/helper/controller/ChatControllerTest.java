package com.aiwork.helper.controller;

import com.aiwork.helper.dto.request.ChatRequest;
import com.aiwork.helper.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ChatControllerTest {

    private final ChatService chatService = mock(ChatService.class);
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new ChatController(chatService))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void returnsClearDisabledMessageFromChatService() throws Exception {
        when(chatService.handleAIChat(any(), eq("hello"), any(), any(), any()))
                .thenReturn("AI is disabled. Set FLOWDESK_AI_ENABLED=true and configure FLOWDESK_AI_PROVIDER to enable AI chat.");

        ChatRequest request = new ChatRequest();
        request.setPrompts("hello");
        request.setChatType(0);

        mockMvc.perform(post("/v1/chat")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").value(containsString("AI is disabled")));
    }

    @Test
    void returnsOllamaProviderResponseFromChatService() throws Exception {
        when(chatService.handleAIChat(any(), eq("hello"), any(), any(), any()))
                .thenReturn("hello from local ollama");

        ChatRequest request = new ChatRequest();
        request.setPrompts("hello");
        request.setChatType(0);

        mockMvc.perform(post("/v1/chat")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").value("hello from local ollama"));
    }

    @Test
    void wrapsProviderUnavailableMessageWithoutStackTrace() throws Exception {
        when(chatService.handleAIChat(any(), eq("hello"), any(), any(), any()))
                .thenReturn("Ollama is unavailable. Start Ollama and pull the configured model.");

        ChatRequest request = new ChatRequest();
        request.setPrompts("hello");
        request.setChatType(0);

        mockMvc.perform(post("/v1/chat")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").value(containsString("Ollama is unavailable")))
                .andExpect(jsonPath("$.data.data").value(org.hamcrest.Matchers.not(containsString("Exception"))));
    }
}
