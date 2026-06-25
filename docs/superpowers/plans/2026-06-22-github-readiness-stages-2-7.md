# GitHub Readiness Stages 2-7 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Improve Flowdesk's GitHub readiness with verified builds, Swagger/OpenAPI, polished README, runnable demo docs, community files, and focused low-risk tests.

**Architecture:** Keep business behavior unchanged. Add springdoc as a documentation dependency, permit Swagger endpoints in security, document developer flows, and add tests around existing JWT/upload behavior plus a context smoke test that mocks infrastructure-heavy beans.

**Tech Stack:** Java 21, Spring Boot 3.2, Spring Security, springdoc-openapi, Maven Wrapper, JUnit 5, Mockito, MockMvc.

---

### Task 1: Stage Two Baseline Verification

**Files:**
- Read only

- [ ] **Step 1: Run whitespace check**

Run: `git diff --check`

Expected: exit code 0.

- [ ] **Step 2: Run tests**

Run: `.\mvnw.cmd test`

Expected: build success. Existing protobuf dependency metadata warnings may appear.

- [ ] **Step 3: Run package**

Run: `.\mvnw.cmd package`

Expected: build success.

### Task 2: Swagger/OpenAPI

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/java/com/aiwork/helper/security/SecurityConfig.java`
- Create: `src/test/java/com/aiwork/helper/security/SecurityConfigTest.java`

- [ ] **Step 1: Write failing security test**

Create a MockMvc security test that asserts `/v3/api-docs` and `/swagger-ui/index.html` do not return 401/403.

- [ ] **Step 2: Run the security test before implementation**

Run: `.\mvnw.cmd -Dtest=SecurityConfigTest test`

Expected: fail before Swagger/security changes.

- [ ] **Step 3: Add springdoc dependency and permit documentation endpoints**

Add `org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0` and permit `/v3/api-docs/**`, `/swagger-ui/**`, and `/swagger-ui.html`.

- [ ] **Step 4: Run the security test after implementation**

Run: `.\mvnw.cmd -Dtest=SecurityConfigTest test`

Expected: pass.

### Task 3: README Upgrade

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Rewrite README**

Replace README with a clear GitHub-facing bilingual overview, feature list, tech stack, architecture, quick start, environment variables, Swagger URL, RAG/DashScope notes, security notes, test commands, roadmap, license, and topics.

- [ ] **Step 2: Verify readable UTF-8**

Run: `Get-Content -Encoding UTF8 -LiteralPath README.md -TotalCount 40`

Expected: readable Markdown.

### Task 4: Demo Experience

**Files:**
- Create: `docs/demo.md`
- Create: `docs/api-examples.http`

- [ ] **Step 1: Add demo guide**

Document the local demo loop: dependencies, environment variables, service startup, login, upload, chat/RAG, and diagnostics.

- [ ] **Step 2: Add HTTP request examples**

Use placeholder variables only. Do not include real secrets.

### Task 5: Community Files

**Files:**
- Create: `CONTRIBUTING.md`
- Create: `SECURITY.md`
- Create: `ROADMAP.md`
- Create: `.github/ISSUE_TEMPLATE/bug_report.md`
- Create: `.github/ISSUE_TEMPLATE/feature_request.md`
- Create: `.github/pull_request_template.md`

- [ ] **Step 1: Add contribution and security docs**

Keep docs concise and honest.

- [ ] **Step 2: Add issue and PR templates**

Include build/test checklist and no-secret reminder.

### Task 6: Minimal Tests

**Files:**
- Create: `src/test/java/com/aiwork/helper/FlowdeskApplicationSmokeTest.java`
- Create: `src/test/java/com/aiwork/helper/security/JwtTokenProviderTest.java`
- Create: `src/test/java/com/aiwork/helper/controller/UploadControllerTest.java`
- Create: `src/test/java/com/aiwork/helper/service/impl/UserServiceImplTest.java`

- [ ] **Step 1: Add smoke test**

Load the Spring context with infrastructure-heavy beans mocked.

- [ ] **Step 2: Add JWT token provider test**

Assert generated tokens validate, resolve Bearer headers, and expose the configured user id.

- [ ] **Step 3: Add upload validation test**

Assert path traversal filenames and unsupported MIME types are rejected.

- [ ] **Step 4: Add user login service test**

Assert valid credentials return a token and wrong passwords throw `BusinessException`.

### Task 7: Final Verification

**Files:**
- Read only

- [ ] **Step 1: Run secret-pattern scan**

Run: `git grep -n -I -E "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" -- .`

Expected: no obvious real secrets.

- [ ] **Step 2: Run whitespace check**

Run: `git diff --check`

Expected: exit code 0.

- [ ] **Step 3: Run all tests**

Run: `.\mvnw.cmd test`

Expected: build success.

- [ ] **Step 4: Run package**

Run: `.\mvnw.cmd package`

Expected: build success.
