# Release Checklist

Use this checklist before publishing Flowdesk as a public GitHub repository or tagging a release.

## 1. Security Checks

Run broad and high-confidence scans:

```powershell
rg -n "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" .
rg --hidden --no-ignore -g '!target/**' -g '!.git/**' -g '!logs/**' -n "AKIA[0-9A-Z]{16}|-----BEGIN (RSA |DSA |EC |OPENSSH |)PRIVATE KEY-----|sk-[A-Za-z0-9]{20,}" .
```

Confirm:

- `logs/` is empty or ignored.
- `upload/` is empty or ignored.
- `.env` and `.env.*` are ignored.
- `application.yml`, `application-dev.yml`, and `application-prod.yml` contain placeholders only.
- `docs/examples/sample-employee-handbook.pdf` is synthetic demo content.
- No real API keys, JWTs, database credentials, Redis passwords, production URLs, or private hostnames appear in docs, tests, issues, or logs.

## 2. GitHub Repository Settings

Suggested description:

```text
AI office automation backend template with Spring Boot 3, Java 21, DashScope, MongoDB, Redis Stack, WebSocket, and RAG.
```

Suggested topics:

```text
spring-boot, java21, spring-ai, spring-ai-alibaba, dashscope, rag, ai-agent, office-automation, mongodb, redis, websocket
```

Confirm:

- License is MIT.
- Homepage points to the repository or project docs.
- GitHub Security Advisories are enabled.
- Issue templates and PR template are visible.
- README badge points to the real repository workflow.

## 3. CI Checks

Confirm GitHub Actions runs:

- Ubuntu Java 21 build.
- Windows Java 21 build.
- `git diff --check`.
- High-confidence secret scan.
- Maven test.
- Maven package.

Local verification:

```powershell
git diff --check
.\mvnw.cmd test
.\mvnw.cmd package
```

## 4. Local Startup Checks

Start dependencies:

```powershell
docker compose up -d
docker compose ps
```

Expected:

- `flowdesk-mongodb` is healthy.
- `flowdesk-redis-stack` is healthy.

Set placeholder local values:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:FLOWDESK_AI_ENABLED="false"
$env:DASHSCOPE_API_KEY="test-dashscope-api-key"
$env:JWT_SECRET="test-jwt-secret-value-with-at-least-32-bytes"
$env:FLOWDESK_ADMIN_USER="flowdesk-local-owner"
$env:FLOWDESK_ADMIN_PASSWORD="local-only-bootstrap-password"
$env:FLOWDESK_USER_DEFAULT_PASSWORD="local-only-user-password"
# Optional Windows workaround if Tomcat NIO fails with "Unable to establish loopback connection":
# $env:FLOWDESK_TOMCAT_PROTOCOL="org.apache.coyote.http11.Http11Nio2Protocol"
# $env:REDIS_CLIENT_TYPE="jedis"
```

Run the app:

```powershell
java -jar target/flowdesk-1.0.0-SNAPSHOT.jar
```

Check:

- `GET http://localhost:8888/actuator/health`
- `GET http://localhost:8888/swagger-ui/index.html`
- `POST http://localhost:8888/v1/user/login`
- `POST http://localhost:8888/v1/upload/file` with `docs/examples/sample-employee-handbook.pdf`

With `FLOWDESK_AI_ENABLED=false`, AI-only beans are not loaded and placeholder DashScope values are only for startup and non-AI endpoint checks. AI and RAG calls require `FLOWDESK_AI_ENABLED=true` and a real `DASHSCOPE_API_KEY`.

## 5. Tag and Release

Recommended first public version:

```text
v0.1.0
```

Release notes should include:

- What Flowdesk currently includes.
- Known limitations.
- Quick start link.
- Security and configuration notes.
- Demo PDF and API examples.

## 6. After Publishing

- Confirm README badge is green.
- Confirm Actions are passing on the default branch.
- Open a test issue preview to verify templates.
- Check Swagger/OpenAPI docs after local startup.
- Re-run secret scans after any docs or sample changes.
