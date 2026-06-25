package com.aiwork.helper.controller;

import com.aiwork.helper.common.Result;
import com.aiwork.helper.dto.request.AddGroupMembersRequest;
import com.aiwork.helper.dto.request.CreateGroupRequest;
import com.aiwork.helper.dto.response.GroupResponse;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 群聊管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/v1/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    /**
     * 创建群聊
     * POST /v1/group/create
     */
    @PostMapping("/create")
    public Result<Void> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        // 从Spring Security上下文中获取当前用户ID
        String userId = SecurityUtils.getCurrentUserId();

        log.info("创建群聊: groupId={}, groupName={}, creatorId={}, memberCount={}",
                request.getGroupId(), request.getGroupName(), userId, request.getMemberIds().size());

        // 将创建者也添加到群成员中
        groupService.addMember(request.getGroupId(), userId);

        // 添加其他成员
        groupService.addMembers(request.getGroupId(), request.getMemberIds());

        return Result.ok();
    }

    /**
     * 获取群成员列表
     * GET /v1/group/{groupId}/members
     */
    @GetMapping("/{groupId}/members")
    public Result<List<String>> getGroupMembers(@PathVariable String groupId) {
        log.info("获取群成员列表: groupId={}", groupId);

        List<String> memberIds = groupService.getGroupMemberIds(groupId);

        return Result.ok(memberIds);
    }

    /**
     * 添加群成员
     * POST /v1/group/members/add
     */
    @PostMapping("/members/add")
    public Result<Void> addMembers(@Valid @RequestBody AddGroupMembersRequest request) {
        log.info("添加群成员: groupId={}, memberCount={}", request.getGroupId(), request.getMemberIds().size());

        groupService.addMembers(request.getGroupId(), request.getMemberIds());

        return Result.ok();
    }

    /**
     * 移除群成员
     * DELETE /v1/group/{groupId}/members/{userId}
     */
    @DeleteMapping("/{groupId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable String groupId, @PathVariable String userId) {
        log.info("移除群成员: groupId={}, userId={}", groupId, userId);

        groupService.removeMember(groupId, userId);

        return Result.ok();
    }

    /**
     * 检查用户是否在群中
     * GET /v1/group/{groupId}/members/{userId}/exists
     */
    @GetMapping("/{groupId}/members/{userId}/exists")
    public Result<Boolean> isMember(@PathVariable String groupId, @PathVariable String userId) {
        log.debug("检查用户是否在群中: groupId={}, userId={}", groupId, userId);

        boolean isMember = groupService.isMember(groupId, userId);

        return Result.ok(isMember);
    }

    /**
     * 获取群成员数量
     * GET /v1/group/{groupId}/count
     */
    @GetMapping("/{groupId}/count")
    public Result<Long> getMemberCount(@PathVariable String groupId) {
        log.debug("获取群成员数量: groupId={}", groupId);

        Long count = groupService.getMemberCount(groupId);

        return Result.ok(count);
    }
}
