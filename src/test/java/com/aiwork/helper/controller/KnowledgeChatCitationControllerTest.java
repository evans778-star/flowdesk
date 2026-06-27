package com.aiwork.helper.controller;

import com.aiwork.helper.ai.knowledge.RagCitationMapper;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.dto.request.ChatRequest;
import com.aiwork.helper.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class KnowledgeChatCitationControllerTest {

    private final ChatService chatService = mock(ChatService.class);
    private final VectorStoreService vectorStoreService = mock(VectorStoreService.class);
    private final ObjectProvider<VectorStoreService> vectorStoreProvider = mockVectorStoreProvider();
    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new KnowledgeChatController(
                    chatService,
                    vectorStoreProvider,
                    new RagCitationMapper()
            ))
            .build();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    private static ObjectProvider<VectorStoreService> mockVectorStoreProvider() {
        return mock(ObjectProvider.class);
    }

    @Test
    void returnsChatAnswerWithCitations() throws Exception {
        when(vectorStoreProvider.getIfAvailable()).thenReturn(vectorStoreService);
        when(chatService.handleAIChat(any(), eq("What is the attendance policy?"), any(), any(), any()))
                .thenReturn("Employees should notify their manager before leave.");

        VectorStoreService.StoredDocument document = new VectorStoreService.StoredDocument();
        document.setId("doc:knowledge_java:test-id");
        document.setSource("docs/examples/sample-employee-handbook.pdf");
        document.setChunkIndex(0);
        document.setContent("Attendance policy requires employees to notify their manager.");
        when(vectorStoreService.searchSimilar("What is the attendance policy?", 3))
                .thenReturn(List.of(document));

        ChatRequest request = new ChatRequest();
        request.setPrompts("What is the attendance policy?");
        request.setChatType(0);

        mockMvc.perform(post("/v1/knowledge/chat-with-citations")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").value("Employees should notify their manager before leave."))
                .andExpect(jsonPath("$.data.citations[0].documentName").value("sample-employee-handbook.pdf"))
                .andExpect(jsonPath("$.data.citations[0].chunkId").value(0))
                .andExpect(jsonPath("$.data.citations[0].snippet").value("Attendance policy requires employees to notify their manager."));
    }
}
