package com.aiwork.helper.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批列表请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalListRequest {

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 操作类型 (1=我提交的, 2=待我审批的)
     */
    private Integer type;

    /**
     * 页码
     */
    private Integer page;

    /**
     * 每页数量
     */
    private Integer count;
}