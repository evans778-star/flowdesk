# Demo Walkthrough

This walkthrough is the shortest path for a GitHub visitor to verify that Flowdesk starts, exposes the demo APIs, and keeps AI providers optional.

## Requirements

- JDK 21
- Docker Desktop or compatible Docker runtime
- PowerShell
- Node.js 20 only if you want to run the MCP bridge test

## 1. Build The Jar

```powershell
.\mvnw.cmd package
```

## 2. Start Demo Compose

```powershell
docker compose -f docker-compose.demo.yml up -d --build
```

The demo compose starts MongoDB, Redis Stack, and the Flowdesk backend. It uses placeholder local values, enables MCP, disables write tools, and keeps AI disabled.

## 3. Run Smoke Checks

```powershell
.\scripts\demo-smoke.ps1
```

Equivalent path for shells that prefer forward slashes:

```powershell
.\scripts/demo-smoke.ps1
```

The smoke script checks health, login, MCP JSON-RPC initialize/ping/tools/list, a metadata tool call, write-tool disabled behavior, and the RAG citation response shape.

## 4. Login And Use The Token

```powershell
$loginBody = @{
  name = "flowdesk-local-owner"
  password = "local-only-bootstrap-password"
} | ConvertTo-Json

$login = Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8888/v1/user/login" `
  -ContentType "application/json" `
  -Body $loginBody

$token = "<jwt-token>"
```

Use the returned token as `Authorization: Bearer <jwt-token>`. Do not paste real production tokens into docs, screenshots, or issues. `demo-local-token` is acceptable for bridge examples that use a mock server.

## 5. Health

```powershell
Invoke-RestMethod "http://localhost:8888/actuator/health"
```

## 6. MCP JSON-RPC Initialize

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8888/v1/mcp/jsonrpc" `
  -Headers @{ Authorization = "Bearer <jwt-token>" } `
  -ContentType "application/json" `
  -Body '{"jsonrpc":"2.0","id":"init-1","method":"initialize","params":{"protocolVersion":"2025-06-18"}}'
```

## 7. MCP JSON-RPC Tools List

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8888/v1/mcp/jsonrpc" `
  -Headers @{ Authorization = "Bearer <jwt-token>" } `
  -ContentType "application/json" `
  -Body '{"jsonrpc":"2.0","id":"tools-1","method":"tools/list","params":{}}'
```

## 8. MCP JSON-RPC Tool Call

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8888/v1/mcp/jsonrpc" `
  -Headers @{ Authorization = "Bearer <jwt-token>" } `
  -ContentType "application/json" `
  -Body '{"jsonrpc":"2.0","id":"call-1","method":"tools/call","params":{"name":"flowdesk_upload_document_metadata","arguments":{"fileName":"handbook.pdf","contentType":"application/pdf"}}}'
```

## 9. RAG Citation Response Shape

```powershell
Invoke-RestMethod `
  -Method Post `
  -Uri "http://localhost:8888/v1/knowledge/chat-with-citations" `
  -Headers @{ Authorization = "Bearer <jwt-token>" } `
  -ContentType "application/json" `
  -Body '{"prompts":"What is in the employee handbook?","chatType":0}'
```

The response shape includes `citations`. Without a real indexed knowledge base, it may be empty.

## Notes

- Smoke checks run without real DashScope or Ollama keys.
- Real AI answers require Ollama or DashScope.
- Real RAG retrieval requires Redis Stack plus an embedding provider.
- Demo values such as `<placeholder>`, `<jwt-token>`, and `demo-local-token` are placeholders only.
