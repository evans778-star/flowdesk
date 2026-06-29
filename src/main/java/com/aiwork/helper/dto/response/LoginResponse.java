package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    /**
     * 用户ID
     */
    private String id;

    /**
     * 用户名
     */
    private String name;

    /**
     * JWT Token
     */
    private String token;

    /**
     * Token过期时间戳（秒）
     */
    private Long accessExpire;

    /**
     * Token刷新时间戳（秒）
     */
    private Long refreshAfter;
}