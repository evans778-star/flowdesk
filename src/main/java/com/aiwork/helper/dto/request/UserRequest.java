package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 用户请求
 */
@Data
public class UserRequest {

    /**
     * 用户ID (编辑时需要)
     */
    private String id;

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String name;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态 (0-禁用, 1-启用)
     */
    private Integer status;
}
