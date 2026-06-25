package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 待办列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoListResponse {

    /**
     * 总记录数
     */
    private Long count;

    /**
     * 待办列表
     */
    private List<TodoResponse> data;
}
