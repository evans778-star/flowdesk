package com.aiwork.helper.exception;

import lombok.Getter;

/**
 * 业务异常类
 * 用于业务逻辑层抛出的异常
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * 错误代码
     */
    private Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public BusinessException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
