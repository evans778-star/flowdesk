package com.aiwork.helper.service.impl;

import com.aiwork.helper.config.JwtProperties;
import com.aiwork.helper.dto.request.LoginRequest;
import com.aiwork.helper.dto.response.LoginResponse;
import com.aiwork.helper.entity.User;
import com.aiwork.helper.exception.BusinessException;
import com.aiwork.helper.repository.UserRepository;
import com.aiwork.helper.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider jwtTokenProvider;
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();

        JwtProperties jwtProperties = new JwtProperties();
        jwtProperties.setSecret("test-jwt-secret-value-with-at-least-32-bytes");
        jwtProperties.setExpire(3600L);
        jwtTokenProvider = new JwtTokenProvider(jwtProperties);
        jwtTokenProvider.init();

        SecurityProperties securityProperties = new SecurityProperties();
        userService = new UserServiceImpl(userRepository, passwordEncoder, jwtTokenProvider, securityProperties);
    }

    @Test
    void loginReturnsTokenForValidCredentials() {
        User user = new User();
        user.setId("user-123");
        user.setName("alice");
        user.setPassword(passwordEncoder.encode("correct-password"));

        when(userRepository.findByName("alice")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setName("alice");
        request.setPassword("correct-password");

        LoginResponse response = userService.login(request);

        assertThat(response.getId()).isEqualTo("user-123");
        assertThat(response.getName()).isEqualTo("alice");
        assertThat(response.getToken()).isNotBlank();
        assertThat(jwtTokenProvider.validateToken(response.getToken())).isTrue();
        assertThat(jwtTokenProvider.getUserIdFromToken(response.getToken())).isEqualTo("user-123");
    }

    @Test
    void loginRejectsWrongPassword() {
        User user = new User();
        user.setId("user-123");
        user.setName("alice");
        user.setPassword(passwordEncoder.encode("correct-password"));

        when(userRepository.findByName("alice")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest();
        request.setName("alice");
        request.setPassword("wrong-password");

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("密码错误");
    }
}
