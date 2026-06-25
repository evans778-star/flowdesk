# Flowdesk Local Demo

This demo shows the shortest local loop for evaluating Flowdesk. It uses placeholder credentials and assumes a local development environment.

## 1. Start Dependencies

From the repository root:

```powershell
docker compose up -d
docker compose ps
```

This starts:

- MongoDB on `localhost:27017`
- Redis Stack on `localhost:6379`
- RedisInsight on `http://localhost:8001`

## 2. Configure Environment Variables

PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:FLOWDESK_AI_ENABLED="false"
$env:FLOWDESK_AI_PROVIDER="ollama"
$env:DASHSCOPE_API_KEY="test-dashscope-api-key"
$env:JWT_SECRET="replace-with-at-least-32-bytes-secret"
$env:FLOWDESK_ADMIN_USER="flowdesk-local-owner"
$env:FLOWDESK_ADMIN_PASSWORD="local-only-bootstrap-password"
```

Optional local override for created users:

```powershell
$env:FLOWDESK_USER_DEFAULT_PASSWORD="local-only-user-password"
```

Do not use real production values in examples, screenshots, issues, or commits.
This baseline starts Flowdesk without loading AI provider beans. To test AI chat, embeddings, RAG, or AI diagnostics, enable either Ollama or DashScope.

No-key Ollama demo:

```powershell
ollama pull qwen2.5:7b
ollama pull nomic-embed-text
ollama serve

$env:FLOWDESK_AI_ENABLED="true"
$env:FLOWDESK_AI_PROVIDER="ollama"
$env:OLLAMA_BASE_URL="http://localhost:11434"
$env:OLLAMA_CHAT_MODEL="qwen2.5:7b"
$env:OLLAMA_EMBEDDING_MODEL="nomic-embed-text"
```

DashScope cloud mode:

```powershell
$env:FLOWDESK_AI_ENABLED="true"
$env:FLOWDESK_AI_PROVIDER="dashscope"
$env:DASHSCOPE_API_KEY="your-dashscope-api-key"
```

## 3. Build and Run

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
java -jar target/flowdesk-1.0.0-SNAPSHOT.jar
```

If a Windows JDK/Tomcat startup fails with `Unable to establish loopback connection`, retry with:

```powershell
$env:FLOWDESK_TOMCAT_PROTOCOL="org.apache.coyote.http11.Http11Nio2Protocol"
$env:REDIS_CLIENT_TYPE="jedis"
java -jar target/flowdesk-1.0.0-SNAPSHOT.jar
```

Open:

- API base: `http://localhost:8888`
- Swagger UI: `http://localhost:8888/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8888/v3/api-docs`

## 4. Login

Use the configured admin credentials:

```http
POST http://localhost:8888/v1/user/login
Content-Type: application/json

{
  "name": "flowdesk-local-owner",
  "password": "local-only-bootstrap-password"
}
```

Copy the returned `token` into an `Authorization: Bearer <token>` header for protected APIs.

## 5. Upload a File

```http
POST http://localhost:8888/v1/upload/file
Authorization: Bearer {{token}}
Content-Type: multipart/form-data

file=@./docs/examples/sample-employee-handbook.pdf
```

You can use the synthetic sample PDF at `docs/examples/sample-employee-handbook.pdf`.
Do not upload real internal documents to a public demo.

## 6. Chat

```http
POST http://localhost:8888/v1/chat
Authorization: Bearer {{token}}
Content-Type: application/json

{
  "prompts": "Summarize the uploaded document and list the follow-up actions.",
  "chatType": 0,
  "relationId": "local-demo"
}
```

With `FLOWDESK_AI_ENABLED=false`, this call returns a clear AI-disabled message. To get a model response, restart with `FLOWDESK_AI_ENABLED=true` and either `FLOWDESK_AI_PROVIDER=ollama` with local Ollama running, or `FLOWDESK_AI_PROVIDER=dashscope` with a valid DashScope API key.

## 7. Knowledge Diagnostics

The diagnostic endpoints are registered only under the `dev` profile when `FLOWDESK_AI_ENABLED=true`.
If you switch embedding providers or models, clear and rebuild the local knowledge index before comparing RAG results.

```http
GET http://localhost:8888/api/knowledge/diag/status
Authorization: Bearer {{token}}
```

```http
GET http://localhost:8888/api/knowledge/diag/search?query=attendance%20policy
Authorization: Bearer {{token}}
```

## 8. Stop Local Dependencies

```powershell
docker compose down
```

Use `docker compose down -v` only when you intentionally want to delete local MongoDB and Redis data.
