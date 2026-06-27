# MCP Server Adapter Preview

Flowdesk includes a minimal MCP-style HTTP adapter preview for exposing a small set of office assistant tools to AI developer clients.

This is an HTTP MCP adapter preview with a JSON-RPC preview endpoint, not a full standard MCP transport yet. Flowdesk currently exposes tool discovery and tool calls through authenticated REST endpoints under `/v1/mcp`.

This preview does not add a standard MCP SDK dependency yet. The current project uses Spring Boot 3.2 and an older Spring AI generation, while the current Spring AI MCP server starter documentation is on the Spring AI 2.0 line. Introducing that starter now would likely require broader dependency alignment and transport configuration work than this preview phase needs. The internal tool registry and DTOs are intentionally small so they can later be wired to a standard MCP transport such as stdio, HTTP/SSE, or Streamable HTTP.

The official MCP tools specification uses JSON-RPC methods such as `initialize`, `ping`, `tools/list`, and `tools/call`. Flowdesk mirrors this small method set in `/v1/mcp/jsonrpc` for local experimentation.

## Compatibility Matrix

| Capability | Flowdesk status | Notes |
| --- | --- | --- |
| HTTP tool registry | Preview | `GET /v1/mcp/tools` and `POST /v1/mcp/tools/{toolName}/call` remain available for existing clients. |
| JSON-RPC envelope | Preview | `POST /v1/mcp/jsonrpc` returns JSON-RPC 2.0 responses. |
| `initialize` | Preview | Returns protocol version, server info, and tool capabilities. |
| `ping` | Preview | Returns an empty JSON-RPC result for smoke checks. |
| `tools/list` | Preview | Returns tool metadata with `inputSchema`, `outputSchema`, and annotations. |
| `tools/call` | Preview | Calls registered Flowdesk tools and wraps tool errors in `isError=true` results. |
| stdio transport | Not implemented | Use a bridge process if a client only supports stdio MCP servers. |
| SSE / Streamable HTTP transport | Not implemented | Planned for a later standard transport phase. |

## Safety Defaults

MCP is disabled by default:

```powershell
$env:FLOWDESK_MCP_ENABLED="false"
$env:FLOWDESK_MCP_WRITE_TOOLS_ENABLED="false"
```

Enable the HTTP adapter preview locally:

```powershell
$env:FLOWDESK_MCP_ENABLED="true"
$env:FLOWDESK_MCP_WRITE_TOOLS_ENABLED="false"
```

Do not expose MCP endpoints directly to the public internet. MCP clients can act quickly and repeatedly on a user's behalf, so keep this adapter behind trusted networks, local tunnels, or explicit API gateway controls.

All `/v1/mcp/**` endpoints use the existing Spring Security rules. They require the same JWT authentication as normal protected REST APIs. Write tools remain blocked unless `FLOWDESK_MCP_WRITE_TOOLS_ENABLED=true`.

## HTTP Endpoints

List tools:

```http
GET /v1/mcp/tools
Authorization: Bearer <jwt-token>
```

Call a tool:

```http
POST /v1/mcp/tools/{toolName}/call
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "arguments": {
    "query": "What should employees do before taking leave?",
    "topK": 3
  }
}
```

JSON-RPC preview:

```http
POST /v1/mcp/jsonrpc
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/list",
  "params": {}
}
```

Initialize the JSON-RPC preview:

```http
POST /v1/mcp/jsonrpc
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "id": "init-1",
  "method": "initialize",
  "params": {
    "protocolVersion": "2025-06-18"
  }
}
```

Smoke-check the JSON-RPC preview:

```http
POST /v1/mcp/jsonrpc
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "jsonrpc": "2.0",
  "id": "ping-1",
  "method": "ping",
  "params": {}
}
```

## Tool Definition Shape

`GET /v1/mcp/tools` returns stable tool metadata that AI clients can inspect before calling a tool:

```json
{
  "name": "flowdesk_search_knowledge",
  "title": "Search knowledge",
  "description": "Search Flowdesk RAG knowledge chunks for the authenticated user context.",
  "readOnly": true,
  "write": false,
  "requiredPermissions": ["mcp:knowledge:read"],
  "inputSchema": {
    "type": "object",
    "required": ["query"],
    "properties": {
      "query": {
        "type": "string",
        "description": "Natural-language knowledge search query."
      },
      "topK": {
        "type": "integer",
        "description": "Maximum number of chunks to retrieve. Default 3, maximum 10."
      }
    },
    "additionalProperties": false
  },
  "outputSchema": {
    "type": "object",
    "properties": {
      "success": { "type": "boolean" },
      "toolName": { "type": "string" },
      "data": {
        "type": "object",
        "description": "knowledgeSearchResult"
      },
      "error": { "type": ["object", "null"] },
      "metadata": { "type": "object" }
    }
  },
  "annotations": {
    "readOnlyHint": true,
    "destructiveHint": false,
    "idempotentHint": true,
    "openWorldHint": false
  },
  "resultType": "knowledgeSearchResult"
}
```

