package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 完成待办请求
 */
@Data
public class FinishTodoRequest {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;

    /**
     * 待办ID
     */
    @NotBlank(message = "待办ID不能为空")
    private String todoId;
}
