package com.aiwork.helper.dto.response;

import com.aiwork.helper.entity.embedded.GoOut;
import com.aiwork.helper.entity.embedded.Leave;
import com.aiwork.helper.entity.embedded.MakeCard;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 审批列表项响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalListItemResponse {

    /**
     * 审批ID
     */
    private String id;

    /**
     * 审批编号
     */
    private String no;

    /**
     * 审批类型
     */
    private Integer type;

    /**
     * 审批状态
     */
    private Integer status;

    /**
     * 标题
     */
    private String title;

    /**
     * 摘要
     */
    @JsonProperty("abstract")
    private String abstract_;

    /**
     * 创建人ID
     */
    private String createId;

    /**
     * 参与人ID
     */
    private String participatingId;

    /**
     * 创建时间
     */
    private Long createAt;

    /**
     * 请假信息
     */
    private Leave leave;

    /**
     * 补卡信息
     */
    private MakeCard makeCard;

    /**
     * 外出信息
     */
    private GoOut goOut;
}
