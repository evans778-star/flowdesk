# Release Checklist

Use this checklist before creating a GitHub release or tag. It is written for maintainers preparing a public beta release.

## Local Verification

Check the working tree first:

```powershell
git status --short --untracked-files=all
```

Check whitespace:

```powershell
git diff --check
```

Run the unit test suite:

```powershell
.\mvnw.cmd test
```

Build the Spring Boot jar:

```powershell
.\mvnw.cmd package
```

Run focused Demo Pack, MCP, bridge, and compose checks:

```powershell
.\mvnw.cmd "-Dtest=*Demo*,*Compose*,*Bridge*,*Mcp*" test
```

Run the Node bridge test when `tools/mcp-bridge` is present:

```powershell
node --test tools/mcp-bridge/test/bridge.test.js
```

Run the broad secret scan and review each hit:

```powershell
rg -n "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" README.md docs scripts tools src\main src\test docker-compose.yml docker-compose.demo.yml
```

Allowed hits include placeholders, test fake values, environment variable names, field names, security guidance, and the scan rules themselves.

Run the high-confidence secret scan. This command must produce no matches:

```powershell
rg --hidden --no-ignore -g '!target/**' -g '!.git/**' -g '!logs/**' -n "AKIA[0-9A-Z]{16}|-----BEGIN (RSA |DSA |EC |OPENSSH |)?PRIVATE KEY-----|sk-[A-Za-z0-9]{20,}" README.md docs scripts tools src\main src\test docker-compose.yml docker-compose.demo.yml
```

## GitHub Page Checklist

Complete these steps on the GitHub page after the branch is pushed and reviewed:

- Create labels from `docs/github-issue-pack.md`.
- Create 2-3 starter issues from `docs/github-issue-pack.md`.
- Confirm the PR is merged or the intended push is visible on GitHub.
- Confirm CI passed on the release branch or default branch.
- Create the `v0.1.0` tag.
- Draft a GitHub Release for `v0.1.0`.
- Paste the release notes from `docs/release-notes-v0.1.0.md`.
- Check the README, Release page, and Issues page after publishing.

## GitHub Release Content

Prepare:

- Tag name, for example `v0.1.0`.
- Release title.
- Highlights.
- Known limitations.
- Verification evidence with command names and pass/fail status.
- Link to `CHANGELOG.md`.
- Link to `docs/demo-walkthrough.md`.
- Link to `docs/release-notes-v0.1.0.md`.

## Do Not Upload

Do not attach or commit:

- `logs`
- `target`
- `upload`
- `.env`
- `application-local.yml`
- real screenshots with secrets
- private URLs
- real user or company data

## Release Notes Boundary

Flowdesk `v0.1.0` is a beta demo/template release. Do not describe it as production-ready until RBAC, rate limiting, monitoring, backup, and production storage are implemented and verified.
