package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 打卡类型枚举
 * 1.上班卡，2.下班卡
 */
@Getter
public enum WorkCheckType {
    ON_WORK(1, "上班卡"),
    OFF_WORK(2, "下班卡");

    private final int value;
    private final String description;

    WorkCheckType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static WorkCheckType fromValue(int value) {
        for (WorkCheckType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return ON_WORK;
    }
}