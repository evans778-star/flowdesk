package com.aiwork.helper.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 创建群聊请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequest {

    /**
     * 群ID (conversationId)
     */
    @NotBlank(message = "群ID不能为空")
    private String groupId;

    /**
     * 群名称
     */
    @NotBlank(message = "群名称不能为空")
    private String groupName;

    /**
     * 群成员ID列表（不包括创建者）
     */
    @NotEmpty(message = "群成员列表不能为空")
    private List<String> memberIds;
}
