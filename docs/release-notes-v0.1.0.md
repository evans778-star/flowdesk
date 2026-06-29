# Flowdesk v0.1.0 Beta

Flowdesk v0.1.0 Beta is a public demo and backend template release for Java 21 / Spring Boot 3 AI office automation work. This beta is suitable for demo, learning, GitHub review, and internal template adaptation. It is not recommended for direct production use without RBAC, rate limiting, monitoring, backup, production storage, and deployment-specific security controls.

## Highlights

- Java 21 / Spring Boot 3 backend template for AI office automation.
- User, department, group, todo, approval, and upload APIs.
- Optional DashScope or Ollama AI provider mode.
- Local RAG foundation with Redis Stack / RediSearch.
- RAG citations for knowledge answers.
- Offline RAG Quality Lab for simple retrieval checks.
- HTTP MCP Adapter Preview.
- JSON-RPC Preview with initialize, ping, tools/list, and tools/call.
- stdio MCP bridge preview for local command-based MCP clients.
- Demo Pack with Docker Compose and smoke verification.
- Request ID observability and production hardening docs.

## What works without API keys

| Capability | Works without API keys? | Notes |
| --- | --- | --- |
| Health check | Yes | `GET /actuator/health`. |
| Local login | Yes | Uses local placeholder demo credentials. |
| File upload metadata and normal REST shape checks | Yes | No model provider required. |
| MCP metadata and JSON-RPC initialize/ping/tools/list | Yes | Requires local JWT only. |
| RAG citation response shape | Yes | The response can include `citations` even when no real retrieval data is available. |

## AI/RAG requirements

- Real AI answer generation requires Ollama or DashScope.
- Real RAG retrieval requires Redis Stack + embedding provider configuration.
- Changing embedding providers or dimensions requires rebuilding the knowledge index.
- DashScope and Ollama remain optional; the application must still start without real model keys when AI is disabled.

## MCP status

- Current MCP support is an HTTP/JSON-RPC/stdio bridge preview.
- Flowdesk exposes HTTP MCP Adapter Preview endpoints under `/v1/mcp`.
- Flowdesk exposes JSON-RPC Preview at `/v1/mcp/jsonrpc`.
- `tools/mcp-bridge/flowdesk-mcp-bridge.js` provides a local stdio bridge preview for command-based MCP clients.
- This is not a full standard MCP transport yet.
- MCP is disabled by default.
- MCP write tools are disabled by default.

## Demo Pack

The fastest local demo path is:

```powershell
.\mvnw.cmd package
docker compose -f docker-compose.demo.yml up -d --build
.\scripts\demo-smoke.ps1
```

The demo compose starts MongoDB, Redis Stack, and the Flowdesk backend with AI disabled, MCP enabled, and write tools disabled.

## Security notes

- No real secrets are included.
- Demo credentials, JWT examples, tokens, and API key examples are placeholders only.
- Do not expose demo or MCP endpoints directly to the public internet.
- Do not paste long-lived production JWTs into MCP client configuration.
- Do not upload real internal documents into a public demo.

## Verification

Recommended verification before publishing this release:

```powershell
git status --short --untracked-files=all
git diff --check
.\mvnw.cmd "-Dtest=*IssuePack*,*ReleaseNotes*,*Roadmap*,*Readme*,*ReleaseChecklist*,*Docs*" test
.\mvnw.cmd test
.\mvnw.cmd package
node --test tools/mcp-bridge/test/bridge.test.js
```

Also run the broad and high-confidence secret scans from `docs/release-checklist.md`.

## Known limitations

- Flowdesk v0.1.0 Beta is not production-ready.
- RBAC / permission model work is not complete.
- MCP support is preview-level and not a full native standard transport.
- RAG page numbers can be `null` because current chunk metadata does not track exact page ranges.
- Real RAG retrieval requires indexed documents and an embedding provider.
- Demo compose is for local evaluation only.

## Recommended next issues

- Add reviewed screenshots and short demo GIFs.
- Add RBAC and permission model for production readiness.
- Track standard MCP transport compatibility.
