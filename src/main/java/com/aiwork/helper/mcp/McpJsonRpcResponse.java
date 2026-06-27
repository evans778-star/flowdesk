package com.aiwork.helper.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpJsonRpcResponse {

    private String jsonrpc;

    private Object id;

    private Object result;

    private McpJsonRpcError error;

    public static McpJsonRpcResponse result(Object id, Object result) {
        return new McpJsonRpcResponse("2.0", id, result, null);
    }

    public static McpJsonRpcResponse error(Object id, int code, String message) {
        return new McpJsonRpcResponse("2.0", id, null, new McpJsonRpcError(code, message));
    }
}
