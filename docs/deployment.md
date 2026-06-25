# Deployment Notes

Flowdesk can be packaged as a Spring Boot jar and deployed behind a reverse proxy or container platform. This document lists production checks rather than prescribing a specific platform.

## Build

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

The jar is written to:

```text
target/flowdesk-1.0.0-SNAPSHOT.jar
```

## Required Production Settings

Set:

```bash
SPRING_PROFILES_ACTIVE=prod
DASHSCOPE_API_KEY=...
JWT_SECRET=...
FLOWDESK_ADMIN_USER=...
FLOWDESK_ADMIN_PASSWORD=...
MONGODB_URI=...
REDIS_HOST=...
APP_CORS_ALLOWED_ORIGINS=...
UPLOAD_HOST=...
```

Use a strong `JWT_SECRET` and rotate it if it has ever appeared in logs, screenshots, commits, or issues.

## Infrastructure

| Dependency | Requirement |
| --- | --- |
| MongoDB | Persistent storage for users, todos, approvals, departments, chat logs |
| Redis Stack | RediSearch / vector retrieval for knowledge-base features |
| DashScope | Chat and embedding calls |
| File storage | Durable upload location if uploaded files must survive restarts |

## Health and API Docs

| Endpoint | Purpose |
| --- | --- |
| `/actuator/health` | Health check |
| `/swagger-ui/index.html` | Swagger UI |
| `/v3/api-docs` | OpenAPI JSON |

Protect Swagger in public production deployments if API discovery should not be anonymous.

## Security Checklist

- Use HTTPS at the edge.
- Set explicit CORS origins.
- Keep logs out of git and redact tokens from operational logs.
- Store secrets in a platform secret manager.
- Review upload size, file type, and storage limits.
- Add rate limiting and audit logging before exposing to untrusted users.
- Back up MongoDB and Redis data according to your recovery goals.
