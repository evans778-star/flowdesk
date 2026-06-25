package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 审批状态枚举
 * 0.未开始，1.进行中，2.通过，3.拒绝，4.撤销，5.自动通过
 */
@Getter
public enum ApprovalStatus {
    NOT_STARTED(0, "未开始"),
    PROCESSED(1, "处理中"),
    PASS(2, "通过"),
    REFUSE(3, "拒绝"),
    CANCEL(4, "撤销"),
    AUTO_PASS(5, "自动通过");

    private final int value;
    private final String description;

    ApprovalStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ApprovalStatus fromValue(int value) {
        for (ApprovalStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return NOT_STARTED;
    }
}
