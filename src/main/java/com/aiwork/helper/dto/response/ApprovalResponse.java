package com.aiwork.helper.dto.response;

import com.aiwork.helper.entity.embedded.Approver;
import com.aiwork.helper.entity.embedded.GoOut;
import com.aiwork.helper.entity.embedded.Leave;
import com.aiwork.helper.entity.embedded.MakeCard;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 审批详情响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {

    /**
     * 审批ID
     */
    private String id;

    /**
     * 申请人信息
     */
    private Approver user;

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
     * 原因
     */
    private String reason;

    /**
     * 当前审批人
     */
    private Approver approver;

    /**
     * 所有审批人列表
     */
    private List<Approver> approvers;

    /**
     * 抄送人列表
     */
    private List<Approver> copyPersons;

    /**
     * 完成时间
     */
    private Long finishAt;

    /**
     * 完成日期 (yyyyMMdd)
     */
    private Long finishDay;

    /**
     * 完成月份 (yyyyMM)
     */
    private Long finishMonth;

    /**
     * 完成年份 (yyyy)
     */
    private Long finishYeas;

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

    /**
     * 更新时间
     */
    private Long updateAt;

    /**
     * 创建时间
     */
    private Long createAt;
}
