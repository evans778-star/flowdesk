package com.aiwork.helper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI聊天请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {

    /**
     * 用户输入内容/提示词
     */
    private String prompts;

    /**
     * 聊天类型
     * 0 = 默认AI聊天
     * 其他值对应特定的业务类型（如待办、审批等）
     */
    private Integer chatType;

    /**
     * 关联ID（如群聊ID、待办ID等）
     */
    private String relationId;

    /**
     * 开始时间（用于查询历史消息）
     */
    private Long startTime;

    /**
     * 结束时间（用于查询历史消息）
     */
    private Long endTime;
}