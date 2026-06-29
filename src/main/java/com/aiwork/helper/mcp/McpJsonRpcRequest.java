package com.aiwork.helper.mcp;

import lombok.Data;

import java.util.HashMap;

@Data
public class McpJsonRpcRequest {

    private String jsonrpc = "2.0";

    private Object id;

    private String method;

    private Object params = new HashMap<String, Object>();
}
