package com.aiwork.helper.service;

import com.aiwork.helper.dto.request.*;
import com.aiwork.helper.dto.response.DepartmentResponse;
import com.aiwork.helper.dto.response.DepartmentTreeResponse;

/**
 * 部门服务接口
 */
public interface DepartmentService {

    /**
     * 获取部门树结构（SOA）
     */
    DepartmentTreeResponse soa();

    /**
     * 获取部门详情
     */
    DepartmentResponse info(String id);

    /**
     * 创建部门
     */
    void create(DepartmentRequest request);

    /**
     * 更新部门
     */
    void edit(DepartmentRequest request);

    /**
     * 删除部门
     */
    void delete(String id);

    /**
     * 设置部门用户
     */
    void setDepartmentUsers(SetDepartmentUsersRequest request);

    /**
     * 添加部门用户（级联到上级部门）
     */
    void addDepartmentUser(AddDepartmentUserRequest request);

    /**
     * 删除部门用户（级联从上级部门删除）
     */
    void removeDepartmentUser(RemoveDepartmentUserRequest request);

    /**
     * 获取用户部门信息（包含完整的上级部门层级）
     */
    DepartmentResponse departmentUserInfo(String userId);
}
