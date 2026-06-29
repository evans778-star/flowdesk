package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 待办事项状态枚举
 */
@Getter
public enum TodoStatus {
    PENDING(1, "待处理"),
    IN_PROGRESS(2, "进行中"),
    FINISHED(3, "已完成"),
    CANCELLED(4, "已取消"),
    TIMEOUT(5, "已超时");

    private final int value;
    private final String description;

    TodoStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TodoStatus fromValue(int value) {
        for (TodoStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return PENDING;
    }
}