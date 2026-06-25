package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 删除部门用户请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RemoveDepartmentUserRequest {

    /**
     * 部门ID
     */
    @NotBlank(message = "部门ID不能为空")
    private String depId;

    /**
     * 用户ID
     */
    @NotBlank(message = "用户ID不能为空")
    private String userId;
}
