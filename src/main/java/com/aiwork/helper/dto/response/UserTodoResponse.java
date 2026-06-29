package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户待办响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserTodoResponse {

    /**
     * ID
     */
    private String id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 用户姓名
     */
    private String userName;

    /**
     * 待办ID
     */
    private String todoId;

    /**
     * 待办状态
     */
    private Integer todoStatus;
}
