package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 部门树结构响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentTreeResponse {

    /**
     * 根部门列表（树形结构）
     */
    private List<DepartmentResponse> child;
}