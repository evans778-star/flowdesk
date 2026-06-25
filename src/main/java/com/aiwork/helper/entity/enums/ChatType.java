package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 聊天类型枚举
 * 1.群聊，2.私聊
 */
@Getter
public enum ChatType {
    GROUP(1, "群聊"),
    SINGLE(2, "私聊");

    private final int value;
    private final String description;

    ChatType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static ChatType fromValue(int value) {
        for (ChatType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return GROUP;
    }
}