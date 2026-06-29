package com.aiwork.helper.entity.enums;

import lombok.Getter;

/**
 * 请假类型枚举
 * 1.事假, 2.调休, 3.病假, 4.年假, 5.产假, 6.陪产假, 7.婚假, 8.丧假, 9.哺乳假
 */
@Getter
public enum LeaveType {
    MATTER(1, "事假"),
    REST(2, "调休"),
    FALL(3, "病假"),
    ANNUAL(4, "年假"),
    MATERNITY(5, "产假"),
    PATERNITY(6, "陪产假"),
    MARRIAGE(7, "婚假"),
    FUNERAL(8, "丧假"),
    BREASTFEEDING(9, "哺乳假");

    private final int value;
    private final String description;

    LeaveType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static LeaveType fromValue(int value) {
        for (LeaveType type : values()) {
            if (type.value == value) {
                return type;
            }
        }
        return MATTER;
    }
}
