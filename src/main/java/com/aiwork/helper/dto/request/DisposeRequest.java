package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批处理请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisposeRequest {

    /**
     * 审批ID
     */
    @NotBlank(message = "审批ID不能为空")
    private String approvalId;

    /**
     * 处理状态 (2-通过, 3-拒绝, 4-取消)
     */
    @NotNull(message = "处理状态不能为空")
    private Integer status;

    /**
     * 处理原因/备注
     */
    private String reason;
}
