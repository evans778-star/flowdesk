package com.aiwork.helper.security;

import com.aiwork.helper.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("test-jwt-secret-value-with-at-least-32-bytes");
        properties.setExpire(3600L);

        jwtTokenProvider = new JwtTokenProvider(properties);
        jwtTokenProvider.init();
    }

    @Test
    void generatedTokenCanBeValidatedAndResolvedToUserId() {
        String token = jwtTokenProvider.generateToken("user-123");

        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(token)).isEqualTo("user-123");
    }

    @Test
    void resolveTokenRemovesBearerPrefix() {
        assertThat(jwtTokenProvider.resolveToken("Bearer abc.def.ghi")).isEqualTo("abc.def.ghi");
        assertThat(jwtTokenProvider.resolveToken("abc.def.ghi")).isEqualTo("abc.def.ghi");
        assertThat(jwtTokenProvider.resolveToken(null)).isNull();
    }
}
