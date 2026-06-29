package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 待办事项响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoResponse {

    /**
     * 待办ID
     */
    private String id;

    /**
     * 创建人ID
     */
    private String creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 标题
     */
    private String title;

    /**
     * 截止时间
     */
    private Long deadlineAt;

    /**
     * 描述
     */
    private String desc;

    /**
     * 待办状态
     */
    private Integer status;

    /**
     * 待办状态
     */
    private Integer todoStatus;

    /**
     * 执行人ID列表
     */
    private List<String> executeIds;

    /**
     * 操作记录列表
     */
    private List<TodoRecordResponse> records;

    /**
     * 创建时间
     */
    private Long createAt;

    /**
     * 更新时间
     */
    private Long updateAt;
}
