package com.aiwork.helper.dto.request;

import com.aiwork.helper.entity.embedded.GoOut;
import com.aiwork.helper.entity.embedded.Leave;
import com.aiwork.helper.entity.embedded.MakeCard;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    /**
     * 审批ID (编辑时需要)
     */
    private String id;

    /**
     * 审批类型
     */
    @NotNull(message = "审批类型不能为空")
    private Integer type;

    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;

    /**
     * 摘要
     */
    private String abstract_;

    /**
     * 原因
     */
    private String reason;

    /**
     * 审批人ID列表
     */
    private java.util.List<String> approverIds;

    /**
     * 抄送人ID列表
     */
    private java.util.List<String> copyPersonIds;

    /**
     * 补卡信息
     */
    private MakeCard makeCard;

    /**
     * 请假信息
     */
    private Leave leave;

    /**
     * 外出信息
     */
    private GoOut goOut;
}
