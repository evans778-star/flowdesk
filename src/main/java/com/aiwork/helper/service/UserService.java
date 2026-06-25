package com.aiwork.helper.service;

import com.aiwork.helper.dto.request.LoginRequest;
import com.aiwork.helper.dto.request.UpdatePasswordRequest;
import com.aiwork.helper.dto.request.UserListRequest;
import com.aiwork.helper.dto.request.UserRequest;
import com.aiwork.helper.dto.response.LoginResponse;
import com.aiwork.helper.dto.response.UserListResponse;
import com.aiwork.helper.dto.response.UserResponse;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 获取用户信息
     */
    UserResponse info(String id);

    /**
     * 创建用户
     */
    void create(UserRequest request);

    /**
     * 编辑用户
     */
    void edit(UserRequest request);

    /**
     * 删除用户
     */
    void delete(String id);

    /**
     * 用户列表
     */
    UserListResponse list(UserListRequest request);

    /**
     * 修改密码
     */
    void updatePassword(UpdatePasswordRequest request);

    /**
     * 初始化系统管理员用户
     */
    void initAdminUser();

    /**
     * 根据用户名获取用户ID
     * @param name 用户名
     * @return 用户ID，如果找不到返回null
     */
    String getUserIdByName(String name);
}
