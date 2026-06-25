package com.aiwork.helper.service.impl;

import com.aiwork.helper.dto.request.ApprovalListRequest;
import com.aiwork.helper.dto.request.ApprovalRequest;
import com.aiwork.helper.dto.request.DisposeRequest;
import com.aiwork.helper.dto.response.ApprovalListItemResponse;
import com.aiwork.helper.dto.response.ApprovalListResponse;
import com.aiwork.helper.dto.response.ApprovalResponse;
import com.aiwork.helper.entity.Approval;
import com.aiwork.helper.entity.Department;
import com.aiwork.helper.entity.DepartmentUser;
import com.aiwork.helper.entity.User;
import com.aiwork.helper.entity.embedded.*;
import com.aiwork.helper.entity.enums.*;
import com.aiwork.helper.exception.BusinessException;
import com.aiwork.helper.repository.ApprovalRepository;
import com.aiwork.helper.repository.DepartmentRepository;
import com.aiwork.helper.repository.DepartmentUserRepository;
import com.aiwork.helper.repository.UserRepository;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 审批服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ApprovalRepository approvalRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentUserRepository departmentUserRepository;

    @Override
    public ApprovalResponse info(String id) {
        // 查询审批记录
        Approval approval = approvalRepository.findById(id)
                .orElseThrow(() -> new BusinessException("审批记录不存在"));

        // 查询所有参与人员信息
        List<User> users = userRepository.findByIdIn(approval.getParticipation());
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 获取申请人信息
        User applicant = userMap.get(approval.getUserId());
        if (applicant == null) {
            throw new BusinessException("申请人信息不存在");
        }

        Approver userInfo = new Approver();
        userInfo.setUserId(applicant.getId());
        userInfo.setUserName(applicant.getName());

        // 获取当前审批人信息
        User currentApprover = userMap.get(approval.getApprovalId());
        Approver approverInfo = new Approver();
        if (currentApprover != null) {
            approverInfo.setUserId(currentApprover.getId());
            approverInfo.setUserName(currentApprover.getName());
        }

        // 构建审批人列表（包含状态和原因）
        List<Approver> approvers = new ArrayList<>();
        if (approval.getApprovers() != null) {
            for (Approver approver : approval.getApprovers()) {
                User user = userMap.get(approver.getUserId());
                Approver approverWithName = new Approver();
                approverWithName.setUserId(approver.getUserId());
                approverWithName.setUserName(user != null ? user.getName() : "");
                approverWithName.setStatus(approver.getStatus());
                approverWithName.setReason(approver.getReason());
                approvers.add(approverWithName);
            }
        }

        // 构建抄送人列表
        List<Approver> copyPersons = new ArrayList<>();
        if (approval.getCopyPersons() != null) {
            for (Approver copyPerson : approval.getCopyPersons()) {
                User user = userMap.get(copyPerson.getUserId());
                Approver copyPersonWithName = new Approver();
                copyPersonWithName.setUserId(copyPerson.getUserId());
                copyPersonWithName.setUserName(user != null ? user.getName() : "");
                copyPersons.add(copyPersonWithName);
            }
        }

        return ApprovalResponse.builder()
                .id(approval.getId())
                .user(userInfo)
                .no(approval.getNo())
                .type(approval.getType())
                .status(approval.getStatus())
                .title(approval.getTitle())
                .abstract_(approval.getAbstract_())
                .reason(approval.getReason())
                .approver(approverInfo)
                .approvers(approvers)
                .copyPersons(copyPersons)
                .finishAt(approval.getFinishAt())
                .finishDay(approval.getFinishDay())
                .finishMonth(approval.getFinishMonth())
                .finishYeas(approval.getFinishYeas())
                .makeCard(approval.getMakeCard())
                .leave(approval.getLeave())
                .goOut(approval.getGoOut())
                .updateAt(approval.getUpdateAt())
                .createAt(approval.getCreateAt())
                .build();
    }

    @Override
    public String create(ApprovalRequest request) {
        log.info("create approval: {}", request);

        // 获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 获取申请人信息
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 创建审批记录
        long currentTime = System.currentTimeMillis() / 1000;
        Approval approval = new Approval();
        approval.setUserId(currentUserId);
        approval.setNo(generateRandomNo(11));
        approval.setType(request.getType());
        approval.setStatus(ApprovalStatus.PROCESSED.getValue());
        approval.setReason(request.getReason());
        approval.setCreateAt(currentTime);
        approval.setUpdateAt(currentTime);

        // 根据审批类型处理不同的审批内容
        String abstract_ = processApprovalContent(approval, request);

        // 生成审批标题和摘要
        approval.setTitle(String.format("%s 提交的 %s", user.getName(),
                ApprovalType.fromValue(request.getType()).getDescription()));
        approval.setAbstract_(abstract_);

        // 构建审批流程：根据部门层级确定审批人
        buildApprovalProcess(approval, currentUserId);

        // 保存审批记录
        Approval saved = approvalRepository.save(approval);
        log.info("create approval success, id: {}", saved.getId());

        return saved.getId();
    }

    @Override
    public void dispose(DisposeRequest request) {
        log.info("dispose approval: {}", request);

        // 获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 查询审批记录
        Approval approval = approvalRepository.findById(request.getApprovalId())
                .orElseThrow(() -> new BusinessException("审批记录不存在"));

        // 处理撤销操作
        if (request.getStatus().equals(ApprovalStatus.CANCEL.getValue())) {
            // 只有申请人才能撤销
            if (!currentUserId.equals(approval.getUserId())) {
                throw new BusinessException("只有申请人才能撤销审批");
            }
            approval.setStatus(ApprovalStatus.CANCEL.getValue());
            approval.setUpdateAt(System.currentTimeMillis() / 1000);
            approvalRepository.save(approval);
            log.info("cancel approval success, id: {}", request.getApprovalId());
            return;
        }

        // 验证当前用户是否为当前审批人
        if (!currentUserId.equals(approval.getApprovalId())) {
            throw new BusinessException("您不是当前审批人");
        }

        // 检查审批状态
        if (approval.getStatus().equals(ApprovalStatus.CANCEL.getValue())) {
            throw new BusinessException("该审批已撤销");
        }
        if (approval.getStatus().equals(ApprovalStatus.PASS.getValue())) {
            throw new BusinessException("该审批已通过");
        }
        if (approval.getStatus().equals(ApprovalStatus.REFUSE.getValue())) {
            throw new BusinessException("该审批已拒绝");
        }

        // 处理拒绝操作
        if (request.getStatus().equals(ApprovalStatus.REFUSE.getValue())) {
            // 记录当前审批人的拒绝状态和原因
            List<Approver> approvers = approval.getApprovers();
            if (approvers != null && approval.getApprovalIdx() < approvers.size()) {
                approvers.get(approval.getApprovalIdx()).setStatus(ApprovalStatus.REFUSE.getValue());
                approvers.get(approval.getApprovalIdx()).setReason(request.getReason());
            }
            // 设置整体审批状态为拒绝
            approval.setStatus(ApprovalStatus.REFUSE.getValue());
        }
        // 处理通过操作
        else if (request.getStatus().equals(ApprovalStatus.PASS.getValue())) {
            // 记录当前审批人的通过状态和原因
            List<Approver> approvers = approval.getApprovers();
            if (approvers != null && approval.getApprovalIdx() < approvers.size()) {
                approvers.get(approval.getApprovalIdx()).setStatus(ApprovalStatus.PASS.getValue());
                approvers.get(approval.getApprovalIdx()).setReason(request.getReason());

                // 如果还有下一级审批人，则流转到下一级
                if (approvers.size() - 1 > approval.getApprovalIdx()) {
                    approval.setApprovalIdx(approval.getApprovalIdx() + 1);
                    approval.setApprovalId(approvers.get(approval.getApprovalIdx()).getUserId());
                } else {
                    // 这是最后一个审批人，检查是否所有审批人都已通过
                    boolean isPass = true;
                    for (Approver approver : approvers) {
                        if (!approver.getStatus().equals(ApprovalStatus.PASS.getValue())) {
                            isPass = false;
                            break;
                        }
                    }
                    // 如果所有审批人都已通过，则设置整体状态为通过
                    if (isPass) {
                        approval.setStatus(ApprovalStatus.PASS.getValue());
                    }
                }
            }
        }

        approval.setUpdateAt(System.currentTimeMillis() / 1000);
        approvalRepository.save(approval);
        log.info("dispose approval success, id: {}, status: {}", request.getApprovalId(), request.getStatus());
    }

    @Override
    public ApprovalListResponse list(ApprovalListRequest request) {
        log.info("approval list request: {}", request);

        // 获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 构建分页参数
        int page = request.getPage() != null ? request.getPage() - 1 : 0;
        int pageSize = request.getCount() != null ? request.getCount() : 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));

        // 根据操作类型查询审批列表
        Page<Approval> approvalPage;
        ApprovalOptionType optionType = ApprovalOptionType.fromValue(request.getType());

        if (optionType == ApprovalOptionType.SUBMIT) {
            // 我提交的：查询申请人是当前用户的记录
            approvalPage = approvalRepository.findByUserId(currentUserId, pageable);
        } else if (optionType == ApprovalOptionType.AUDIT) {
            // 待我审批的：查询当前审批人是当前用户且状态为处理中的记录
            approvalPage = approvalRepository.findByApprovalIdAndStatus(
                    currentUserId, ApprovalStatus.PROCESSED.getValue(), pageable);
        } else {
            // 默认查询与我相关的所有审批
            approvalPage = approvalRepository.findByParticipationContaining(currentUserId, pageable);
        }

        List<Approval> approvals = approvalPage.getContent();
        long count = approvalPage.getTotalElements();

        // 构建响应列表
        List<ApprovalListItemResponse> data = approvals.stream()
                .map(approval -> ApprovalListItemResponse.builder()
                        .id(approval.getId())
                        .no(approval.getNo())
                        .type(approval.getType())
                        .status(approval.getStatus())
                        .title(approval.getTitle())
                        .abstract_(approval.getAbstract_())
                        .createId(approval.getUserId())
                        .participatingId(approval.getApprovalId())
                        .createAt(approval.getCreateAt())
                        .leave(approval.getLeave())
                        .makeCard(approval.getMakeCard())
                        .goOut(approval.getGoOut())
                        .build())
                .collect(Collectors.toList());

        return ApprovalListResponse.builder()
                .count(count)
                .data(data)
                .build();
    }

    /**
     * 处理审批内容并生成摘要
     */
    private String processApprovalContent(Approval approval, ApprovalRequest request) {
        String abstract_ = "";
        ApprovalType approvalType = ApprovalType.fromValue(request.getType());

        log.info("processApprovalContent: type={}, approvalType={}, leave={}",
                request.getType(), approvalType, request.getLeave() != null);

        switch (approvalType) {
            case LEAVE:
                // 请假审批
                if (request.getLeave() != null) {
                    Leave leave = request.getLeave();
                    log.info("Leave data: type={}, startTime={}, endTime={}, reason={}, timeType={}",
                            leave.getType(), leave.getStartTime(), leave.getEndTime(),
                            leave.getReason(), leave.getTimeType());

                    // 计算时长
                    float duration;
                    if (leave.getTimeType() != null &&
                            leave.getTimeType().equals(TimeFormatType.HOUR.getValue())) {
                        duration = (leave.getEndTime() - leave.getStartTime()) / 3600.0f;
                    } else {
                        duration = (leave.getEndTime() - leave.getStartTime()) / 86400.0f;
                    }
                    leave.setDuration(duration);
                    approval.setLeave(leave);

                    String leaveTypeName = LeaveType.fromValue(leave.getType()).getDescription();
                    abstract_ = String.format("【%s】: 【%s】-【%s】",
                            leaveTypeName,
                            formatTimestamp(leave.getStartTime()),
                            formatTimestamp(leave.getEndTime()));
                    log.info("Generated abstract: {}", abstract_);
                    approval.setReason(leave.getReason());
                } else {
                    log.warn("Leave data is null for LEAVE approval type!");
                }
                break;

            case GO_OUT:
                // 外出审批
                if (request.getGoOut() != null) {
                    GoOut goOut = request.getGoOut();
                    // 计算时长（小时）
                    float duration = (goOut.getEndTime() - goOut.getStartTime()) / 3600.0f;
                    goOut.setDuration(duration);
                    approval.setGoOut(goOut);

                    abstract_ = String.format("【%s】-【%s】",
                            formatTimestamp(goOut.getStartTime()),
                            formatTimestamp(goOut.getEndTime()));
                    approval.setReason(goOut.getReason());
                }
                break;

            case MAKE_CARD:
                // 补卡审批
                if (request.getMakeCard() != null) {
                    MakeCard makeCard = request.getMakeCard();
                    approval.setMakeCard(makeCard);

                    abstract_ = String.format("【%s】【%s】",
                            formatTimestamp(makeCard.getDate()),
                            makeCard.getReason());
                    approval.setReason(makeCard.getReason());
                }
                break;

            default:
                // 其他类型审批
                approval.setReason(request.getReason());
                abstract_ = request.getAbstract_();
                break;
        }

        return abstract_;
    }

    /**
     * 构建审批流程：根据部门层级确定审批人
     */
    private void buildApprovalProcess(Approval approval, String userId) {
        // 获取申请人所有关联的部门
        List<DepartmentUser> depUsers = departmentUserRepository.findByUserId(userId);
        if (depUsers == null || depUsers.isEmpty()) {
            throw new BusinessException("用户未分配部门");
        }

        // 获取所有这些部门的ID
        List<String> userDepIds = depUsers.stream()
                .map(DepartmentUser::getDepId)
                .collect(Collectors.toList());

        // 查询所有这些部门的详细信息
        List<Department> userDeps = departmentRepository.findByIdIn(userDepIds);
        if (userDeps == null || userDeps.isEmpty()) {
            throw new BusinessException("用户关联的部门不存在");
        }

        // 找出层级最深的部门（ParentPath最长的，即用户实际直接所属的部门）
        Department department = null;
        int maxPathLen = -1;
        for (Department d : userDeps) {
            int pathLen = d.getParentPath() != null ? d.getParentPath().length() : 0;
            if (pathLen > maxPathLen) {
                maxPathLen = pathLen;
                department = d;
            }
        }

        if (department == null) {
            throw new BusinessException("未找到用户所属部门");
        }

        log.info("用户 {} 所属部门: {}, ParentPath: {}", userId, department.getName(), department.getParentPath());

        // 解析父级路径
        List<String> parentIds = parseParentPath(department.getParentPath());
        log.info("解析的父级部门ID列表: {}", parentIds);

        // 查询所有父级部门
        List<Department> parentDepartments = departmentRepository.findByIdIn(parentIds);
        Map<String, Department> depMap = parentDepartments.stream()
                .collect(Collectors.toMap(Department::getId, d -> d));

        log.info("查询到的父级部门: {}", depMap.keySet());

        List<Approver> approvers = new ArrayList<>();
        List<String> participations = new ArrayList<>();

        // 添加直属部门负责人作为第一级审批人
        Approver firstApprover = new Approver();
        firstApprover.setUserId(department.getLeaderId());
        firstApprover.setStatus(ApprovalStatus.PROCESSED.getValue());
        approvers.add(firstApprover);
        log.info("添加第一级审批人（直属部门负责人）: {}", department.getLeaderId());

        participations.add(department.getLeaderId());
        participations.add(userId);

        // 按部门层级从下到上添加审批人
        for (int i = parentIds.size() - 1; i > 0; i--) {
            String parentId = parentIds.get(i);
            Department parentDep = depMap.get(parentId);
            log.info("检查父级部门 index={}, id={}, found={}", i, parentId, parentDep != null);
            if (parentDep != null) {
                Approver approver = new Approver();
                approver.setUserId(parentDep.getLeaderId());
                approver.setStatus(ApprovalStatus.NOT_STARTED.getValue());
                approvers.add(approver);
                log.info("添加上级审批人: 部门={}, 领导={}", parentDep.getName(), parentDep.getLeaderId());

                participations.add(parentDep.getLeaderId());
            }
        }

        log.info("最终审批人列表数量: {}, 参与人数量: {}", approvers.size(), participations.size());

        approval.setApprovers(approvers);
        approval.setParticipation(participations);
        approval.setApprovalId(department.getLeaderId());
        approval.setApprovalIdx(0);
    }

    /**
     * 解析父级路径
     * 例如: ":root:dep1:dep2:" -> ["root", "dep1", "dep2"]
     */
    private List<String> parseParentPath(String parentPath) {
        if (parentPath == null || parentPath.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(parentPath.split(":"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 格式化时间戳为字符串
     */
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return "未设置";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date(timestamp * 1000));
    }

    /**
     * 生成指定位数的随机数字字符串
     */
    private String generateRandomNo(int width) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < width; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}