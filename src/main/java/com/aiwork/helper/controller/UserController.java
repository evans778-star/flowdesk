package com.aiwork.helper.controller;

import com.aiwork.helper.common.Result;
import com.aiwork.helper.dto.request.LoginRequest;
import com.aiwork.helper.dto.request.UpdatePasswordRequest;
import com.aiwork.helper.dto.request.UserListRequest;
import com.aiwork.helper.dto.request.UserRequest;
import com.aiwork.helper.dto.response.LoginResponse;
import com.aiwork.helper.dto.response.UserListResponse;
import com.aiwork.helper.dto.response.UserResponse;
import com.aiwork.helper.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "Login, user profile, and user management APIs")
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     *
     * POST /v1/user/login
     */
    @PostMapping("/login")
    @Operation(summary = "Login and issue JWT", description = "Authenticates a local Flowdesk user and returns a JWT token.")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.login(request);
        return Result.ok(response);
    }

    /**
     * 获取用户信息
     *
     * GET /v1/user/{id}
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user profile", description = "Returns basic user information for an authenticated request.")
    public Result<UserResponse> info(@PathVariable String id) {
        UserResponse response = userService.info(id);
        return Result.ok(response);
    }

    /**
     * 创建用户
     *
     * POST /v1/user
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Creates a Flowdesk user. Requires authentication and project-level authorization.")
    public Result<Void> create(@Valid @RequestBody UserRequest request) {
        userService.create(request);
        return Result.ok();
    }

    /**
     * 编辑用户
     *
     * PUT /v1/user
     */
    @PutMapping
    @Operation(summary = "Update user", description = "Updates a Flowdesk user. Requires authentication.")
    public Result<Void> edit(@Valid @RequestBody UserRequest request) {
        userService.edit(request);
        return Result.ok();
    }

    /**
     * 删除用户
     *
     * DELETE /v1/user/{id}
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a Flowdesk user by id. Requires authentication.")
    public Result<Void> delete(@PathVariable String id) {
        userService.delete(id);
        return Result.ok();
    }

    /**
     * 用户列表
     *
     * GET /v1/user/list
     */
    @GetMapping("/list")
    @Operation(summary = "List users", description = "Lists users with optional query parameters.")
    public Result<UserListResponse> list(UserListRequest request) {
        UserListResponse response = userService.list(request);
        return Result.ok(response);
    }

    /**
     * 修改密码
     *
     * POST /v1/user/password
     */
    @PostMapping("/password")
    @Operation(summary = "Update password", description = "Updates the current user's password.")
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(request);
        return Result.ok();
    }
}
