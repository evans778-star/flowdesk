package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 时间格式类型枚举
 * 1.小时，2.天
 */
@Getter
public enum TimeFormatType {
    HOUR(1, "小时"),
    DAY(2, "天");

    private final int value;
    private final String description;

    TimeFormatType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TimeFormatType fromValue(int value) {
        for (TimeFormatType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return HOUR;
    }
}