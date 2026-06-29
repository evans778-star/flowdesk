package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 待办操作记录请求
 */
@Data
public class TodoRecordRequest {

    /**
     * 待办ID
     */
    @NotBlank(message = "待办ID不能为空")
    private String todoId;

    /**
     * 记录内容
     */
    @NotBlank(message = "记录内容不能为空")
    private String content;

    /**
     * 图片URL
     */
    private String image;
}
