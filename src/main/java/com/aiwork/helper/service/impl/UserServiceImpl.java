package com.aiwork.helper.service.impl;

import com.aiwork.helper.dto.request.LoginRequest;
import com.aiwork.helper.dto.request.UpdatePasswordRequest;
import com.aiwork.helper.dto.request.UserListRequest;
import com.aiwork.helper.dto.request.UserRequest;
import com.aiwork.helper.dto.response.LoginResponse;
import com.aiwork.helper.dto.response.UserListResponse;
import com.aiwork.helper.dto.response.UserResponse;
import com.aiwork.helper.entity.User;
import com.aiwork.helper.exception.BusinessException;
import com.aiwork.helper.repository.UserRepository;
import com.aiwork.helper.security.JwtTokenProvider;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SecurityProperties securityProperties;

    @Value("${flowdesk.user.default-password:}")
    private String defaultUserPassword;

    /**
     * 系统启动时初始化管理员用户
     */
    @PostConstruct
    @Override
    public void initAdminUser() {
        // 检查是否已存在管理员用户
        if (userRepository.findByIsAdminTrue().isPresent()) {
            log.info("Admin user already exists");
            return;
        }

        // 创建默认管理员用户
        String adminName = securityProperties.getUser().getName();
        String adminPassword = securityProperties.getUser().getPassword();
        if (!StringUtils.hasText(adminName) || !StringUtils.hasText(adminPassword)) {
            throw new IllegalStateException("Admin username and password must be configured");
        }

        User admin = new User();
        admin.setName(adminName);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setStatus(0);
        admin.setIsAdmin(true);
        admin.setCreateAt(System.currentTimeMillis() / 1000);
        admin.setUpdateAt(System.currentTimeMillis() / 1000);

        userRepository.save(admin);
        log.info("Admin user initialized successfully");
    }

    /**
     * 用户登录
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        // 根据用户名查找用户
        User user = userRepository.findByName(request.getName())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }

        // 生成JWT Token
        String token = jwtTokenProvider.generateToken(user.getId());
        Long expireTime = jwtTokenProvider.getExpirationTime();

        // 返回登录响应
        return LoginResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .token(token)
                .accessExpire(expireTime)
                .build();
    }

    /**
     * 获取用户信息
     */
    @Override
    public UserResponse info(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        return convertToResponse(user);
    }

    /**
     * 创建用户
     */
    @Override
    public void create(UserRequest request) {
        // 检查用户名是否已存在
        if (userRepository.findByName(request.getName()).isPresent()) {
            throw new BusinessException("已存在该用户");
        }

        // 设置默认密码
        String password = StringUtils.hasText(request.getPassword()) ?
                request.getPassword() : defaultUserPassword;
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("Default user password is not configured");
        }

        // 创建用户实体
        User user = new User();
        user.setName(request.getName());
        user.setPassword(passwordEncoder.encode(password));
        user.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        user.setIsAdmin(false);
        user.setCreateAt(System.currentTimeMillis() / 1000);
        user.setUpdateAt(System.currentTimeMillis() / 1000);

        userRepository.save(user);
    }

    /**
     * 编辑用户
     */
    @Override
    public void edit(UserRequest request) {
        // 查找用户
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 如果用户名有变化，检查新用户名是否已被使用
        if (StringUtils.hasText(request.getName()) &&
                !request.getName().equals(user.getName())) {
            userRepository.findByName(request.getName()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(request.getId())) {
                    throw new BusinessException("用户名已被占用");
                }
            });
            user.setName(request.getName());
        }

        // 更新密码
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        // 更新状态
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }

        user.setUpdateAt(System.currentTimeMillis() / 1000);
        userRepository.save(user);
    }

    /**
     * 删除用户
     */
    @Override
    public void delete(String id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    /**
     * 用户列表查询
     */
    @Override
    public UserListResponse list(UserListRequest request) {
        List<User> users;
        long count;

        // 根据条件查询
        if (request.getIds() != null && !request.getIds().isEmpty()) {
            // 按ID列表查询
            users = userRepository.findByIdIn(request.getIds());
            count = users.size();
        } else if (StringUtils.hasText(request.getName())) {
            // 按用户名查询
            users = List.of(userRepository.findByName(request.getName())
                    .orElseThrow(() -> new BusinessException("用户不存在")));
            count = 1;
        } else {
            // 分页查询所有用户
            Pageable pageable = PageRequest.of(
                    request.getPage() - 1,
                    request.getCount()
            );
            var page = userRepository.findAll(pageable);
            users = page.getContent();
            count = page.getTotalElements();
        }

        // 转换为响应对象
        List<UserResponse> userResponses = users.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return UserListResponse.builder()
                .count(count)
                .data(userResponses)
                .build();
    }

    /**
     * 修改密码
     */
    @Override
    public void updatePassword(UpdatePasswordRequest request) {
        // 查找用户
        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPwd(), user.getPassword())) {
            throw new BusinessException("原密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPwd()));
        user.setUpdateAt(System.currentTimeMillis() / 1000);

        userRepository.save(user);
    }

    /**
     * 转换User实体为UserResponse
     */
    private UserResponse convertToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .status(user.getStatus())
                .isAdmin(user.getIsAdmin())
                .createAt(user.getCreateAt())
                .updateAt(user.getUpdateAt())
                .build();
    }

    @Override
    public String getUserIdByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        log.debug("根据用户名查询ID: name={}", name);

        Optional<User> userOptional = userRepository.findByName(name.trim());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            log.debug("找到用户: name={}, id={}", name, user.getId());
            return user.getId();
        }

        log.debug("未找到用户: name={}", name);
        return null;
    }
}
