# Flowdesk Demo Pack

The Demo Pack is the shortest local path for evaluating Flowdesk with MongoDB, Redis Stack, the Spring Boot backend, RAG citation response shapes, and the MCP JSON-RPC preview.

It is for local demo use only. It is not a production deployment, and it should not be exposed directly to the public internet.

## What Starts

`docker-compose.demo.yml` starts:

- MongoDB on `localhost:27017`
- Redis Stack on `localhost:6379`
- RedisInsight on `localhost:8001`
- Flowdesk backend on `localhost:8888`
- Flowdesk WebSocket port on `localhost:9000`

The backend uses a jar-based Dockerfile. Build the jar before starting the demo compose:

```powershell
.\mvnw.cmd package
docker compose -f docker-compose.demo.yml up -d --build
.\scripts\demo-smoke.ps1
```

Stop the demo:

```powershell
docker compose -f docker-compose.demo.yml down
```

## Safe Defaults

The demo compose intentionally uses local placeholder values:

```text
FLOWDESK_AI_ENABLED=false
FLOWDESK_MCP_ENABLED=true
FLOWDESK_MCP_WRITE_TOOLS_ENABLED=false
FLOWDESK_ADMIN_USER=flowdesk-local-owner
FLOWDESK_ADMIN_PASSWORD=local-only-bootstrap-password
```

With `FLOWDESK_AI_ENABLED=false`, Flowdesk can start without a real DashScope key or a running Ollama model. Real AI answers and real RAG retrieval still require an embedding provider.

With `FLOWDESK_MCP_WRITE_TOOLS_ENABLED=false`, write-capable MCP tools return `WRITE_TOOLS_DISABLED`.

## Smoke Checks

`scripts/demo-smoke.ps1` checks:

- `GET /actuator/health`
- `POST /v1/user/login`
- MCP `initialize`
- MCP `ping`
- MCP `tools/list`
- `flowdesk_upload_document_metadata`
- `flowdesk_create_todo` returns `WRITE_TOOLS_DISABLED`
- `POST /v1/knowledge/chat-with-citations` returns a response with `citations`

Optional parameters:

```powershell
.\scripts\demo-smoke.ps1 `
  -BaseUrl "http://localhost:8888" `
  -Username "flowdesk-local-owner" `
  -Password "local-only-bootstrap-password"
```

## Security Boundaries

- Do not expose the demo compose directly to the public internet.
- Do not use real API keys, production JWTs, production passwords, or private URLs in demo files.
- Do not upload real internal documents into a public demo.
- Do not pass server-local file paths to MCP tools.
- Keep MCP write tools disabled unless you are intentionally testing writes on local throwaway data.
- Rotate any token used by an MCP client bridge after a demo.
