package com.aiwork.helper.service;

import com.aiwork.helper.dto.request.ApprovalListRequest;
import com.aiwork.helper.dto.request.ApprovalRequest;
import com.aiwork.helper.dto.request.DisposeRequest;
import com.aiwork.helper.dto.response.ApprovalListResponse;
import com.aiwork.helper.dto.response.ApprovalResponse;

/**
 * 审批服务接口
 */
public interface ApprovalService {

    /**
     * 获取审批详情
     */
    ApprovalResponse info(String id);

    /**
     * 创建审批申请
     */
    String create(ApprovalRequest request);

    /**
     * 处理审批（通过/拒绝/撤销）
     */
    void dispose(DisposeRequest request);

    /**
     * 获取审批列表
     */
    ApprovalListResponse list(ApprovalListRequest request);
}
