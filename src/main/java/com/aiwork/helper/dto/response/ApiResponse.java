package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    /**
     * 响应码：200成功，500失败
     */
    private Integer code;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 成功响应
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(200)
                .data(data)
                .msg("success")
                .build();
    }

    /**
     * 失败响应
     */
    public static <T> ApiResponse<T> fail(String message) {
        return ApiResponse.<T>builder()
                .code(500)
                .data(null)
                .msg(message)
                .build();
    }

    /**
     * 失败响应（默认消息）
     */
    public static <T> ApiResponse<T> fail() {
        return fail("fail");
    }
}
