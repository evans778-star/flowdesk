package com.aiwork.helper.ai.tools;

import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import com.aiwork.helper.dto.request.ApprovalListRequest;
import com.aiwork.helper.dto.request.ApprovalRequest;
import com.aiwork.helper.dto.response.ApprovalListResponse;
import com.aiwork.helper.entity.embedded.GoOut;
import com.aiwork.helper.entity.embedded.Leave;
import com.aiwork.helper.entity.embedded.MakeCard;
import com.aiwork.helper.entity.enums.ApprovalStatus;
import com.aiwork.helper.entity.enums.ApprovalType;
import com.aiwork.helper.service.ApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 审批工具集
 * 提供请假、补卡、外出等审批的创建和查询功能
 */
@Slf4j
@Component
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class ApprovalTools {

    private final ApprovalService approvalService;

    @Tool(description = "创建请假审批。当用户要请假时使用。leaveType: 1=事假, 2=调休, 3=病假, 4=年假, 5=产假, 6=陪产假, 7=婚假, 8=丧假, 9=哺乳假。timeType: 1=小时, 2=天。")
    public String createLeaveApproval(
            @ToolParam(description = "请假类型: 1=事假, 2=调休, 3=病假, 4=年假, 5=产假, 6=陪产假, 7=婚假, 8=丧假, 9=哺乳假") Integer leaveType,
            @ToolParam(description = "开始时间，Unix时间戳(秒)") Long startTime,
            @ToolParam(description = "结束时间，Unix时间戳(秒)") Long endTime,
            @ToolParam(description = "请假原因") String reason,
            @ToolParam(description = "时间单位类型: 1=小时, 2=天", required = false) Integer timeType
    ) {
        log.info("Tool调用 - createLeaveApproval: leaveType={}, startTime={}, endTime={}, reason={}, timeType={}",
                leaveType, startTime, endTime, reason, timeType);

        try {
            ApprovalRequest request = new ApprovalRequest();
            request.setType(2); // 请假审批类型

            Leave leave = new Leave();
            leave.setType(leaveType != null ? leaveType : 1); // 默认事假
            leave.setStartTime(startTime);
            leave.setEndTime(endTime);
            leave.setReason(reason != null ? reason : "请假");
            leave.setTimeType(timeType != null ? timeType : 2); // 默认按天

            request.setLeave(leave);

            String approvalId = approvalService.create(request);

            return String.format("请假审批创建成功!\n审批ID: %s\n类型: %s\n时间: %s 至 %s\n原因: %s",
                    approvalId,
                    getLeaveTypeName(leave.getType()),
                    formatTimestamp(leave.getStartTime()),
                    formatTimestamp(leave.getEndTime()),
                    leave.getReason());

        } catch (Exception e) {
            log.error("创建请假审批失败", e);
            return "创建请假审批失败: " + e.getMessage();
        }
    }

    @Tool(description = "创建补卡审批。当用户要申请补卡时使用。checkType: 1=上班卡, 2=下班卡。")
    public String createPunchApproval(
            @ToolParam(description = "补卡日期，Unix时间戳(秒)") Long makeCardDate,
            @ToolParam(description = "补卡类型: 1=上班卡, 2=下班卡") Integer checkType,
            @ToolParam(description = "补卡原因") String reason
    ) {
        log.info("Tool调用 - createPunchApproval: makeCardDate={}, checkType={}, reason={}",
                makeCardDate, checkType, reason);

        try {
            ApprovalRequest request = new ApprovalRequest();
            request.setType(3); // 补卡审批类型

            MakeCard makeCard = new MakeCard();
            makeCard.setDate(makeCardDate);
            makeCard.setCheckType(checkType != null ? checkType : 2); // 默认下班卡
            makeCard.setReason(reason != null ? reason : "补卡");

            request.setMakeCard(makeCard);

            String approvalId = approvalService.create(request);

            return String.format("补卡审批创建成功!\n审批ID: %s\n补卡时间: %s\n类型: %s\n原因: %s",
                    approvalId,
                    formatTimestamp(makeCard.getDate()),
                    makeCard.getCheckType() == 1 ? "上班卡" : "下班卡",
                    makeCard.getReason());

        } catch (Exception e) {
            log.error("创建补卡审批失败", e);
            return "创建补卡审批失败: " + e.getMessage();
        }
    }

    @Tool(description = "创建外出审批。当用户要申请外出时使用。")
    public String createGoOutApproval(
            @ToolParam(description = "外出开始时间，Unix时间戳(秒)") Long startTime,
            @ToolParam(description = "外出结束时间，Unix时间戳(秒)") Long endTime,
            @ToolParam(description = "外出原因") String reason
    ) {
        log.info("Tool调用 - createGoOutApproval: startTime={}, endTime={}, reason={}",
                startTime, endTime, reason);

        try {
            ApprovalRequest request = new ApprovalRequest();
            request.setType(4); // 外出审批类型

            GoOut goOut = new GoOut();
            goOut.setStartTime(startTime);
            goOut.setEndTime(endTime);
            goOut.setReason(reason != null ? reason : "外出");

            request.setGoOut(goOut);

            String approvalId = approvalService.create(request);

            return String.format("外出审批创建成功!\n审批ID: %s\n时间: %s 至 %s\n原因: %s",
                    approvalId,
                    formatTimestamp(goOut.getStartTime()),
                    formatTimestamp(goOut.getEndTime()),
                    goOut.getReason());

        } catch (Exception e) {
            log.error("创建外出审批失败", e);
            return "创建外出审批失败: " + e.getMessage();
        }
    }

    @Tool(description = "查询审批记录。当用户想要查看审批列表时使用。")
    public String findApprovals(
            @ToolParam(description = "查询类型: 1=我提交的, 2=待我审批的", required = false) Integer queryType
    ) {
        log.info("Tool调用 - findApprovals: queryType={}", queryType);

        try {
            ApprovalListRequest listRequest = new ApprovalListRequest();
            listRequest.setType(queryType != null ? queryType : 1); // 默认查询我提交的
            listRequest.setPage(1);
            listRequest.setCount(10);

            ApprovalListResponse listResponse = approvalService.list(listRequest);

            if (listResponse.getData() == null || listResponse.getData().isEmpty()) {
                return "您当前没有审批记录。";
            }

            var approvals = listResponse.getData();

            StringBuilder result = new StringBuilder("您的审批记录:\n\n");
            int index = 1;
            for (var approval : approvals) {
                result.append(String.format("%d. %s\n", index++,
                        ApprovalType.fromValue(approval.getType()).getDescription()));
                result.append(String.format("   状态: %s\n",
                        ApprovalStatus.fromValue(approval.getStatus()).getDescription()));
                result.append(String.format("   创建时间: %s\n",
                        formatTimestamp(approval.getCreateAt())));

                // 根据类型显示详细信息
                if (approval.getLeave() != null) {
                    Leave leave = approval.getLeave();
                    result.append(String.format("   请假类型: %s\n", getLeaveTypeName(leave.getType())));
                    result.append(String.format("   时间: %s 至 %s\n",
                            formatTimestamp(leave.getStartTime()),
                            formatTimestamp(leave.getEndTime())));
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("查询审批失败", e);
            return "查询审批失败: " + e.getMessage();
        }
    }

    /**
     * 获取请假类型名称
     */
    private String getLeaveTypeName(Integer type) {
        if (type == null) return "未知";
        return switch (type) {
            case 1 -> "事假";
            case 2 -> "调休";
            case 3 -> "病假";
            case 4 -> "年假";
            case 5 -> "产假";
            case 6 -> "陪产假";
            case 7 -> "婚假";
            case 8 -> "丧假";
            case 9 -> "哺乳假";
            default -> "其他";
        };
    }

    /**
     * 格式化时间戳
     */
    private String formatTimestamp(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return "未设置";
        }
        long millis = timestamp * 1000L;
        Instant instant = Instant.ofEpochMilli(millis);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }
}
