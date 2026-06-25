package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 设置部门用户请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetDepartmentUsersRequest {

    /**
     * 部门ID
     */
    @NotBlank(message = "部门ID不能为空")
    private String depId;

    /**
     * 用户ID列表
     */
    @NotNull(message = "用户ID列表不能为空")
    private List<String> userIds;
}
