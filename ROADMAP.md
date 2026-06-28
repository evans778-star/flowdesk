# Flowdesk Roadmap

This roadmap keeps the public beta release, production-readiness work, and later agent/MCP expansion separate. Flowdesk v0.1.0 is a beta/demo/template release and is not recommended for direct production use until security, operations, and storage hardening are complete.

## v0.1.0 Beta Release

Scope: make the repository easy to review, run, and evaluate as a Java 21 / Spring Boot 3 AI office automation backend template.

- [x] Keep public-safe example configuration.
- [x] Add Maven Wrapper based CI.
- [x] Add Swagger/OpenAPI UI.
- [x] Add Demo Pack.
- [x] Add MCP Adapter Preview.
- [x] Add JSON-RPC Preview.
- [x] Add stdio MCP bridge preview.
- [x] Add RAG citations.
- [x] Add RAG Quality Lab.
- [x] Add CI / docs / release checklist.
- [x] Add request ID observability and production hardening docs.
- [ ] Add reviewed screenshots or short GIFs from local demo data.
- [ ] Create GitHub labels and starter issues from `docs/github-issue-pack.md`.
- [ ] Publish the first `v0.1.0` GitHub Release after final maintainer review.

## v0.2.0 Production Readiness

Scope: make the backend safer to adapt for real internal environments. This milestone should not be treated as a promise of turnkey production deployment.

- [ ] Add RBAC / permission model for users, todos, approvals, uploads, RAG, and MCP surfaces.
- [ ] Add permission-aware MCP tools that cannot bypass current-user boundaries.
- [ ] Add persistent audit logs for sensitive workflows and MCP tool calls.
- [ ] Add upload storage hardening, including durable storage guidance and stricter retention notes.
- [ ] Add rate limiting for login, upload, chat, RAG, and MCP calls.
- [ ] Add backup / deployment guidance for MongoDB, Redis Stack, uploads, and release rollback.
- [ ] Expand production health checks and operational runbooks.

## v0.3.0 Agent / MCP Expansion

Scope: improve AI developer adoption while keeping the preview APIs backward compatible.

- [ ] Complete standard MCP transport evaluation for Java 21 / Spring Boot 3.
- [ ] Decide whether to adopt a stable MCP SDK or continue the bridge strategy.
- [ ] Add more office tools for approvals, departments, groups, and document metadata.
- [ ] Add richer client examples for Claude Desktop, Cursor, Codex, and custom HTTP clients.
- [ ] Add MCP compatibility tests for initialize, ping, tools/list, tools/call, and error responses.
- [ ] Keep write tools disabled by default and document permission boundaries.

## Future

Scope: larger product directions that should wait until the backend release flow is stable.

- [ ] Add an optional frontend/admin console.
- [ ] Add Testcontainers integration tests for MongoDB and Redis Stack.
- [ ] Add multi-tenant support if there is a clear deployment target.
- [ ] Add advanced RAG evaluation with larger offline datasets and score summaries.
- [ ] Improve chunk metadata, including page ranges when available.
- [ ] Add observability examples for metrics and tracing.
