package com.aiwork.helper.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 用户列表查询请求
 */
@Data
public class UserListRequest {

    /**
     * 用户ID列表
     */
    private List<String> ids;

    /**
     * 用户名模糊搜索
     */
    private String name;

    /**
     * 页码 (从1开始)
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer count = 10;
}