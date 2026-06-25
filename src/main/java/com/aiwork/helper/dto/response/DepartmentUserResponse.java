package com.aiwork.helper.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门用户响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentUserResponse {

    /**
     * 关联ID
     */
    private String id;

    /**
     * 用户ID
     */
    @JsonProperty("user")
    private String userId;

    /**
     * 部门ID
     */
    @JsonProperty("dep")
    private String depId;

    /**
     * 用户姓名
     */
    private String userName;
}