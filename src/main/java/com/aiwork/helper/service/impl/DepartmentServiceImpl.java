package com.aiwork.helper.service.impl;

import com.aiwork.helper.dto.request.AddDepartmentUserRequest;
import com.aiwork.helper.dto.request.DepartmentRequest;
import com.aiwork.helper.dto.request.RemoveDepartmentUserRequest;
import com.aiwork.helper.dto.request.SetDepartmentUsersRequest;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implements department tree, department user, and leader relationship workflows.
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
        List<Department> allDepartments = departmentRepository.findAll();

        Set<String> leaderIds = allDepartments.stream()
                .map(Department::getLeaderId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toSet());

        Map<String, String> leaderMap = new HashMap<>();
        if (!leaderIds.isEmpty()) {
            List<User> leaders = userRepository.findByIdIn(new ArrayList<>(leaderIds));
            leaderMap = leaders.stream()
                    .collect(Collectors.toMap(User::getId, User::getName));
        }

        List<DepartmentUser> allDepUsers = departmentUserRepository.findAll();
        Set<String> allUserIds = allDepUsers.stream()
                .map(DepartmentUser::getUserId)
                .collect(Collectors.toSet());

        Map<String, String> userMap = new HashMap<>();
        if (!allUserIds.isEmpty()) {
            List<User> users = userRepository.findByIdIn(new ArrayList<>(allUserIds));
            userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, User::getName));
        }

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

        Map<String, List<DepartmentResponse>> groupDep = new HashMap<>();
        List<DepartmentResponse> rootDep = new ArrayList<>();

        final Map<String, String> finalLeaderMap = leaderMap;
        for (Department dep : allDepartments) {
            DepartmentResponse depResp = buildDepartmentResponse(dep);

            if (finalLeaderMap.containsKey(dep.getLeaderId())) {
                depResp.setLeader(finalLeaderMap.get(dep.getLeaderId()));
            }

            depResp.setCount(depUserCountMap.getOrDefault(dep.getId(), 0L));
            depResp.setUsers(depUsersMap.getOrDefault(dep.getId(), new ArrayList<>()));

            if (dep.getParentPath() == null || dep.getParentPath().isEmpty()) {
                rootDep.add(depResp);
            } else {
                groupDep.computeIfAbsent(dep.getParentPath(), k -> new ArrayList<>()).add(depResp);
            }
        }

        buildTree(rootDep, groupDep);

        return DepartmentTreeResponse.builder()
                .child(rootDep)
                .build();
    }

    @Override
    public DepartmentResponse info(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Department does not exist"));

        User leader = userRepository.findById(department.getLeaderId())
                .orElseThrow(() -> new BusinessException("Department leader does not exist"));

        DepartmentResponse response = buildDepartmentResponse(department);
        response.setLeader(leader.getName());

        return response;
    }

    @Override
    public void create(DepartmentRequest request) {
        Department existingDep = departmentRepository.findByName(request.getName());
        if (existingDep != null) {
            throw new BusinessException("Department already exists");
        }

        String parentPath = "";
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            Department parentDep = departmentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new BusinessException("Parent department does not exist"));
            parentPath = buildParentPath(parentDep.getParentPath(), request.getParentId());
        }

        long currentTime = System.currentTimeMillis() / 1000;
        Department department = new Department();
        department.setName(request.getName());
        department.setParentId(request.getParentId());
        department.setParentPath(parentPath);
        department.setLevel(request.getLevel() != null ? request.getLevel() : 0);
        department.setLeaderId(request.getLeaderId());
        department.setCount(1L);
        department.setCreateAt(currentTime);

        Department saved = departmentRepository.save(department);
        log.info("create department success, id: {}", saved.getId());

        addDepartmentUser(AddDepartmentUserRequest.builder()
                .depId(saved.getId())
                .userId(request.getLeaderId())
                .build());
    }

    @Override
    public void edit(DepartmentRequest request) {
        if (request.getId() == null || request.getId().isEmpty()) {
            throw new BusinessException("Department id is required");
        }

        Department department = departmentRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("Department does not exist"));

        Department existingDep = departmentRepository.findByName(request.getName());
        if (existingDep != null && !existingDep.getId().equals(request.getId())) {
            throw new BusinessException("Department already exists");
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
        Department department = departmentRepository.findById(id).orElse(null);

        if (department == null) {
            return;
        }

        List<DepartmentUser> depUsers = departmentUserRepository.findByDepId(id);

        if (depUsers.isEmpty()) {
            departmentRepository.deleteById(id);
            log.info("delete department success, id: {}", id);
            return;
        }

        if (depUsers.size() > 1 ||
                !depUsers.get(0).getUserId().equals(department.getLeaderId())) {
            throw new BusinessException("Department still has users and cannot be deleted");
        }

        String leaderId = department.getLeaderId();

        for (DepartmentUser du : depUsers) {
            if (du.getUserId().equals(leaderId)) {
                departmentUserRepository.deleteById(du.getId());
                break;
            }
        }

        removeLeaderFromParentDepartmentsIfNeeded(department, id, leaderId);

        departmentRepository.deleteById(id);
        log.info("delete department success, id: {}", id);
    }

    private void removeLeaderFromParentDepartmentsIfNeeded(Department department, String deletedDepartmentId, String leaderId) {
        if (department.getParentPath() == null || department.getParentPath().isEmpty()) {
            return;
        }

        List<String> parentIds = parseParentPath(department.getParentPath());
        List<DepartmentUser> allUserDeps = departmentUserRepository.findAll();

        Set<String> leaderDepIds = new HashSet<>();
        for (DepartmentUser ud : allUserDeps) {
            if (ud.getUserId().equals(leaderId) && !ud.getDepId().equals(deletedDepartmentId)) {
                leaderDepIds.add(ud.getDepId());
            }
        }

        if (leaderDepIds.isEmpty()) {
            removeUserFromDepartments(leaderId, parentIds);
            return;
        }

        List<Department> allDeps = departmentRepository.findAll();
        Map<String, Department> depMap = allDeps.stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        Collections.reverse(parentIds);

        for (String parentId : parentIds) {
            boolean stillUnderThisParent = isAnyDepartmentUnderParent(leaderDepIds, parentId, depMap);
            if (!stillUnderThisParent) {
                removeUserFromDepartment(leaderId, parentId);
                leaderDepIds.remove(parentId);
            }
        }
    }

    @Override
    public void setDepartmentUsers(SetDepartmentUsersRequest request) {
        Department department = departmentRepository.findById(request.getDepId())
                .orElseThrow(() -> new BusinessException("Department does not exist"));

        List<DepartmentUser> currentDepUsers = departmentUserRepository.findByDepId(request.getDepId());
        Set<String> currentUserSet = currentDepUsers.stream()
                .map(DepartmentUser::getUserId)
                .collect(Collectors.toSet());

        Set<String> newUserSet = new HashSet<>(request.getUserIds() != null ? request.getUserIds() : new ArrayList<>());

        for (DepartmentUser du : currentDepUsers) {
            if (!newUserSet.contains(du.getUserId())) {
                if (du.getUserId().equals(department.getLeaderId())) {
                    continue;
                }
                try {
                    removeDepartmentUser(RemoveDepartmentUserRequest.builder()
                            .depId(request.getDepId())
                            .userId(du.getUserId())
                            .build());
                } catch (Exception e) {
                    log.warn("Failed to remove user {} from department {}: {}",
                            du.getUserId(), request.getDepId(), e.getMessage());
                }
            }
        }

        for (String userId : newUserSet) {
            if (!currentUserSet.contains(userId)) {
                try {
                    addDepartmentUser(AddDepartmentUserRequest.builder()
                            .depId(request.getDepId())
                            .userId(userId)
                            .build());
                } catch (Exception e) {
                    if (e.getMessage() != null && e.getMessage().contains("already belongs to this department")) {
                        continue;
                    }
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
        Department department = departmentRepository.findById(request.getDepId())
                .orElseThrow(() -> new BusinessException("Department does not exist"));

        userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("User does not exist"));

        List<DepartmentUser> depUsers = departmentUserRepository.findByDepId(request.getDepId());
        for (DepartmentUser du : depUsers) {
            if (du.getUserId().equals(request.getUserId())) {
                throw new BusinessException("User already belongs to this department");
            }
        }

        DepartmentUser depUser = new DepartmentUser();
        depUser.setDepId(request.getDepId());
        depUser.setUserId(request.getUserId());
        departmentUserRepository.save(depUser);

        if (department.getParentPath() != null && !department.getParentPath().isEmpty()) {
            List<String> parentIds = parseParentPath(department.getParentPath());

            for (String parentId : parentIds) {
                List<DepartmentUser> parentDepUsers = departmentUserRepository.findByDepId(parentId);
                boolean exists = parentDepUsers.stream()
                        .anyMatch(pdu -> pdu.getUserId().equals(request.getUserId()));

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
        Department department = departmentRepository.findById(request.getDepId())
                .orElseThrow(() -> new BusinessException("Department does not exist"));

        if (request.getUserId().equals(department.getLeaderId())) {
            throw new BusinessException("Department leader cannot be removed");
        }

        List<DepartmentUser> depUsers = departmentUserRepository.findByDepId(request.getDepId());
        DepartmentUser targetDepUser = null;
        for (DepartmentUser du : depUsers) {
            if (du.getUserId().equals(request.getUserId())) {
                targetDepUser = du;
                break;
            }
        }

        if (targetDepUser == null) {
            throw new BusinessException("User does not belong to this department");
        }

        departmentUserRepository.deleteById(targetDepUser.getId());
        removeUserFromParentDepartmentsIfNeeded(department, request.getDepId(), request.getUserId());

        log.info("remove department user success, depId: {}, userId: {}",
                request.getDepId(), request.getUserId());
    }

    private void removeUserFromParentDepartmentsIfNeeded(Department department, String currentDepId, String userId) {
        if (department.getParentPath() == null || department.getParentPath().isEmpty()) {
            return;
        }

        List<String> parentIds = parseParentPath(department.getParentPath());
        List<DepartmentUser> allUserDeps = departmentUserRepository.findAll();

        Set<String> userDepIds = new HashSet<>();
        for (DepartmentUser ud : allUserDeps) {
            if (ud.getUserId().equals(userId) && !ud.getDepId().equals(currentDepId)) {
                userDepIds.add(ud.getDepId());
            }
        }

        if (userDepIds.isEmpty()) {
            removeUserFromDepartments(userId, parentIds);
            return;
        }

        List<Department> allDeps = departmentRepository.findAll();
        Map<String, Department> depMap = allDeps.stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        Collections.reverse(parentIds);

        for (String parentId : parentIds) {
            boolean stillUnderThisParent = isAnyDepartmentUnderParent(userDepIds, parentId, depMap);
            if (!stillUnderThisParent) {
                removeUserFromDepartment(userId, parentId);
                userDepIds.remove(parentId);
            }
        }
    }

    private boolean isAnyDepartmentUnderParent(Set<String> departmentIds, String parentId, Map<String, Department> depMap) {
        for (String departmentId : departmentIds) {
            if (departmentId.equals(parentId)) {
                continue;
            }

            Department department = depMap.get(departmentId);
            if (department == null) {
                continue;
            }

            if (department.getParentPath() != null && department.getParentPath().contains(parentId)) {
                return true;
            }
            if (parentId.equals(department.getParentId())) {
                return true;
            }
        }
        return false;
    }

    private void removeUserFromDepartments(String userId, List<String> departmentIds) {
        for (String departmentId : departmentIds) {
            removeUserFromDepartment(userId, departmentId);
        }
    }

    private void removeUserFromDepartment(String userId, String departmentId) {
        List<DepartmentUser> depUsers = departmentUserRepository.findByDepId(departmentId);
        for (DepartmentUser depUser : depUsers) {
            if (depUser.getUserId().equals(userId)) {
                departmentUserRepository.deleteById(depUser.getId());
                break;
            }
        }
    }

    @Override
    public DepartmentResponse departmentUserInfo(String userId) {
        List<DepartmentUser> depUsers = departmentUserRepository.findByUserId(userId);
        if (depUsers == null || depUsers.isEmpty()) {
            throw new BusinessException("User is not associated with any department");
        }
        DepartmentUser depUser = depUsers.get(0);

        Department department = departmentRepository.findById(depUser.getDepId())
                .orElseThrow(() -> new BusinessException("Associated department does not exist"));

        if (department.getParentPath() == null || department.getParentPath().isEmpty()) {
            return buildDepartmentResponse(department);
        }

        List<String> parentIds = parseParentPath(department.getParentPath());
        List<Department> parentDeps = departmentRepository.findByIdIn(parentIds);
        Map<String, Department> depMap = parentDeps.stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        DepartmentResponse root = null;
        DepartmentResponse node = null;

        for (String id : parentIds) {
            Department dep = depMap.get(id);
            if (dep == null) {
                continue;
            }

            if (root == null) {
                root = buildDepartmentResponse(dep);
                node = root;
                continue;
            }

            DepartmentResponse tmp = buildDepartmentResponse(dep);
            if (node.getChild() == null) {
                node.setChild(new ArrayList<>());
            }
            node.getChild().add(tmp);
            node = tmp;
        }

        if (node != null) {
            if (node.getChild() == null) {
                node.setChild(new ArrayList<>());
            }
            node.getChild().add(buildDepartmentResponse(department));
        }

        return root;
    }

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

    private String buildParentPath(String parentPath, String id) {
        if (parentPath == null || parentPath.isEmpty()) {
            return ":" + id;
        }
        return parentPath + ":" + id;
    }

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