The schema is intentionally represented as plain JSON-compatible maps instead of introducing a separate JSON Schema dependency.

The same tool list is available through JSON-RPC:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "result": {
    "tools": [
      {
        "name": "flowdesk_search_knowledge",
        "title": "Search knowledge",
        "description": "Search Flowdesk RAG knowledge chunks for the authenticated user context.",
        "inputSchema": {
          "type": "object",
          "required": ["query"]
        },
        "outputSchema": {
          "type": "object"
        },
        "annotations": {
          "readOnlyHint": true,
          "destructiveHint": false
        }
      }
    ]
  }
}
```

## Tool Catalog

| Tool | Permission | Write | Result type | Notes |
| --- | --- | --- | --- | --- |
| `flowdesk_search_knowledge` | `mcp:knowledge:read` | No | `knowledgeSearchResult` | Searches Redis Stack RAG chunks and returns chunks plus citations. Requires AI/RAG vector store beans to be available. |
| `flowdesk_list_todos` | `mcp:todo:read` | No | `todoListResult` | Lists todos for the authenticated current user. It rejects arbitrary `userId` arguments. |
| `flowdesk_create_todo` | `mcp:todo:write` | Yes | `todoCreateResult` | Creates a todo for the authenticated current user only when write tools are enabled. |
| `flowdesk_list_approvals` | `mcp:approval:read` | No | `approvalListResult` | Lists approvals related to the authenticated current user. It rejects arbitrary `userId` arguments. |
| `flowdesk_upload_document_metadata` | `mcp:document:metadata` | No | `documentUploadMetadataResult` | Returns safe upload guidance and echoes metadata. It never reads server-local file paths. |

## Permission Model

The adapter exposes required permission strings in tool metadata so future MCP transports, API gateways, or RBAC layers can enforce them consistently. In this preview, Flowdesk still relies on existing JWT authentication and service-layer current-user checks. Tools that could otherwise cross user boundaries reject caller-supplied `userId` arguments.

Write-capable tools are additionally gated by `FLOWDESK_MCP_WRITE_TOOLS_ENABLED=false` by default. This is a configuration boundary, not a substitute for production RBAC.

## Example Calls

JSON-RPC `initialize` preview response:

```json
{
  "jsonrpc": "2.0",
  "id": "init-1",
  "result": {
    "protocolVersion": "2025-06-18",
    "serverInfo": {
      "name": "flowdesk",
      "version": "1.0.0-SNAPSHOT"
    },
    "capabilities": {
      "tools": {
        "listChanged": false
      }
    }
  }
}
```

JSON-RPC `ping` preview response:

```json
{
  "jsonrpc": "2.0",
  "id": "ping-1",
  "result": {}
}
```

Search knowledge:

```json
{
  "arguments": {
    "query": "What should employees do before taking leave?",
    "topK": 3
  }
}
```

JSON-RPC `tools/call` preview:

```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/call",
  "params": {
    "name": "flowdesk_search_knowledge",
    "arguments": {
      "query": "What should employees do before taking leave?",
      "topK": 3
    }
  }
}
```

List todos for the authenticated user:

```json
{
  "arguments": {
    "currentUser": true,
    "page": 1,
    "count": 10
  }
}
```

Create todo, disabled unless `FLOWDESK_MCP_WRITE_TOOLS_ENABLED=true`:

```json
{
  "arguments": {
    "title": "Prepare release notes",
    "description": "Draft public-safe release notes",
    "assigneeIds": ["example-user-id"],
    "dueDate": 1893456000
  }
}
```

Document metadata guidance:

```json
{
  "arguments": {
    "fileName": "sample-employee-handbook.pdf",
    "contentType": "application/pdf",
    "sizeBytes": 123456
  }
}
```

Do not pass `filePath` or server-local paths to MCP tools. Upload files through `POST /v1/upload/file`, then ask Flowdesk to index the uploaded document through the normal knowledge workflow.

## Response Shape

Successful tool call:

```json
{
  "success": true,
  "toolName": "flowdesk_search_knowledge",
  "data": {
    "query": "What should employees do before taking leave?",
    "topK": 3,
    "chunks": [],
    "citations": []
  },
  "error": null,
  "metadata": {
    "durationMs": 12
  }
}
```

Unknown tool:

```json
{
  "success": false,
  "toolName": "missing_tool",
  "data": {},
  "error": {
    "code": "TOOL_NOT_FOUND",
    "message": "Unknown MCP tool: missing_tool",
    "retryable": false
  },
  "metadata": {
    "durationMs": 1
  }
}
```

Write tool disabled:

```json
{
  "success": false,
  "toolName": "flowdesk_create_todo",
  "data": {},
  "error": {
    "code": "WRITE_TOOLS_DISABLED",
    "message": "MCP write tools are disabled. Set FLOWDESK_MCP_WRITE_TOOLS_ENABLED=true to enable this tool.",
    "retryable": false
  },
  "metadata": {
    "durationMs": 1
  }
}
```

Flowdesk uses these MCP error codes:

- `TOOL_NOT_FOUND`
- `VALIDATION_ERROR`
- `WRITE_TOOLS_DISABLED`
- `PERMISSION_DENIED`
- `TOOL_EXECUTION_FAILED`

Unexpected tool exceptions are returned as `TOOL_EXECUTION_FAILED` with a generic message. Stack traces, connection strings, and sensitive configuration values are not returned to clients.

## JSON-RPC Preview Response Shape

The JSON-RPC preview uses a minimal JSON-RPC 2.0 envelope:

```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "result": {
    "content": [
      {
        "type": "text",
        "text": "Tool flowdesk_search_knowledge returned structured content."
      }
    ],
    "structuredContent": {
      "success": true,
      "toolName": "flowdesk_search_knowledge",
      "data": {
        "query": "What should employees do before taking leave?",
        "topK": 3,
        "chunks": [],
        "citations": []
      },
      "error": null,
      "metadata": {
        "durationMs": 12
      }
    },
    "isError": false
  }
}
```

Unknown JSON-RPC methods return a protocol error:

```json
{
  "jsonrpc": "2.0",
  "id": "3",
  "error": {
    "code": -32601,
    "message": "Method not found"
  }
}
```

Invalid request or parameter shapes return protocol errors:

```json
{
  "jsonrpc": "2.0",
  "id": "bad-params",
  "error": {
    "code": -32602,
    "message": "Tool arguments must be an object"
  }
}
```

Tool-level failures, including `WRITE_TOOLS_DISABLED` and `TOOL_NOT_FOUND`, are returned as a JSON-RPC result with `isError=true` and the structured Flowdesk tool error in `structuredContent`.

## Audit Logging

Each tool call emits a structured application log entry with:

- tool name
- caller user id, or `anonymous` if no security context is present
- success flag
- error code
- duration in milliseconds
- sanitized argument summary

Argument logging masks sensitive keys such as password, secret, token, API key, authorization, and credential. Long string values are truncated before logging.

## Client Notes

Claude Desktop and many MCP clients expect a standard MCP transport, commonly stdio or a current HTTP transport. This preview exposes the tool model over authenticated Flowdesk HTTP endpoints for local experimentation and for future adapter work.

For clients that support custom HTTP tools, configure:

```json
{
  "name": "flowdesk-http-mcp-preview",
  "baseUrl": "http://localhost:8888/v1/mcp",
  "headers": {
    "Authorization": "Bearer <jwt-token>"
  }
}
```

Use a short-lived local token. Do not paste production credentials into public MCP client config files.

For JSON-RPC-capable clients that allow a custom HTTP endpoint, use:

```json
{
  "name": "flowdesk-jsonrpc-preview",
  "url": "http://localhost:8888/v1/mcp/jsonrpc",
  "headers": {
    "Authorization": "Bearer <jwt-token>"
  }
}
```

This is still a preview endpoint. It implements a minimal JSON-RPC compatibility surface for `initialize`, `ping`, `tools/list`, and `tools/call`, but it does not implement a full MCP stdio, SSE, or Streamable HTTP transport.

## Verification

```powershell
.\mvnw.cmd "-Dtest=*Mcp*,*Tool*" test
```

## References

- MCP tools specification: https://modelcontextprotocol.io/specification/2025-06-18/server/tools
- Spring AI MCP Server Boot Starter docs: https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html
