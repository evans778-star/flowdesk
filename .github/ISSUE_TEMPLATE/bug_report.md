---
name: Bug report
about: Report a reproducible problem in Flowdesk
title: "[Bug]: "
labels: bug
assignees: ""
---

## Summary

Describe the problem clearly.

## Steps to Reproduce

1.
2.
3.

## Expected Behavior

What did you expect to happen?

## Actual Behavior

What happened instead?

## Environment

- OS:
- JDK version:
- Docker available: yes/no
- Spring profile:
- Branch or commit:
- AI/MCP/RAG enabled:
- Request ID if available:

## Logs with secrets removed

Paste only the smallest useful log excerpt. Remove API keys, JWTs, passwords, tokens, private URLs, local user paths, and production data.

## Verification

Please include relevant command output:

```powershell
git diff --check
.\mvnw.cmd test
.\mvnw.cmd package
```

## Security Check

- [ ] I did not include real API keys, JWT secrets, passwords, tokens, private URLs, or production data.
