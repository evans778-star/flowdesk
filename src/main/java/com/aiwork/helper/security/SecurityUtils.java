package com.aiwork.helper.security;

import com.aiwork.helper.security.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 安全工具类
 * 提供获取当前用户信息的便捷方法
 */
public class SecurityUtils {

    /**
     * 获取当前登录用户ID
     *
     * @return 用户ID，未登录返回null
     */
    public static String getCurrentUserId() {
        // 方法1: 从Spring Security上下文获取
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }

        // 方法2: 从请求属性获取（备用方案）
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object userId = request.getAttribute(JwtTokenProvider.IDENTIFY);
            if (userId instanceof String) {
                return (String) userId;
            }
        }

        return null;
    }

    /**
     * 获取当前请求的JWT Token
     *
     * @return JWT Token字符串
     */
    public static String getCurrentToken() {
        ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Object token = request.getAttribute(JwtTokenProvider.AUTHORIZATION);
            if (token instanceof String) {
                return (String) token;
            }
        }
        return null;
    }

    /**
     * 检查当前用户是否已认证
     *
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null;
    }
}
