package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.dto.request.ApprovalListRequest;
import com.aiwork.helper.dto.response.ApprovalListItemResponse;
import com.aiwork.helper.dto.response.ApprovalListResponse;
import com.aiwork.helper.mcp.McpToolCallError;
import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.McpToolDefinition;
import com.aiwork.helper.service.ApprovalService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ListApprovalsMcpTool implements FlowdeskMcpTool {

    private final ApprovalService approvalService;

    public ListApprovalsMcpTool(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @Override
    public McpToolDefinition definition() {
        Map<String, Map<String, Object>> properties = new LinkedHashMap<>();
        properties.put("type", McpToolSchemas.integerProperty("Approval list type. 1=submitted, 2=pending audit, other=related."));
        properties.put("status", McpToolSchemas.integerProperty("Optional status hint applied to returned items."));
        properties.put("page", McpToolSchemas.integerProperty("Page number, default 1."));
        properties.put("count", McpToolSchemas.integerProperty("Page size, default 10."));
        return new McpToolDefinition(
                "flowdesk_list_approvals",
                "List approvals",
                "List approvals related to the authenticated current user.",
                true,
                false,
                List.of("mcp:approval:read"),
                McpToolSchemas.objectSchema(List.of(), properties),
                "approvalListResult"
        );
    }

    @Override
    public McpToolCallResponse call(Map<String, Object> arguments) {
        if (arguments.containsKey("userId")) {
            return McpToolCallResponse.error(
                    definition().getName(),
                    McpToolCallError.PERMISSION_DENIED,
                    "userId is not accepted; this tool uses the authenticated current user.",
                    false
            );
        }

        ApprovalListRequest request = ApprovalListRequest.builder()
                .type(McpArgumentUtils.intValue(arguments, "type", 0))
                .page(McpArgumentUtils.intValue(arguments, "page", 1))
                .count(McpArgumentUtils.intValue(arguments, "count", 10))
                .build();

        ApprovalListResponse response = approvalService.list(request);
        Integer status = McpArgumentUtils.intValue(arguments, "status", null);
        List<ApprovalListItemResponse> approvals = response.getData() != null ? response.getData() : java.util.List.of();
        if (status != null) {
            approvals = approvals.stream()
                    .filter(approval -> status.equals(approval.getStatus()))
                    .toList();
        }
        return McpToolCallResponse.success(definition().getName(), Map.of(
                "count", approvals.size(),
                "approvals", approvals
        ));
    }
}
