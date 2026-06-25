# Configuration

Flowdesk uses environment variables for secrets and deploy-specific values. Public configuration files should contain placeholders only.

## Profiles

| Profile | File | Use |
| --- | --- | --- |
| default | `application.yml` | Shared non-secret defaults |
| dev | `application-dev.yml` | Local development, still driven by environment variables |
| prod | `application-prod.yml` | Production deployment, all sensitive values required from the environment |
| local | `application-local.yml` | Optional ignored local overrides |

Set a profile with:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
```

## Required Variables

| Variable | Description | Example |
| --- | --- | --- |
| `JWT_SECRET` | JWT signing secret, at least 32 bytes recommended | `replace-with-at-least-32-bytes-secret` |
| `FLOWDESK_ADMIN_USER` | Bootstrap admin username | `flowdesk-local-owner` |
| `FLOWDESK_ADMIN_PASSWORD` | Bootstrap admin password | `local-only-bootstrap-password` |

## Local Dependency Variables

| Variable | Default | Description |
| --- | --- | --- |
| `MONGODB_HOST` | `127.0.0.1` | MongoDB host for local dev |
| `MONGODB_PORT` | `27017` | MongoDB port |
| `MONGODB_DATABASE` | `Flowdesk` | MongoDB database |
| `REDIS_HOST` | `127.0.0.1` | Redis Stack host |
| `REDIS_PORT` | `6379` | Redis Stack port |
| `REDIS_DATABASE` | `0` | Redis logical database |
| `REDIS_CLIENT_TYPE` | `lettuce` | Redis client. Use `jedis` if Lettuce/Netty fails to open selectors on a Windows host. |

## Production Variables

| Variable | Description |
| --- | --- |
| `MONGODB_URI` | Production MongoDB URI |
| `REDIS_PASSWORD` | Redis password, if enabled |
| `APP_CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins |
| `UPLOAD_SAVE_PATH` | Upload directory |
| `UPLOAD_HOST` | Public file host/base URL |
| `FLOWDESK_USER_DEFAULT_PASSWORD` | Optional default password for created users |
| `FLOWDESK_TOMCAT_PROTOCOL` | Optional Tomcat connector protocol override. Use `org.apache.coyote.http11.Http11Nio2Protocol` if a Windows host fails with `Unable to establish loopback connection`. |

## AI Options

| Variable | Default | Description |
| --- | --- | --- |
| `FLOWDESK_AI_ENABLED` | `false` in default/dev | Enables AI provider beans, knowledge diagnostics, and RAG beans |
| `FLOWDESK_AI_PROVIDER` | `ollama` | AI backend provider: `ollama` or `dashscope` |
| `OLLAMA_BASE_URL` | `http://localhost:11434` | Local Ollama API endpoint |
| `OLLAMA_CHAT_MODEL` | `qwen2.5:7b` | Ollama chat model |
| `OLLAMA_EMBEDDING_MODEL` | `nomic-embed-text` | Ollama embedding model |
| `OLLAMA_TIMEOUT` | `30s` | Ollama HTTP timeout |
| `FLOWDESK_AI_EMBEDDING_DIMENSION` | provider default | Vector dimension override. Ollama default is 768; DashScope default is 1024 |
| `FLOWDESK_AI_VECTOR_INDEX_NAME` | provider-specific | Optional Redis Stack index name override |
| `DASHSCOPE_API_KEY` | none | Required only when `FLOWDESK_AI_PROVIDER=dashscope` |
| `DASHSCOPE_BASE_URL` | `https://dashscope.aliyuncs.com/compatible-mode/v1` | DashScope OpenAI-compatible endpoint |
| `DASHSCOPE_CHAT_MODEL` | `qwen-max` | Chat model |
| `DASHSCOPE_CHAT_TEMPERATURE` | `0.7` | Chat sampling temperature |
| `DASHSCOPE_EMBEDDING_MODEL` | `text-embedding-v3` | Embedding model |
| `AI_MEMORY_MAX_TOKEN_LIMIT` | `2000` | Approximate per-session memory threshold |

For a first local startup, keep `FLOWDESK_AI_ENABLED=false`. Health, Swagger, login, upload, and non-AI APIs should still start.

Use Ollama for a no-key local AI demo:

```powershell
ollama pull qwen2.5:7b
ollama pull nomic-embed-text
ollama serve

$env:FLOWDESK_AI_ENABLED="true"
$env:FLOWDESK_AI_PROVIDER="ollama"
$env:OLLAMA_BASE_URL="http://localhost:11434"
```

Use DashScope for cloud mode:

```powershell
$env:FLOWDESK_AI_ENABLED="true"
$env:FLOWDESK_AI_PROVIDER="dashscope"
$env:DASHSCOPE_API_KEY="your-dashscope-api-key"
```

If you change embedding providers or embedding models, rebuild the knowledge index because vector dimensions may differ.

In `prod`, set `FLOWDESK_AI_ENABLED` explicitly to either `true` or `false` so the deployment decision is visible.

## Safety Rules

- Do not commit `.env`, `application-local.yml`, logs, upload files, or real credentials.
- Rotate any credential that was ever exposed in a commit, issue, screenshot, or log.
- Keep production Swagger access restricted if the deployment is public.
