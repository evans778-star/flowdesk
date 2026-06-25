package com.aiwork.helper.service.impl;

import com.aiwork.helper.dto.request.*;
import com.aiwork.helper.dto.response.DepartmentResponse;
import com.aiwork.helper.dto.response.DepartmentTreeResponse;
import com.aiwork.helper.dto.response.DepartmentUserResponse;
import com.aiwork.helper.entity.Department;
import com.aiwork.helper.entity.DepartmentUser;
import com.aiwork.helper.entity.User;
import com.aiwork.helper.exception.BusinessException;
import com.aiwork.helper.repository.DepartmentRepository;
import com.aiwork.helper.repository.DepartmentUserRepository;
import com.aiwork.helper.repository.UserRepository;
import com.aiwork.helper.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DepartmentUserRepository departmentUserRepository;
    private final UserRepository userRepository;

    @Override
    public DepartmentTreeResponse soa() {
        // 获取所有部门
        List<Department> allDepartments = departmentRepository.findAll();

        // 收集所有负责人ID
        Set<String> leaderIds = allDepartments.stream()
                .map(Department::getLeaderId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        // 批量查询所有负责人信息
        Map<String, String> leaderMap = new HashMap<>();
        if (!leaderIds.isEmpty()) {
            List<User> leaders = userRepository.findByIdIn(new ArrayList<>(leaderIds));
            leaderMap = leaders.stream()
                    .collect(Collectors.toMap(User::getId, User::getName));
        }

        // 查询所有部门用户关联
        List<DepartmentUser> allDepUsers = departmentUserRepository.findAll();

        // 收集所有用户ID
        Set<String> allUserIds = allDepUsers.stream()
                .map(DepartmentUser::getUserId)
                .collect(Collectors.toSet());

        // 批量查询所有用户信息
        Map<String, String> userMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            List<User> users = userRepository.findByIdIn(new ArrayList<>(allUserIds));
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getName));
        }

        // 统计每个部门的用户数量和用户列表
        Map<String, Long> depUserCountMap = new HashMap<>();
        Map<String, List<DepartmentUserResponse>> depUsersMap = new HashMap<>();

        final Map<String, String> finalUserMap = userMap;
        for (DepartmentUser depUser : allDepUsers) {
            depUserCountMap.merge(depUser.getDepId(), 1L, Long::sum);

            String userName = finalUserMap.get(depUser.getUserId());
            DepartmentUserResponse userResp = DepartmentUserResponse.builder()
                    .id(depUser.getId())
                    .userId(depUser.getUserId())
                    .depId(depUser.getDepId())
                    .userName(userName != null ? userName : "")
                    .build();

            depUsersMap.computeIfAbsent(depUser.getDepId(), k -> new ArrayList<>()).add(userResp);
        }

        // 按父路径分组部门
        Map<String, List<DepartmentResponse>> groupDep = new HashMap<>();
        List<DepartmentResponse> rootDep = new ArrayList<>();

        final Map<String, String> finalLeaderMap = leaderMap;
        for (Department dep : allDepartments) {
            DepartmentResponse depResp = buildDepartmentResponse(dep);

            // 填充负责人名称
            if (finalLeaderMap.containsKey(dep.getLeaderId())) {
                depResp.setLeader(finalLeaderMap.get(dep.getLeaderId()));
            }

            // 填充部门成员数量
            depResp.setCount(depUserCountMap.getOrDefault(dep.getId(), 0L));

            // 填充部门用户列表
            depResp.setUsers(depUsersMap.getOrDefault(dep.getId(), new ArrayList<>()));

            if (dep.getParentPath() == null || dep.getParentPath().isEmpty()) {
                // 根部门
                rootDep.add(depResp);
            } else {
                // 子部门
                groupDep.computeIfAbsent(dep.getParentPath(), k -> new ArrayList<>()).add(depResp);
            }
        }

        // 构建部门树
        buildTree(rootDep, groupDep);

        return DepartmentTreeResponse.builder()
                .child(rootDep)
                .build();
    }

    @Override
    public DepartmentResponse info(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("部门不存在"));

        User leader = userRepository.findById(department.getLeaderId())
                .orElseThrow(() -> new BusinessException("部门负责人不存在"));

        DepartmentResponse response = buildDepartmentResponse(department);
        response.setLeader(leader.getName());

        return response;
    }

    @Override
    public void create(DepartmentRequest request) {
        // 检查部门名称是否已存在
        Department existingDep = departmentRepository.findByName(request.getName());
        if (existingDep != null) {
            throw new BusinessException("已存在该部门");
        }

        // 构建父路径
        String parentPath = "";
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            Department parentDep = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("父部门不存在"));
            parentPath = buildParentPath(parentDep.getParentPath(), request.getParentId());
        }

        // 创建部门
        long currentTime = System.currentTimeMillis() / 1000;
        Department department = new Department();
        department.setName(request.getName());
        department.setParentId(request.getParentId());
        department.setParentPath(parentPath);
        department.setLevel(request.getLevel() != null ? request.getLevel() : 0);
        department.setLeaderId(request.getLeaderId());
        department.setCount(1L); // 创建时默认有1个成员（负责人）
        department.setCreateAt(currentTime);

        Department saved = departmentRepository.save(department);
        log.info("create department success, id: {}", saved.getId());

        // 将负责人添加到部门（会级联到所有父部门）
        addDepartmentUser(AddDepartmentUserRequest.builder()
                .depId(saved.getId())
                .userId(request.getLeaderId())
                .build());
    }

    @Override
    public void edit(DepartmentRequest request) {
        if (request.getId() == null || request.getId().isEmpty()) {
            throw new BusinessException("部门ID不能为空");
        }

        Department department = departmentRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("部门不存在"));

        // 检查新名称是否重复
        Department existingDep = departmentRepository.findByName(request.getName());
        if (existingDep != null && !existingDep.getId().equals(request.getId())) {
            throw new BusinessException("已存在该部门");
        }

        department.setName(request.getName());
        department.setParentId(request.getParentId());
        department.setLevel(request.getLevel() != null ? request.getLevel() : department.getLevel());
        department.setLeaderId(request.getLeaderId());

        departmentRepository.save(department);
        log.info("edit department success, id: {}", request.getId());
    }

    @Override
    public void delete(String id) {
        Department department = departmentRepository.findById(id)
                .orElse(null);

        if (department == null) {
            // 部门不存在，视为删除成功
            return;
        }

        // 查找部门下的用户
        List<DepartmentUser> depUsers = departmentUserRepository.findByDepId(id);

        if (depUsers.isEmpty()) {
            // 部门下没有用户，可以直接删除
            departmentRepository.deleteById(id);
            log.info("delete department success, id: {}", id);
            return;
        }

        // 检查是否只有负责人
        if (depUsers.size() > 1 ||
                !depUsers.get(0).getUserId().equals(department.getLeaderId())) {
            throw new BusinessException("该部门下还存在用户，不能删除该部门");
        }

        // 只有负责人的情况下，删除部门前需要：
        // 1. 智能删除负���人（会检查是否在其他部门）
        // 2. 然后删除部门本身

        String leaderId = department.getLeaderId();

        // 删除负责人在当前部门的关联
        for (DepartmentUser du : depUsers) {
            if (du.getUserId().equals(leaderId)) {
                departmentUserRepository.deleteById(du.getId());
                break;
            }
        }

        // 如果有父部门，智能地从父部门删除该负责人
        // 关键：只有当负责人不在该父部门管辖的任何其他部门中时，才从父部门删除
        if (department.getParentPath() != null && !department.getParentPath().isEmpty()) {
            List<String> parentIds = parseParentPath(department.getParentPath());

            // 查询该负责人在所有部门中的关联
            List<DepartmentUser> allUserDeps = departmentUserRepository.findAll();

            // 获取该负责人当前所在的所有部门ID（排除刚删除的部门）
            Set<String> leaderDepIds = new HashSet<>();
            for (DepartmentUser ud : allUserDeps) {
                if (ud.getUserId().equals(leaderId) && !ud.getDepId().equals(id)) {
                    leaderDepIds.add(ud.getDepId());
                }
            }

            // 如果负责人已经不在任何部门了，直接从所有父部门删除
            if (leaderDepIds.isEmpty()) {
                for (String parentId : parentIds) {
                    List<DepartmentUser> parentDepUsers = departmentUserRepository.findByDepId(parentId);
                    for (DepartmentUser pdu : parentDepUsers) {
                        if (pdu.getUserId().equals(leaderId)) {
                            departmentUserRepository.deleteById(pdu.getId());
                            break;
                        }
                    }
                }
            } else {
                // 获取所有部门信息
                List<Department> allDeps = departmentRepository.findAll();

                // 构建部门ID到部门的映射
                Map<String, Department> depMap = allDeps.stream()
                        .collect(Collectors.toMap(Department::getId, d -> d));

                // 关键修复：反转parentIds顺序，从近到远逐级处理
                // parseParentPath返回的是从远到近的顺序，需要反转
                Collections.reverse(parentIds);

                for (String parentId : parentIds) {
                    // 检查负责人是否还在该父部门管辖的其他部门中
                    // 关键：排除当前要检查的父部门自己
                    boolean stillUnderThisParent = false;

                    for (String leaderDepId : leaderDepIds) {
                        // 跳过当前检查的父部门自己
                        if (leaderDepId.equals(parentId)) {
                            continue;
                        }

                        Department leaderDep = depMap.get(leaderDepId);
                        if (leaderDep == null) {
                            continue;
                        }

                        // 检查这个负责人所在的部门是否在当前父部门的管辖下
                        // 方法1：检查ParentPath是否包含parentId
                        if (leaderDep.getParentPath() != null && leaderDep.getParentPath().contains(parentId)) {
                            stillUnderThisParent = true;
                            break;
                        }
                        // 方法2：检查ParentId是否等于parentId
                        if (parentId.equals(leaderDep.getParentId())) {
                            stillUnderThisParent = true;
                            break;
                        }
                    }

                    // 只有当负责人不在该父部门管辖的任何部门中时，才从父部门删除
                    if (!stillUnderThisParent) {
                        List<DepartmentUser> parentDepUsers = departmentUserRepository.findByDepId(parentId);
                        for (DepartmentUser pdu : parentDepUsers) {
                            if (pdu.getUserId().equals(leaderId)) {
                                departmentUserRepository.deleteById(pdu.getId());
                                // 删除成功后，从leaderDepIds中移除这个父部门
                                leaderDepIds.remove(parentId);
                                break;
                            }
                        }
                    }
                }
            }
        }

        // 最后删除部门本身
        departmentRepository.deleteById(id);
        log.info("delete department success, id: {}", id);
    }

    @Override
    public void setDepartmentUsers(SetDepartmentUsersRequest request) {
        // 验证部门是否存在
        Department department = departmentRepository.findById(request.getDepId())
                .orElseThrow(() -> new BusinessException("部门不存在"));

        // 1. 获取当前部门的所有用户
        List<DepartmentUser> currentDepUsers = departmentUserRepository.findByDepId(request.getDepId());

        // 将当前用户ID转为set方便查找
        Set<String> currentUserSet = currentDepUsers.stream()
                .map(DepartmentUser::getUserId)
                .collect(Collectors.toSet());

        // 将新用户ID转为set方便查找
        Set<String> newUserSet = new HashSet<>(request.getUserIds() != null ? request.getUserIds() : new ArrayList<>());

        // 2. 找出需要���除的用户(在当前列表中但不在新列表中)
        for (DepartmentUser du : currentDepUsers) {
            if (!newUserSet.contains(du.getUserId())) {
                // 不能删除部门负责人
                if (du.getUserId().equals(department.getLeaderId())) {
                    continue;
                }
                // 使用级联删除方法
                try {
                    removeDepartmentUser(RemoveDepartmentUserRequest.builder()
                            .depId(request.getDepId())
                            .userId(du.getUserId())
                            .build());
                } catch (Exception e) {
                    // 记录错误但继续处理
                    log.warn("Failed to remove user {} from department {}: {}",
                            du.getUserId(), request.getDepId(), e.getMessage());
                }
            }
        }

        // 3. 找出需要添加的用户(在新列表中但不在当前列表中)
        for (String userId : newUserSet) {
            if (!currentUserSet.contains(userId)) {
                // 使用级联添加方法
                try {
                    addDepartmentUser(AddDepartmentUserRequest.builder()
                            .depId(request.getDepId())
                            .userId(userId)
                            .build());
                } catch (Exception e) {
                    // 如果是"已在部门中"的错误,忽略并继续
                    if (e.getMessage() != null && e.getMessage().contains("已在此部门中")) {
                        continue;
                    }
                    // 其他错误也继续处理
                    log.warn("Failed to add user {} to department {}: {}",
                            userId, request.getDepId(), e.getMessage());
                }
            }
        }

        log.info("set department users success, depId: {}, userCount: {}",
                request.getDepId(), newUserSet.size());
    }

    @Override
    public void addDepartmentUser(AddDepartmentUserRequest request) {
        // 验证部门是否存在
        Department department = departmentRepository.findById(request.getDepId())
                .orElseThrow(() -> new BusinessException("部门不存在"));

        // 验证用户是否存在
        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户是否已在该部门
        List<DepartmentUser> depUsers = departmentUserRepository.findByDepId(request.getDepId());
        for (DepartmentUser du : depUsers) {
            if (du.getUserId().equals(request.getUserId())) {
                throw new BusinessException("该用户已在此部门中");
            }
        }

        // 1. 添加用户到当前部门
        DepartmentUser depUser = new DepartmentUser();
        depUser.setDepId(request.getDepId());
        depUser.setUserId(request.getUserId());
        departmentUserRepository.save(depUser);

        // 2. 如果有上级部门，将用户也添加到所有上级部门
        if (department.getParentPath() != null && !department.getParentPath().isEmpty()) {
            List<String> parentIds = parseParentPath(department.getParentPath());

            for (String parentId : parentIds) {
                // 检查用户是否已在上级部门中
                List<DepartmentUser> parentDepUsers = departmentUserRepository.findByDepId(parentId);
                boolean exists = parentDepUsers.stream()
                        .anyMatch(pdu -> pdu.getUserId().equals(request.getUserId()));

                // 如果不存在则添加
                if (!exists) {
                    DepartmentUser parentDepUser = new DepartmentUser();
                    parentDepUser.setDepId(parentId);
                    parentDepUser.setUserId(request.getUserId());
                    departmentUserRepository.save(parentDepUser);
                }
            }
        }

        log.info("add department user success, depId: {}, userId: {}",
                request.getDepId(), request.getUserId());
    }

    @Override
    public void removeDepartmentUser(RemoveDepartmentUserRequest request) {
        // 验证部门是否存在
        Department department = departmentRepository.findById(request.getDepId())
                .orElseThrow(() -> new BusinessException("部门不存在"));

        // 不能删除部门负责人
        if (request.getUserId().equals(department.getLeaderId())) {
            throw new BusinessException("不能删除部门负责人");
        }

        // 查找用户在该部门的关联记录
        List<DepartmentUser> depUsers = departmentUserRepository.findByDepId(request.getDepId());
        DepartmentUser targetDepUser = null;
        for (DepartmentUser du : depUsers) {
            if (du.getUserId().equals(request.getUserId())) {
                targetDepUser = du;
                break;
            }
        }

        if (targetDepUser == null) {
            throw new BusinessException("该用户不在此部门中");
        }

        // 1. 删除用户与当前部门的关联
        departmentUserRepository.deleteById(targetDepUser.getId());

        // 2. 如果有上级部门，需要智能地从上级部门删除该用户
        // 关键：只有当用户不在该父部门管辖的任何其他部门中时，才从父部门删除
        if (department.getParentPath() != null && !department.getParentPath().isEmpty()) {
            // 解析父路径获取所有上级部门ID
            List<String> parentIds = parseParentPath(department.getParentPath());

            // 查询该用户在所有部门中的关联（用于判断用户还在哪些部门）
            List<DepartmentUser> allUserDeps = departmentUserRepository.findAll();

            // 获取该用户当前所在的所有部门ID（排除刚删除的部门）
            Set<String> userDepIds = new HashSet<>();
            for (DepartmentUser ud : allUserDeps) {
                if (ud.getUserId().equals(request.getUserId()) && !ud.getDepId().equals(request.getDepId())) {
                    userDepIds.add(ud.getDepId());
                }
            }

            // 如果用户已经不在任何部门了，直接从所有父部门删除
            if (userDepIds.isEmpty()) {
                for (String parentId : parentIds) {
                    List<DepartmentUser> parentDepUsers = departmentUserRepository.findByDepId(parentId);
                    for (DepartmentUser pdu : parentDepUsers) {
                        if (pdu.getUserId().equals(request.getUserId())) {
                            departmentUserRepository.deleteById(pdu.getId());
                            break;
                        }
                    }
                }
            } else {
                // 获取所有部门信息（用于检查部门的ParentPath）
                List<Department> allDeps = departmentRepository.findAll();

                // 构建部门ID到部门的映射
                Map<String, Department> depMap = allDeps.stream()
                        .collect(Collectors.toMap(Department::getId, d -> d));

                // 关键修复：反转parentIds顺序，从近到远逐级处理
                // parseParentPath返回的是从远到近的顺序，需要反转
                Collections.reverse(parentIds);

                for (String parentId : parentIds) {
                    // 检查用户是否还在该父部门管辖的其他部门中
                    // 关键：排除当前���检查的父部门自己
                    boolean stillUnderThisParent = false;

                    for (String userDepId : userDepIds) {
                        // 跳过当前检查的父部门自己
                        if (userDepId.equals(parentId)) {
                            continue;
                        }

                        Department userDep = depMap.get(userDepId);
                        if (userDep == null) {
                            continue;
                        }

                        // 检查这个用户所在的部门是否在当前父部门的管辖下
                        // 方法1：检查ParentPath是否包含parentId
                        if (userDep.getParentPath() != null && userDep.getParentPath().contains(parentId)) {
                            stillUnderThisParent = true;
                            break;
                        }
                        // 方法2：检查ParentId是否等于parentId
                        if (parentId.equals(userDep.getParentId())) {
                            stillUnderThisParent = true;
                            break;
                        }
                    }

                    // 只有当用户不在该父部门管辖的任何部门中时，才从父部门删除
                    if (!stillUnderThisParent) {
                        List<DepartmentUser> parentDepUsers = departmentUserRepository.findByDepId(parentId);
                        for (DepartmentUser pdu : parentDepUsers) {
                            if (pdu.getUserId().equals(request.getUserId())) {
                                departmentUserRepository.deleteById(pdu.getId());
                                // 删除成功后，从userDepIds中移除这个父部门
                                userDepIds.remove(parentId);
                                break;
                            }
                        }
                    }
                }
            }
        }

        log.info("remove department user success, depId: {}, userId: {}",
                request.getDepId(), request.getUserId());
    }

    @Override
    public DepartmentResponse departmentUserInfo(String userId) {
        // 根据用户ID查找用户所属的部门关联
        List<DepartmentUser> depUsers = departmentUserRepository.findByUserId(userId);
        if (depUsers == null || depUsers.isEmpty()) {
            throw new BusinessException("用户未关联任何部门");
        }
        DepartmentUser depUser = depUsers.get(0);

        // 根据部门ID查找部门信息
        Department department = departmentRepository.findById(depUser.getDepId())
                .orElseThrow(() -> new BusinessException("用户关联的部门不存在"));

        // 如果是根部门，直接返回
        if (department.getParentPath() == null || department.getParentPath().isEmpty()) {
            return buildDepartmentResponse(department);
        }

        // 解析父路径，获取所有上级部门ID
        List<String> parentIds = parseParentPath(department.getParentPath());
        List<Department> parentDeps = departmentRepository.findByIdIn(parentIds);
        Map<String, Department> depMap = parentDeps.stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        // 构建完整的部门层级结构
        DepartmentResponse root = null;
        DepartmentResponse node = null;

        for (String id : parentIds) {
            Department dep = depMap.get(id);
            if (dep == null) {
                continue;
            }

            if (root == null) {
                // 第一个父部门作为根节点
                root = buildDepartmentResponse(dep);
                node = root;
                continue;
            }

            // 构建层级关系
            DepartmentResponse tmp = buildDepartmentResponse(dep);
            if (node.getChild() == null) {
                node.setChild(new ArrayList<>());
            }
            node.getChild().add(tmp);
            node = tmp;
        }

        // 将用户直接关联的部门添加为最后一级
        if (node != null) {
            if (node.getChild() == null) {
                node.setChild(new ArrayList<>());
            }
            node.getChild().add(buildDepartmentResponse(department));
        }

        return root;
    }

    /**
     * 递归构建部门树结构
     */
    private void buildTree(List<DepartmentResponse> rootDep,
                           Map<String, List<DepartmentResponse>> groupDep) {
        for (DepartmentResponse dep : rootDep) {
            String path = buildParentPath(dep.getParentPath(), dep.getId());

            List<DepartmentResponse> children = groupDep.get(path);
            if (children != null && !children.isEmpty()) {
                buildTree(children, groupDep);
                dep.setChild(children);
            }
        }
    }

    /**
     * 构建父路径
     */
    private String buildParentPath(String parentPath, String id) {
        if (parentPath == null || parentPath.isEmpty()) {
            return ":" + id;
        }
        return parentPath + ":" + id;
    }

    /**
     * 解析父路径
     */
    private List<String> parseParentPath(String parentPath) {
        if (parentPath == null || parentPath.isEmpty()) {
            return new ArrayList<>();
        }
        String[] parts = parentPath.split(":");
        return Arrays.stream(parts)
                .skip(1)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 构建部门响应对象
     */
    private DepartmentResponse buildDepartmentResponse(Department department) {
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .parentId(department.getParentId())
                .parentPath(department.getParentPath())
                .level(department.getLevel())
                .leaderId(department.getLeaderId())
                .count(department.getCount())
                .build();
    }
}