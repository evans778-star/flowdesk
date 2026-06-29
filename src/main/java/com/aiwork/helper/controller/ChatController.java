package com.aiwork.helper.controller;

import com.aiwork.helper.dto.request.ChatRequest;
import com.aiwork.helper.dto.response.ApiResponse;
import com.aiwork.helper.dto.response.ChatResponse;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
@Tag(name = "AI Chat", description = "AI chat endpoint backed by optional DashScope or Ollama providers")
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    @Operation(summary = "Chat with AI", description = "Sends a prompt to the configured AI provider. Real answers require DashScope or Ollama configuration.")
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        String userId = SecurityUtils.getCurrentUserId();

        log.info("AI chat request: userId={}, prompts={}, chatType={}, relationId={}, startTime={}, endTime={}",
                userId, request.getPrompts(), request.getChatType(), request.getRelationId(),
                request.getStartTime(), request.getEndTime());

        try {
            String aiResponse = chatService.handleAIChat(
                    userId,
                    request.getPrompts(),
                    request.getRelationId(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            ChatResponse chatResponse = ChatResponse.builder()
                    .chatType(request.getChatType() != null ? request.getChatType() : 0)
                    .data(aiResponse)
                    .build();

            return ApiResponse.success(chatResponse);
        } catch (Exception e) {
            log.error("AI chat failed: userId={}, prompts={}", userId, request.getPrompts(), e);
            return ApiResponse.fail("AI service is temporarily unavailable: " + e.getMessage());
        }
    }
}
