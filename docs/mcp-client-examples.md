# MCP Client Examples

Flowdesk currently exposes an authenticated HTTP MCP Adapter Preview and a JSON-RPC Preview endpoint:

- `GET /v1/mcp/tools`
- `POST /v1/mcp/tools/{toolName}/call`
- `POST /v1/mcp/jsonrpc`

It does not yet expose a full standard MCP stdio, SSE, or Streamable HTTP transport. Use these examples only with clients that support custom HTTP tools or custom JSON-RPC HTTP calls.

## Local Safety Checklist

- Keep `FLOWDESK_MCP_ENABLED=false` unless you are actively testing.
- Keep `FLOWDESK_MCP_WRITE_TOOLS_ENABLED=false` for normal demos.
- Do not expose `/v1/mcp/**` directly to the public internet.
- Do not paste long-lived production JWTs into MCP client config files.
- Do not pass server-local file paths to MCP tools.

## Environment

```powershell
$env:FLOWDESK_AI_ENABLED="false"
$env:FLOWDESK_MCP_ENABLED="true"
$env:FLOWDESK_MCP_WRITE_TOOLS_ENABLED="false"
.\mvnw.cmd spring-boot:run
```

Use a local placeholder token in examples:

```text
Authorization: Bearer <jwt-token>
```

## Custom HTTP Tool Client

For clients that can call custom HTTP endpoints:

```json
{
  "name": "flowdesk-http-mcp-preview",
  "baseUrl": "http://localhost:8888/v1/mcp",
  "headers": {
    "Authorization": "Bearer <jwt-token>"
  },
  "toolsEndpoint": "/tools",
  "callEndpointTemplate": "/tools/{toolName}/call"
}
```

Tool call body:

```json
{
  "arguments": {
    "query": "What should employees do before taking leave?",
    "topK": 3
  }
}
```

## JSON-RPC Preview Client

For clients that can send JSON-RPC over HTTP:

```json
{
  "name": "flowdesk-jsonrpc-preview",
  "url": "http://localhost:8888/v1/mcp/jsonrpc",
  "headers": {
    "Authorization": "Bearer <jwt-token>"
  }
}
```

Initialize:

```json
{
  "jsonrpc": "2.0",
  "id": "init-1",
  "method": "initialize",
  "params": {
    "protocolVersion": "2025-06-18"
  }
}
```

Ping:

```json
{
  "jsonrpc": "2.0",
  "id": "ping-1",
  "method": "ping",
  "params": {}
}
```

List tools:

```json
{
  "jsonrpc": "2.0",
  "id": "1",
  "method": "tools/list",
  "params": {}
}
```

Call a tool:

```json
{
  "jsonrpc": "2.0",
  "id": "2",
  "method": "tools/call",
  "params": {
    "name": "flowdesk_upload_document_metadata",
    "arguments": {
      "fileName": "sample-employee-handbook.pdf",
      "contentType": "application/pdf",
      "sizeBytes": 123456
    }
  }
}
```

## Claude Desktop / Cursor / Codex Notes

Many MCP clients expect a real MCP transport such as stdio, SSE, or Streamable HTTP. The current Flowdesk endpoints are useful for local experimentation, HTTP-capable clients, and future adapter work, but they are not a drop-in Claude Desktop stdio server yet.

When a client only accepts standard MCP server configuration, use this project as the backend target for a small bridge process, or wait until Flowdesk adds a standard transport module. The bridge should:

- Authenticate to Flowdesk with a short-lived local token.
- Map `initialize` to `POST /v1/mcp/jsonrpc`.
- Map `ping` to `POST /v1/mcp/jsonrpc`.
- Map `tools/list` to `POST /v1/mcp/jsonrpc`.
- Map `tools/call` to `POST /v1/mcp/jsonrpc`.
- Preserve Flowdesk's `isError` and `structuredContent` fields.
- Avoid logging secrets or full JWTs.
