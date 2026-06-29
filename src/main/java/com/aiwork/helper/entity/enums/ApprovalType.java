package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 审批类型枚举
 * 1.通用，2.请假，3.补卡，4.外出，5.报销，6.付款，7.采购，8.收款，9.转正，10.离职，11.加班，12.合同
 */
@Getter
public enum ApprovalType {
    UNIVERSAL(1, "通用审批"),
    LEAVE(2, "请假审批"),
    MAKE_CARD(3, "补卡审批"),
    GO_OUT(4, "外出审批"),
    REIMBURSE(5, "报销审批"),
    PAYMENT(6, "付款审批"),
    BUYER(7, "采购审批"),
    PROCEEDS(8, "收款审批"),
    POSITIVE(9, "转正审批"),
    DIMISSION(10, "离职审批"),
    OVERTIME(11, "加班审批"),
    BUYER_CONTRACT(12, "采购合同审批");

    private final int value;
    private final String description;

    ApprovalType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ApprovalType fromValue(int value) {
        for (ApprovalType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return UNIVERSAL;
    }
}
