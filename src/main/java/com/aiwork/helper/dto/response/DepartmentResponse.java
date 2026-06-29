package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 部门响应（包含树形结构）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponse {

    /**
     * 部门ID
     */
    private String id;

    /**
     * 部门名称
     */
    private String name;

    /**
     * 父部门ID
     */
    private String parentId;

    /**
     * 父部门路径
     */
    private String parentPath;

    /**
     * 部门层级
     */
    private Integer level;

    /**
     * 部门负责人ID
     */
    private String leaderId;

    /**
     * 部门负责人姓名
     */
    private String leader;

    /**
     * 部门人数
     */
    private Long count;

    /**
     * 部门用户列表
     */
    private List<DepartmentUserResponse> users;

    /**
     * 子部门列表
     */
    private List<DepartmentResponse> child;
}