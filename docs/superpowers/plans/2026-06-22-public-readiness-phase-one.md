# Public Readiness Phase One Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the repository safer to publish by tightening local-only ignores and documenting the first public-readiness checks without changing business behavior.

**Architecture:** This phase only changes repository hygiene and documentation. Runtime code, Spring configuration semantics, and build behavior remain unchanged.

**Tech Stack:** Java 21, Spring Boot 3, Maven Wrapper, GitHub repository hygiene.

---

### Task 1: Ignore Local-Only Runtime Configuration

**Files:**
- Modify: `.gitignore`

- [ ] **Step 1: Add resource-level local configuration ignores**

Add these entries under the application-specific ignore section:

```gitignore
src/main/resources/application-local.yml
src/main/resources/application-dev.yml
src/main/resources/application-prod.yml
```

- [ ] **Step 2: Verify ignore syntax**

Run: `git diff --check`

Expected: command exits with code 0 and prints no whitespace errors.

### Task 2: Add Public Release Checklist

**Files:**
- Create: `docs/public-release-checklist.md`

- [ ] **Step 1: Document safety checks**

Create a concise checklist covering secret scanning, local-only files, tracked sample files, build verification, Swagger/demo readiness, and community files.

- [ ] **Step 2: Verify documentation is plain UTF-8 Markdown**

Run: `Get-Content -Encoding UTF8 -LiteralPath docs\public-release-checklist.md -TotalCount 20`

Expected: Markdown text is readable.

### Task 3: Run Phase-One Verification

**Files:**
- Read only

- [ ] **Step 1: Run tracked-file secret-pattern scan**

Run: `git grep -n -I -E "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" -- .`

Expected: no obvious real secrets; known matches are placeholders, code identifiers, or documentation examples.

- [ ] **Step 2: Run whitespace check**

Run: `git diff --check`

Expected: command exits with code 0.

- [ ] **Step 3: Run Maven tests**

Run: `.\mvnw.cmd test`

Expected: build success. Existing Maven dependency metadata warnings may appear but should not fail the build.
