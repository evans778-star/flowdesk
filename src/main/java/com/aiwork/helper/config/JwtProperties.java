package com.aiwork.helper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT配置属性
 */
@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    /**
     * JWT签名密钥
     */
    private String secret = "";

    /**
     * Token过期时间（秒）
     */
    private Long expire = 8640000L; // 100天
}
