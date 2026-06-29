# Public Release Checklist

Use this checklist before making Flowdesk public on GitHub. It focuses on keeping the repository safe, buildable, and easy for new contributors to evaluate.

## 1. Secret Safety

- Run a tracked-file secret-pattern scan:

  ```powershell
  git grep -n -I -E "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" -- .
  ```

- For local working-tree files that are not tracked by Git, run an additional broad scan before manually publishing or copying them:

  ```powershell
  rg --hidden --no-ignore -g '!target/**' -g '!.git/**' -n "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" .
  ```

- Confirm every match is one of:
  - an environment variable name,
  - a placeholder value,
  - a code identifier,
  - or safe documentation text.
- Never commit real DashScope API keys, JWT secrets, database credentials, Redis passwords, production URLs, private tokens, or local absolute paths.
- If a real secret was ever committed, replace it with an environment-variable placeholder and revoke the original value.

## 2. Local-Only Files

- Keep these files out of public commits:
  - `.env`
  - `.env.*`
  - `application-local.yml`
  - `src/main/resources/application-local.yml`
  - `src/main/resources/application-dev.yml`
  - `src/main/resources/application-prod.yml`
  - local logs and upload files
  - IDE and agent-tool state
- Keep `src/main/resources/application-example.yml` public-safe and complete enough for setup.

## 3. Tracked Sample Content

- Review tracked sample documents before release.
- Replace real company or employee documents with synthetic examples.
- Current synthetic RAG demo asset: `docs/examples/sample-employee-handbook.pdf`.

## 4. Build Confidence

Run these checks before release:

```powershell
git diff --check
.\mvnw.cmd test
.\mvnw.cmd package
```

The repository should build from a clean checkout using the Maven Wrapper.

## 5. Developer Experience

- README explains what Flowdesk is in the first screen.
- README includes quick start, required environment variables, Docker Compose usage, API docs, security notes, roadmap, and license.
- Swagger/OpenAPI is available or the README clearly states it is not yet available.
- Demo requests are available without real secrets.

## 6. Community Readiness

Before asking for stars or contributors, add:

- `CONTRIBUTING.md`
- `SECURITY.md`
- issue templates
- pull request template
- roadmap
- GitHub topics such as `spring-boot`, `java21`, `dashscope`, `rag`, `ai-agent`, `mongodb`, `redis`, and `websocket`
