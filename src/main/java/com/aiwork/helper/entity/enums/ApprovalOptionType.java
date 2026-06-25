package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 审批操作类型枚举
 * 1 我提交、2 待我审批
 */
@Getter
public enum ApprovalOptionType {

    /**
     * 我提交的
     */
    SUBMIT(1, "我提交的"),

    /**
     * 待我审批的
     */
    AUDIT(2, "待我审批的");

    private final Integer value;
    private final String description;

    ApprovalOptionType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ApprovalOptionType fromValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (ApprovalOptionType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
}
