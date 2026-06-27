# Flowdesk Roadmap

This roadmap keeps public beta readiness separate from deeper product work. The project is a demo and backend template first; production use requires additional hardening.

## v0.1 Beta Readiness

Priority: high

- [x] Keep public-safe example configuration.
- [x] Add Maven Wrapper based CI.
- [x] Add Swagger/OpenAPI UI.
- [x] Add demo guide, Demo Pack, and HTTP examples.
- [x] Add RAG citations and offline RAG Quality Lab.
- [x] Add MCP HTTP Adapter Preview, JSON-RPC Preview, and stdio bridge preview.
- [x] Add request ID observability and production hardening docs.
- [x] Add release checklist and changelog.
- [ ] Add reviewed screenshots or short GIFs from local demo data.
- [ ] Create the first GitHub beta tag after final maintainer review.

## Production Hardening

Priority: high before real deployment

- [ ] Add RBAC or explicit authorization rules for sensitive business operations.
- [ ] Add rate limiting guidance or middleware for login, chat, upload, and MCP calls.
- [ ] Add structured audit logging for sensitive workflows.
- [ ] Add production backup guidance for MongoDB, Redis Stack, and uploaded files.
- [ ] Replace local upload storage with an object storage option or documented durable volume strategy.
- [ ] Add operational metrics beyond request ID logging.

## MCP Standard Transport

Priority: medium

- [ ] Track stable Java MCP SDK options compatible with Java 21 and Spring Boot 3.
- [ ] Keep HTTP and JSON-RPC previews backward compatible while standard transport is evaluated.
- [ ] Add conformance-oriented tests for initialize, tools/list, tools/call, and error responses.
- [ ] Document supported client configurations with short-lived local tokens only.

## RAG Quality Improvements

Priority: medium

- [ ] Add deterministic retrieval fixtures for local tests.
- [ ] Improve chunk metadata, including page ranges when available.
- [ ] Expand the offline Quality Lab dataset and scoring summary.
- [ ] Document index rebuild steps when embedding provider or vector dimension changes.

## Frontend / Admin Console Optional

Priority: low

- [ ] Consider a lightweight admin console only after backend release flow is stable.
- [ ] Prefer Swagger, HTTP examples, and demo scripts for the current beta.
- [ ] Avoid coupling the backend template to a frontend framework too early.
