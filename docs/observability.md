# Observability

Flowdesk includes lightweight request correlation and conservative health endpoints. The goal is to make local debugging and production log search easier without adding an external observability stack.

## Request ID

Every HTTP request is handled by `RequestIdFilter`.

- If the client sends `X-Request-Id`, Flowdesk propagates that value.
- If the header is absent, Flowdesk generates a UUID.
- The response always includes `X-Request-Id`.
- The current value is stored in SLF4J MDC as `requestId` during request handling.
- MDC is cleared after the request completes.

Example:

```bash
curl -i http://localhost:8888/actuator/health \
  -H "X-Request-Id: local-demo-001"
```

Response headers include:

```text
X-Request-Id: local-demo-001
```

The default log pattern includes:

```text
requestId=local-demo-001
```

Use the request id from an API response to locate matching application logs.

## Error Responses

Flowdesk keeps the existing response shape for REST errors:

```json
{
  "code": 500,
  "msg": "System error"
}
```

Business and validation errors can return their existing user-facing messages. Unexpected exceptions are logged server-side and returned as a generic message so stack traces, connection strings, and sensitive values do not leak to clients.

MCP tool errors use the MCP adapter response shape:

```json
{
  "success": false,
  "toolName": "flowdesk_create_todo",
  "error": {
    "code": "WRITE_TOOLS_DISABLED",
    "message": "Write tools are disabled.",
    "retryable": false
  }
}
```

## Health Checks

Use:

- `/actuator/health` for liveness and basic dependency health.
- `/actuator/info` for non-sensitive build or runtime information if configured.

MongoDB and Redis health can affect the health response when their auto-configurations are active. AI providers remain optional: with `FLOWDESK_AI_ENABLED=false`, missing DashScope or Ollama configuration should not prevent startup.

The Demo Pack smoke script checks API shape and MCP preview behavior. Production monitoring should add uptime checks, log aggregation, latency metrics, error rates, storage capacity, and dependency alerts.

## Redaction Rules

Do not log full values for:

- JWT or bearer token strings
- passwords
- secrets
- API keys
- private URLs
- private keys
- uploaded document contents

For user-provided query or title fields, log short sanitized summaries only. MCP audit logging follows the same redaction approach.
