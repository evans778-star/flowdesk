# Production Hardening

Flowdesk is designed to run locally without external AI keys, then move to a stricter production profile when you are ready. Treat the demo and development defaults as examples, not as a production baseline.

## Environment Checklist

Set production values through environment variables or a platform secret manager:

| Area | Required setting |
| --- | --- |
| Runtime profile | `SPRING_PROFILES_ACTIVE=prod` |
| JWT | `JWT_SECRET` with a strong value of at least 32 bytes |
| Bootstrap user | `FLOWDESK_ADMIN_USER`, `FLOWDESK_ADMIN_PASSWORD` |
| MongoDB | `MONGODB_URI` for a managed or backed-up database |
| Redis Stack | `REDIS_HOST`, `REDIS_PORT`, and `REDIS_PASSWORD` when enabled |
| CORS | `APP_CORS_ALLOWED_ORIGINS` with explicit frontend origins |
| Uploads | `UPLOAD_SAVE_PATH`, `UPLOAD_HOST`, upload size, and allowed types |
| AI provider | `FLOWDESK_AI_ENABLED`, `FLOWDESK_AI_PROVIDER`, Ollama or DashScope settings |
| MCP | `FLOWDESK_MCP_ENABLED=false` unless intentionally exposed behind auth |

Do not commit `.env`, `application-local.yml`, logs, upload data, or real credentials.

## JWT And Authentication

Use a unique production `JWT_SECRET`; rotate it if it ever appears in logs, screenshots, commits, or support tickets. Most REST APIs require JWT. Keep Swagger/OpenAPI access restricted at the reverse proxy if public API discovery is not desired.

## CORS

Set `APP_CORS_ALLOWED_ORIGINS` to specific frontend origins. Avoid wildcard origins for authenticated browser traffic because Flowdesk allows credentialed requests.

## Uploads

Uploads are validated by extension, MIME type, size, and normalized path. Production deployments should store files on durable storage, keep upload directories outside source control, and review:

- `UPLOAD_SAVE_PATH`
- `UPLOAD_HOST`
- `UPLOAD_MAX_FILE_SIZE`
- `UPLOAD_MAX_REQUEST_SIZE`
- `UPLOAD_ALLOWED_EXTENSIONS`
- `UPLOAD_ALLOWED_CONTENT_TYPES`

Do not add APIs that accept arbitrary server-local file paths.

## MongoDB And Redis Stack

MongoDB stores users, departments, groups, todos, approvals, and chat logs. Redis Stack / RediSearch is used for local RAG vector retrieval. Use persistent volumes, backups, network controls, and resource limits before production use.

## AI Provider Configuration

`FLOWDESK_AI_ENABLED=false` lets the app start without real DashScope or Ollama credentials. Real chat answers and RAG retrieval quality require a configured provider:

- Ollama for local no-key demos.
- DashScope for cloud mode with a real key supplied outside git.

Changing embedding provider or embedding model can change vector dimensions, so rebuild the knowledge index after such changes.

## MCP Boundary

MCP is disabled by default with `FLOWDESK_MCP_ENABLED=false`. Write tools are separately disabled by default with `FLOWDESK_MCP_WRITE_TOOLS_ENABLED=false`.

Do not expose MCP endpoints directly to the public internet. Put them behind normal authentication, TLS, network policy, and audit logging. The Demo Pack and bridge preview are intended for local demo or trusted network use.

## Actuator Exposure

The default and prod profiles expose only:

- `/actuator/health`
- `/actuator/info`

Production keeps health details hidden with `management.endpoint.health.show-details=never`. Keep sensitive Actuator endpoints such as env, beans, mappings, and heapdump unavailable to anonymous users.

## Logging And Privacy

Every HTTP response includes `X-Request-Id`, and the same value is placed in the logging MDC as `requestId`. Log request ids, tool names, and bounded summaries. Do not log full JWT values, passwords, API keys, private URLs, uploaded file contents, or raw exception stacks to client responses.

## Demo Pack Is Not Production

`docker-compose.demo.yml` uses local placeholder settings and is meant for a quick demonstration. It is not hardened for internet exposure, secret management, backup policy, scaling, TLS, or long-running production data.
