package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 修改密码请求
 */
@Data
public class UpdatePasswordRequest {

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String id;

    /**
     * 原密码
     */
    @NotBlank(message = "原密码不能为空")
    private String oldPwd;

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPwd;
}