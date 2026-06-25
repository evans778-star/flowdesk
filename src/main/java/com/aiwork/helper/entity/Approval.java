package com.aiwork.helper.entity;

import com.aiwork.helper.entity.embedded.Approver;
import com.aiwork.helper.entity.embedded.GoOut;
import com.aiwork.helper.entity.embedded.Leave;
import com.aiwork.helper.entity.embedded.MakeCard;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 审批实体类
 */
@Data
@Document(collection = "approval")
public class Approval {

    @Id
    private String id;

    /**
     * 申请人用户ID
     */
    @Field("userId")
    private String userId;

    /**
     * 审批编号
     */
    @Field("no")
    private String no;

    /**
     * 审批类型
     */
    @Field("type")
    private Integer type;

    /**
     * 审批状态
     */
    @Field("status")
    private Integer status;

    /**
     * 审批标题
     */
    @Field("title")
    private String title;

    /**
     * 审批摘要
     */
    @Field("abstract")
    private String abstract_;

    /**
     * 申请理由
     */
    @Field("reason")
    private String reason;

    /**
     * 当前审批人ID
     */
    @Field("approvalId")
    private String approvalId;

    /**
     * 当前审批人索引
     */
    @Field("approvalIdx")
    private Integer approvalIdx;

    /**
     * 审批人列表
     */
    @Field("approvers")
    private List<Approver> approvers;

    /**
     * 抄送人列表
     */
    @Field("copyPersons")
    private List<Approver> copyPersons;

    /**
     * 参与人员ID列表
     */
    @Field("participation")
    private List<String> participation;

    /**
     * 完成时间戳
     */
    @Field("finishAt")
    private Long finishAt;

    /**
     * 完成日期
     */
    @Field("finishDay")
    private Long finishDay;

    /**
     * 完成月份
     */
    @Field("finishMonth")
    private Long finishMonth;

    /**
     * 完成年份
     */
    @Field("finishYeas")
    private Long finishYeas;

    /**
     * 补卡申请详情
     */
    @Field("makeCard")
    private MakeCard makeCard;

    /**
     * 请假申请详情
     */
    @Field("leave")
    private Leave leave;

    /**
     * 外出申请详情
     */
    @Field("goOut")
    private GoOut goOut;

    /**
     * 更新时间戳
     */
    @Field("updateAt")
    private Long updateAt;

    /**
     * 创建时间戳
     */
    @Field("createAt")
    private Long createAt;
}