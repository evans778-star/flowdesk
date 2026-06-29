package com.aiwork.helper.service.impl;

import com.aiwork.helper.entity.GroupMember;
import com.aiwork.helper.repository.GroupMemberRepository;
import com.aiwork.helper.service.GroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 群聊管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {

    private final GroupMemberRepository groupMemberRepository;

    @Override
    public List<String> getGroupMemberIds(String groupId) {
        log.debug("获取群聊成员: groupId={}", groupId);

        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);

        return members.stream()
                .map(GroupMember::getUserId)
                .collect(Collectors.toList());
    }

    @Override
    public void addMember(String groupId, String userId) {
        log.info("添加群聊成员: groupId={}, userId={}", groupId, userId);

        // 检查是否已存在
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            log.debug("用户已在群中: groupId={}, userId={}", groupId, userId);
            return;
        }

        long currentTime = System.currentTimeMillis() / 1000;

        GroupMember member = new GroupMember();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setCreateAt(currentTime);
        member.setUpdateAt(currentTime);

        groupMemberRepository.save(member);
        log.info("成功添加群聊成员: groupId={}, userId={}", groupId, userId);
    }

    @Override
    public void removeMember(String groupId, String userId) {
        log.info("移除群聊成员: groupId={}, userId={}", groupId, userId);
        groupMemberRepository.deleteByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public boolean isMember(String groupId, String userId) {
        return groupMemberRepository.existsByGroupIdAndUserId(groupId, userId);
    }

    @Override
    public Long getMemberCount(String groupId) {
        return groupMemberRepository.countByGroupId(groupId);
    }

    @Override
    public void addMembers(String groupId, List<String> userIds) {
        log.info("批量添加群聊成员: groupId={}, count={}", groupId, userIds.size());

        long currentTime = System.currentTimeMillis() / 1000;

        for (String userId : userIds) {
            // 检查是否已存在
            if (groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
                log.debug("用户已在群中，跳过: groupId={}, userId={}", groupId, userId);
                continue;
            }

            GroupMember member = new GroupMember();
            member.setGroupId(groupId);
            member.setUserId(userId);
            member.setCreateAt(currentTime);
            member.setUpdateAt(currentTime);

            groupMemberRepository.save(member);
        }

        log.info("批量添加群聊成员完成: groupId={}, count={}", groupId, userIds.size());
    }
}