package com.aiwork.helper.dto.request;

import com.aiwork.helper.dto.response.TodoRecordResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 待办事项请求
 */
@Data
public class TodoRequest {

    /**
     * 待办ID (编辑时需要)
     */
    private String id;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 截止时间 (Unix时间戳-秒)
     */
    private Long deadlineAt;

    /**
     * 描述
     */
    private String desc;

    /**
     * 执行人ID列表
     */
    private List<String> executeIds;

    /**
     * 待办状态 (1-待处理, 2-进行中, 3-已完成, 4-已取消, 5-已超时)
     */
    private Integer status;

    /**
     * 操作记录列表
     */
    private List<TodoRecordResponse> records;
}
