# Flowdesk MCP Bridge Preview

This is a small stdio bridge for MCP clients that expect to launch a local command. It forwards line-delimited JSON-RPC requests from stdin to Flowdesk's HTTP JSON-RPC preview at `/v1/mcp/jsonrpc`, then writes the Flowdesk response to stdout.

It is a bridge preview, not a replacement for a future native MCP stdio, SSE, or Streamable HTTP transport in the Flowdesk backend.

## Configuration

Use local placeholder values while testing:

```powershell
$env:FLOWDESK_MCP_BRIDGE_BASE_URL="http://localhost:8888"
$env:FLOWDESK_MCP_BRIDGE_TOKEN="<jwt-token>"
node tools/mcp-bridge/flowdesk-mcp-bridge.js
```

Environment variables:

| Variable | Purpose |
| --- | --- |
| `FLOWDESK_MCP_BRIDGE_BASE_URL` | Flowdesk base URL, for example `http://localhost:8888` |
| `FLOWDESK_MCP_BRIDGE_TOKEN` | Short-lived local JWT for Flowdesk MCP endpoints |

Do not place long-lived production JWTs in MCP client configuration files. Keep the bridge on a trusted local machine or trusted network, and keep Flowdesk write tools disabled unless you are intentionally testing writes.

## Test

```powershell
node --test tools/mcp-bridge/test/bridge.test.js
```
