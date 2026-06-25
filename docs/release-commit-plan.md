# Release Commit Plan

This document suggests how to split the current public-release work into reviewable commits. Do not include local secrets, logs, uploads, or machine-only files in any commit.

## Commit 1: `chore: harden public repository setup`

Suggested files:

- `.gitignore`
- `.github/workflows/ci.yml`
- `.github/ISSUE_TEMPLATE/bug_report.md`
- `.github/ISSUE_TEMPLATE/config.yml`
- `.github/ISSUE_TEMPLATE/feature_request.md`
- `.github/pull_request_template.md`
- `.github/repository-metadata.yml`
- `docker-compose.yml`
- `pom.xml`
- `src/main/java/com/aiwork/helper/FlowdeskApplication.java`
- `src/main/java/com/aiwork/helper/security/SecurityConfig.java`
- `src/main/resources/application.yml`
- `src/main/resources/application-dev.yml`
- `src/main/resources/application-prod.yml`
- `src/main/resources/application-example.yml`
- Delete old duplicate workflow: `.github/workflows/maven.yml`

Recommended verification before commit:

```powershell
rg --hidden --no-ignore -g '!target/**' -g '!.git/**' -g '!logs/**' -n "AKIA[0-9A-Z]{16}|-----BEGIN (RSA |DSA |EC |OPENSSH |)PRIVATE KEY-----|sk-[A-Za-z0-9]{20,}" .
git diff --check
docker compose config
.\mvnw.cmd test
```

## Commit 2: `docs: improve github onboarding`

Suggested files:

- `README.md`
- `CONTRIBUTING.md`
- `SECURITY.md`
- `ROADMAP.md`
- `docs/api-examples.http`
- `docs/architecture.md`
- `docs/configuration.md`
- `docs/demo.md`
- `docs/deployment.md`
- `docs/examples/README.md`
- `docs/examples/sample-employee-handbook.pdf`
- `docs/public-release-checklist.md`
- `docs/rag.md`
- `docs/release.md`
- `docs/release-commit-plan.md`
- Delete old root sample: `员工手册.pdf`

Recommended verification before commit:

```powershell
rg -n "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" .
git diff --check
```

## Commit 3: `test: add release-readiness coverage`

Suggested files:

- `src/test/java/com/aiwork/helper/FlowdeskApplicationSmokeTest.java`
- `src/test/java/com/aiwork/helper/ai/knowledge/PDFProcessorTest.java`
- `src/test/java/com/aiwork/helper/config/JwtPropertiesTest.java`
- `src/test/java/com/aiwork/helper/controller/UploadControllerTest.java`
- `src/test/java/com/aiwork/helper/controller/UserControllerTest.java`
- `src/test/java/com/aiwork/helper/security/JwtTokenProviderTest.java`
- `src/test/java/com/aiwork/helper/security/SecurityConfigTest.java`
- `src/test/java/com/aiwork/helper/service/impl/UserServiceImplTest.java`
- `src/test/java/com/aiwork/helper/testsupport/CiFriendlySpringBootTest.java`

Recommended verification before commit:

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

## Never Commit

- `.env`
- `.env.*`
- `logs/`
- `upload/`
- `target/`
- `src/main/resources/application-local.yml`
- local `application-dev.yml` or `application-prod.yml` files containing real values
- screenshots, issues, or docs containing real API keys, JWTs, database URIs, production URLs, or private hostnames

## Reviewer Checklist

- [ ] No real secrets or local-only paths are included.
- [ ] `application*.yml` files contain placeholders only.
- [ ] The sample PDF is synthetic and stored under `docs/examples/`.
- [ ] CI runs on both Ubuntu and Windows.
- [ ] README quick start matches `docker-compose.yml`.
- [ ] Swagger UI and OpenAPI endpoints are documented.
- [ ] Tests do not require local MongoDB, Redis, or DashScope.
- [ ] `.\mvnw.cmd test` and `.\mvnw.cmd package` pass locally.
