/**
 * @author: 公众号：IT杨秀才
 * @doc:后端，AI知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.security;

import com.aiwork.helper.config.JwtProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * author:  公众号：IT杨秀才
 * 后端，AI知识进阶，后端面试场景题大全：https://golangstar.cn/
 * JWT Token提供者
 * 负责Token的生成和验证
 * 对应Go版本: pkg/token/ctxtoken.go + pkg/token/token.go
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    /**
     * 用户标识键名
     * 对应Go: token.Identify = "aiworkhelper"
     */
    public static final String IDENTIFY = "aiworkhelper";

    /**
     * Authorization头名称
     * 对应Go: token.Authorization
     */
    public static final String AUTHORIZATION = "Authorization";

    /**
     * Bearer前缀
     */
    public static final String BEARER_PREFIX = "Bearer ";

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // 使用配置的密钥生成SecretKey
        this.secretKey = Keys.hmacShaKeyFor(
            jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * 生成JWT Token
     * 对应Go: GetJwtToken(secretKey, iat, seconds, uid)
     *
     * @param userId 用户ID
     * @return JWT Token字符串
     */
    public String generateToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getExpire() * 1000);

        Map<String, Object> claims = new HashMap<>();
        claims.put(IDENTIFY, userId);

        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)  // iat - 签发时间
                .expiration(expiryDate)  // exp - 过期时间
                .signWith(secretKey, Jwts.SIG.HS256)  // 使用HS256算法签名
                .compact();
    }

    /**
     * 从Token中获取用户ID
     * 对应Go: GetUId(ctx)
     *
     * @param token JWT Token
     * @return 用户ID
     */
    public String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get(IDENTIFY, String.class);
    }

    /**
     * 验证Token是否有效
     * 对应Go: ParseToken
     *
     * @param token JWT Token
     * @return 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 解析Token获取Claims
     * 对应Go: ParseToken内部实现
     *
     * @param token JWT Token
     * @return Claims
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从HTTP Authorization头中提取Token
     * 对应Go: extractTokenFromHeader
     *
     * @param bearerToken Authorization头的值 (例如: "Bearer eyJhbGc...")
     * @return 纯Token字符串
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return bearerToken;
    }

    /**
     * 获取Token过期时间戳
     *
     * @return 过期时间戳（秒）
     */
    public Long getExpirationTime() {
        return System.currentTimeMillis() / 1000 + jwtProperties.getExpire();
    }
}
