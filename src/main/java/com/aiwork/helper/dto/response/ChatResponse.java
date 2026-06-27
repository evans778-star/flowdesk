package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * AI聊天响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {

    /**
     * 聊天类型
     */
    private Integer chatType;

    /**
     * 响应数据
     */
    private Object data;

    @Builder.Default
    private List<CitationResponse> citations = List.of();
}
