package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 待办操作记录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoRecordResponse {

    /**
     * 待办ID
     */
    private String todoId;

    /**
     * 操作用户ID
     */
    private String userId;

    /**
     * 操作用户姓名
     */
    private String userName;

    /**
     * 操作内容
     */
    private String content;

    /**
     * 图片URL
     */
    private String image;

    /**
     * 创建时间
     */
    private Long createAt;
}
