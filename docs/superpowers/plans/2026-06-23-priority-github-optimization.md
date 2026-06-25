# Priority GitHub Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the highest-impact GitHub readiness improvements: public-safe sample assets, reliable cross-platform CI, clean Maven output, better first-screen README conversion, core docs, and low-risk tests.

**Architecture:** Keep application behavior stable and focus on repo polish. Use docs and configuration to improve onboarding, dependency management to clean build output, and test-only helpers to keep CI deterministic without requiring local MongoDB or Redis.

**Tech Stack:** Java 21, Spring Boot 3.2, Maven Wrapper, GitHub Actions, Docker Compose, JUnit 5, Spring MockMvc, PDFBox.

---

### Task 1: Public Safety and Sample Asset

**Files:**
- Move: `员工手册.pdf` to `docs/examples/sample-employee-handbook.pdf`
- Create: `docs/examples/README.md`
- Modify: `.gitignore`
- Modify: `README.md`
- Modify: `docs/demo.md`

- [ ] **Step 1: Move sample PDF into docs/examples**

Move the PDF after verifying source and destination stay inside the repository.

- [ ] **Step 2: Document the sample as synthetic**

Add `docs/examples/README.md` explaining that the PDF is synthetic demo material and not real company data.

- [ ] **Step 3: Confirm local runtime outputs are ignored**

Ensure logs, upload directories, `.env`, local Spring profiles, and target outputs are ignored.

### Task 2: CI and Maven Warning Cleanup

**Files:**
- Modify: `.github/workflows/ci.yml`
- Delete: `.github/workflows/maven.yml`
- Modify: `pom.xml`
- Modify: `README.md`

- [ ] **Step 1: Consolidate GitHub Actions**

Use one CI workflow with `windows-latest` and `ubuntu-latest`, plus `git diff --check`, secret-pattern scan, test, and package steps.

- [ ] **Step 2: Clean protobuf warning**

Add dependency management for `protobuf-java` and `protobuf-java-util` to override the bad transitive POM metadata version.

- [ ] **Step 3: Replace badge repository URL**

Use `evans778-star/flowdesk` in README badges and metadata links.

### Task 3: Quick Start and Core Docs

**Files:**
- Create: `docs/architecture.md`
- Create: `docs/configuration.md`
- Create: `docs/rag.md`
- Create: `docs/deployment.md`
- Modify: `README.md`
- Modify: `ROADMAP.md`
- Modify: `SECURITY.md`

- [ ] **Step 1: Add architecture documentation**

Describe HTTP modules, service layer, AI tools, WebSocket, MongoDB, Redis Stack, and RAG flow.

- [ ] **Step 2: Add configuration documentation**

Document all important environment variables and local/production profile behavior.

- [ ] **Step 3: Add RAG documentation**

Explain the DashScope + PDFBox + Redis Stack flow and current limitations.

- [ ] **Step 4: Add deployment documentation**

Document production profile expectations, secret handling, CORS, uploads, logs, and health checks.

### Task 4: Low-Risk Test Coverage

**Files:**
- Create: `src/test/java/com/aiwork/helper/config/JwtPropertiesTest.java`
- Create: `src/test/java/com/aiwork/helper/controller/UserControllerTest.java`
- Create: `src/test/java/com/aiwork/helper/ai/knowledge/PDFProcessorTest.java`

- [ ] **Step 1: Add config loading test**

Verify `JwtProperties` binds placeholder test values correctly.

- [ ] **Step 2: Add controller MockMvc test**

Verify `/v1/user/login` accepts a login request and returns the service token through the public response shape.

- [ ] **Step 3: Add PDF chunking test**

Verify the synthetic handbook PDF can be parsed and split into chunks without external services.

### Task 5: Verification

**Files:** none

- [ ] **Step 1: Run security scan**

Run `rg -n "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" .` and review matches.

- [ ] **Step 2: Run whitespace check**

Run `git diff --check`.

- [ ] **Step 3: Run Docker Compose parse check**

Run `docker compose config`.

- [ ] **Step 4: Run tests and package**

Run `.\mvnw.cmd test` and `.\mvnw.cmd package`.

- [ ] **Step 5: Verify protobuf warning is gone**

Read Maven output and confirm the previous `protobuf-java-util:3.22.1` invalid POM warning no longer appears.
