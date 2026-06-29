package com.aiwork.helper.controller;

import com.aiwork.helper.common.Result;
import com.aiwork.helper.dto.request.ApprovalListRequest;
import com.aiwork.helper.dto.request.ApprovalRequest;
import com.aiwork.helper.dto.request.DisposeRequest;
import com.aiwork.helper.dto.response.ApprovalListResponse;
import com.aiwork.helper.dto.response.ApprovalResponse;
import com.aiwork.helper.service.ApprovalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 审批控制器
 */
@RestController
@RequestMapping("/v1/approval")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    /**
     * 获取审批详情
     */
    @GetMapping("/{id}")
    public Result<ApprovalResponse> info(@PathVariable String id) {
        ApprovalResponse response = approvalService.info(id);
        return Result.ok(response);
    }

    /**
     * 创建审批申请
     */
    @PostMapping
    public Result<String> create(@Valid @RequestBody ApprovalRequest request) {
        String id = approvalService.create(request);
        return Result.ok(id);
    }

    /**
     * 处理审批（通过/拒绝/撤销）
     */
    @PutMapping("/dispose")
    public Result<Void> dispose(@Valid @RequestBody DisposeRequest request) {
        approvalService.dispose(request);
        return Result.ok();
    }

    /**
     * 获取审批列表
     */
    @GetMapping("/list")
    public Result<ApprovalListResponse> list(@Valid ApprovalListRequest request) {
        ApprovalListResponse response = approvalService.list(request);
        return Result.ok(response);
    }
}