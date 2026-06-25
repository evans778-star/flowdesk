package com.aiwork.helper.service;

import java.util.List;

/**
 * 群聊管理服务接口
 * 提供群聊成员的管理功能
 */
public interface GroupService {

    /**
     * 获取群聊的所有成员ID列表
     * @param groupId 群ID (conversationId)
     * @return 成员用户ID列表
     */
    List<String> getGroupMemberIds(String groupId);

    /**
     * 添加用户到群聊
     * @param groupId 群ID
     * @param userId 用户ID
     */
    void addMember(String groupId, String userId);

    /**
     * 从群聊中移除用户
     * @param groupId 群ID
     * @param userId 用户ID
     */
    void removeMember(String groupId, String userId);

    /**
     * 检查用户是否在群中
     * @param groupId 群ID
     * @param userId 用户ID
     * @return 是否在群中
     */
    boolean isMember(String groupId, String userId);

    /**
     * 获取群成员数量
     * @param groupId 群ID
     * @return 成员数量
     */
    Long getMemberCount(String groupId);

    /**
     * 批量添加用户到群聊
     * @param groupId 群ID
     * @param userIds 用户ID列表
     */
    void addMembers(String groupId, List<String> userIds);
}
