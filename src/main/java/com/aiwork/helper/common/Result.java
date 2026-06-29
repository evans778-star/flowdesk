package com.aiwork.helper.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一响应结果
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 响应代码 (200-成功, 500-失败)
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
     * 成功代码
     */
    public static final int SUCCESS = 200;

    /**
     * 失败代码
     */
    public static final int ERROR = 500;

    /**
     * 成功消息
     */
    public static final String SUCCESS_MSG = "success";

    /**
     * 失败消息
     */
    public static final String ERROR_MSG = "fail";

    /**
     * 空数据对象
     */
    public static final Map<String, Object> NULL = new HashMap<>();

    public Result() {
    }

    public Result(Integer code, T data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> ok() {
        return new Result<>(SUCCESS, null, SUCCESS_MSG);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS, data, SUCCESS_MSG);
    }

    /**
     * 成功响应（带数据和消息）
     */
    public static <T> Result<T> ok(T data, String msg) {
        return new Result<>(SUCCESS, data, msg);
    }

    /**
     * 失败响应（默认消息）
     */
    public static <T> Result<T> fail() {
        return new Result<>(ERROR, null, ERROR_MSG);
    }

    /**
     * 失败响应（带错误消息）
     */
    public static <T> Result<T> fail(String msg) {
        return new Result<>(ERROR, null, msg);
    }

    /**
     * 失败响应（带错误代码和消息）
     */
    public static <T> Result<T> fail(Integer code, String msg) {
        return new Result<>(code, null, msg);
    }

    /**
     * 自定义响应
     */
    public static <T> Result<T> build(Integer code, T data, String msg) {
        return new Result<>(code, data, msg);
    }
}
