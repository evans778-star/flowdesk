package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审批列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalListResponse {

    /**
     * 总记录数
     */
    private Long count;

    /**
     * 审批列表
     */
    private List<ApprovalListItemResponse> data;
}
