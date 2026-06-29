package com.aiwork.helper.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

class JwtPropertiesTest {

    @Test
    void bindsJwtPropertiesFromEnvironment() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("jwt.secret", "test-jwt-secret-value-with-at-least-32-bytes")
                .withProperty("jwt.expire", "3600");

        JwtProperties properties = Binder.get(environment)
                .bind("jwt", Bindable.of(JwtProperties.class))
                .orElseGet(JwtProperties::new);

        assertThat(properties.getSecret()).isEqualTo("test-jwt-secret-value-with-at-least-32-bytes");
        assertThat(properties.getExpire()).isEqualTo(3600L);
    }
}
