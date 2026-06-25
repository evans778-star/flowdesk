# Release Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the next GitHub-readiness pass by closing local security risks, adding CI and metadata, verifying Docker Compose docs, and making tests friendlier for clean CI environments.

**Architecture:** Keep behavior changes minimal. Treat public release readiness as repository hygiene: safe configuration examples, documentation, CI automation, and isolated tests that do not depend on a developer's local MongoDB or Redis state.

**Tech Stack:** Java 21, Spring Boot 3.2, Maven Wrapper, GitHub Actions, Docker Compose, JUnit 5, Mockito.

---

### Task 1: Safety Cleanup

**Files:**
- Modify: `src/main/resources/application-dev.yml`
- Modify: `src/main/resources/application-prod.yml`
- Local cleanup: `logs/*`
- Inspect: `员工手册.pdf`

- [ ] **Step 1: Clear ignored local logs**

Verify the absolute `logs` path is inside `D:\flowdesk\AIWorkHelper-Java\flowdesk`, then remove files inside that directory.

- [ ] **Step 2: Make dev/prod profiles public-safe**

Replace local-looking fallback passwords in `application-dev.yml` with empty environment-variable placeholders and comments. Keep `application-prod.yml` strict environment-variable only, adding defaults only where they are non-sensitive.

- [ ] **Step 3: Inspect tracked sample PDF**

Extract metadata/text where possible and decide whether the file appears safe for public release. Do not delete the PDF in this pass unless explicitly requested.

### Task 2: CI and Repository Metadata

**Files:**
- Create: `.github/workflows/ci.yml`
- Create: `.github/ISSUE_TEMPLATE/config.yml`
- Create: `.github/repository-metadata.yml`
- Modify: `README.md`

- [ ] **Step 1: Add GitHub Actions CI**

Run Java 21 on Windows, cache Maven dependencies, execute `.\mvnw.cmd test`, then `.\mvnw.cmd package`.

- [ ] **Step 2: Add GitHub issue template config and metadata notes**

Disable blank issues and document suggested repository description/topics in a tracked metadata file.

- [ ] **Step 3: Add README badges**

Add CI, Java 21, Spring Boot 3, License, and Docker Compose badges near the top of README.

### Task 3: Docker Compose and Quick Start Alignment

**Files:**
- Modify: `docker-compose.yml`
- Modify: `README.md`
- Modify: `docs/demo.md`

- [ ] **Step 1: Make Docker Compose explicit**

Add health checks for MongoDB and Redis Stack and avoid ambiguous `latest` image tags where practical.

- [ ] **Step 2: Align docs with Compose**

Update Quick Start and demo docs to mention MongoDB `27017`, Redis `6379`, RedisInsight `8001`, and the health-check command.

### Task 4: CI-Friendly Tests

**Files:**
- Create: `src/test/java/com/aiwork/helper/testsupport/CiFriendlySpringBootTest.java`
- Modify: `src/test/java/com/aiwork/helper/FlowdeskApplicationSmokeTest.java`
- Modify: `src/test/java/com/aiwork/helper/security/SecurityConfigTest.java`

- [ ] **Step 1: Centralize test properties**

Create a test annotation that excludes MongoDB and Redis auto-configuration and supplies placeholder secrets.

- [ ] **Step 2: Apply it to Spring context tests**

Use the annotation in smoke and Swagger security tests, and mock repository beans needed by services when data auto-configuration is disabled.

### Task 5: Verification

**Files:** none

- [ ] **Step 1: Run secret scan**

Run: `rg -n "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" .`

- [ ] **Step 2: Run diff whitespace check**

Run: `git diff --check`

- [ ] **Step 3: Run tests**

Run: `.\mvnw.cmd test`

- [ ] **Step 4: Run package**

Run: `.\mvnw.cmd package`

- [ ] **Step 5: Report remaining risks**

Summarize changed files, verification results, PDF status, Docker availability, and whether any secret-like content remains.
