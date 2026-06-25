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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     *
     * POST /v1/user/login
     */
    @PostMapping("/login")
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
    public Result<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(request);
        return Result.ok();
    }
}
