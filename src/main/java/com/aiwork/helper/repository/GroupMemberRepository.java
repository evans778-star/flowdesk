package com.aiwork.helper.repository;

import com.aiwork.helper.entity.GroupMember;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 群聊成员关联数据访问接口
 */
@Repository
public interface GroupMemberRepository extends MongoRepository<GroupMember, String> {

    /**
     * 根据群ID查找所有成员关联
     */
    List<GroupMember> findByGroupId(String groupId);

    /**
     * 根据用户ID查找所有群关联
     */
    List<GroupMember> findByUserId(String userId);

    /**
     * 根据群ID和用户ID查找关联
     */
    GroupMember findByGroupIdAndUserId(String groupId, String userId);

    /**
     * 根据群ID删除所有成员关联
     */
    void deleteByGroupId(String groupId);

    /**
     * 根据用户ID删除所有群关联
     */
    void deleteByUserId(String userId);

    /**
     * 根据群ID和用户ID删除关联
     */
    void deleteByGroupIdAndUserId(String groupId, String userId);

    /**
     * 统计群的成员数量
     */
    Long countByGroupId(String groupId);

    /**
     * 统计用户所在的群数量
     */
    Long countByUserId(String userId);

    /**
     * 检查用户是否在群中
     */
    boolean existsByGroupIdAndUserId(String groupId, String userId);
}