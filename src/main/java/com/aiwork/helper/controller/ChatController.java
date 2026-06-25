package com.aiwork.helper.controller;

import com.aiwork.helper.dto.request.ChatRequest;
import com.aiwork.helper.dto.response.ApiResponse;
import com.aiwork.helper.dto.response.ChatResponse;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 聊天控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    /**
     * AI聊天接口
     *
     * @param request 聊天请求
     * @return 统一响应格式包装的聊天响应
     */
    @PostMapping
    public ApiResponse<ChatResponse> chat(@RequestBody ChatRequest request) {
        // 获取当前登录用户ID
        String userId = SecurityUtils.getCurrentUserId();

        log.info("AI聊天请求: userId={}, prompts={}, chatType={}, relationId={}, startTime={}, endTime={}",
                userId, request.getPrompts(), request.getChatType(), request.getRelationId(),
                request.getStartTime(), request.getEndTime());

        try {
            // 调用AI服务处理聊天
            String aiResponse = chatService.handleAIChat(
                    userId,
                    request.getPrompts(),
                    request.getRelationId(),
                    request.getStartTime(),
                    request.getEndTime()
            );

            // 构建响应
            ChatResponse chatResponse = ChatResponse.builder()
                    .chatType(request.getChatType() != null ? request.getChatType() : 0)
                    .data(aiResponse)
                    .build();

            // 使用统一响应格式包装
            return ApiResponse.success(chatResponse);

        } catch (Exception e) {
            log.error("AI聊天失败: userId={}, prompts={}", userId, request.getPrompts(), e);

            // 返回错误响应
            return ApiResponse.fail("AI服务暂时不可用：" + e.getMessage());
        }
    }
}