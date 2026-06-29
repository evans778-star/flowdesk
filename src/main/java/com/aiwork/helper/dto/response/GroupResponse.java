package com.aiwork.helper.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 群聊信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupResponse {

    /**
     * 群ID
     */
    private String groupId;

    /**
     * 群名称
     */
    private String groupName;

    /**
     * 群成员ID列表
     */
    private List<String> memberIds;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 创建时间
     */
    private Long createAt;
}
