package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequest {

    /**
     * 部门ID (编辑时需要)
     */
    private String id;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    private String name;

    /**
     * 父部门ID
     */
    private String parentId;

    /**
     * 部门层级
     */
    private Integer level;

    /**
     * 部门负责人ID
     */
    @NotBlank(message = "部门负责人不能为空")
    private String leaderId;
}
