package com.aiwork.helper.controller;

import com.aiwork.helper.ai.knowledge.RagCitationMapper;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.dto.request.ChatRequest;
import com.aiwork.helper.dto.response.ApiResponse;
import com.aiwork.helper.dto.response.ChatResponse;
import com.aiwork.helper.dto.response.CitationResponse;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/knowledge")
@RequiredArgsConstructor
@Tag(name = "Knowledge RAG", description = "Knowledge-base chat with citation metadata")
public class KnowledgeChatController {

    private static final int DEFAULT_TOP_K = 3;

    private final ChatService chatService;
    private final ObjectProvider<VectorStoreService> vectorStoreServiceProvider;
    private final RagCitationMapper citationMapper;

    @PostMapping("/chat-with-citations")
    @Operation(summary = "Chat with citations", description = "Returns an AI answer plus RAG citation metadata when local retrieval is available.")
    public ApiResponse<ChatResponse> chatWithCitations(@RequestBody ChatRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        String prompt = request.getPrompts();

        List<CitationResponse> citations = findCitations(prompt);
        String answer = chatService.handleAIChat(
                userId,
                prompt,
                request.getRelationId(),
                request.getStartTime(),
                request.getEndTime()
        );

        ChatResponse response = ChatResponse.builder()
                .chatType(request.getChatType() != null ? request.getChatType() : 0)
                .data(answer)
                .citations(citations)
                .build();

        return ApiResponse.success(response);
    }

    private List<CitationResponse> findCitations(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return List.of();
        }

        VectorStoreService vectorStoreService = vectorStoreServiceProvider.getIfAvailable();
        if (vectorStoreService == null) {
            return List.of();
        }

        try {
            return citationMapper.toCitations(vectorStoreService.searchSimilar(prompt, DEFAULT_TOP_K));
        } catch (Exception e) {
            log.warn("RAG citation search skipped: {}", e.getMessage());
            return List.of();
        }
    }
}
