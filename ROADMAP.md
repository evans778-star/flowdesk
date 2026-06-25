# Flowdesk Roadmap

This roadmap keeps public GitHub readiness separate from larger product work.

## Public Repository Readiness

- [x] Keep public-safe example configuration.
- [x] Add Maven Wrapper based CI.
- [x] Add Swagger/OpenAPI UI.
- [x] Add public release checklist.
- [x] Add demo guide and HTTP examples.
- [x] Add contribution and security docs.
- [x] Replace any real or ambiguous sample document with a synthetic public-safe sample.
- [x] Add architecture, configuration, RAG, and deployment docs.
- [ ] Add project screenshots or API documentation screenshots.
- [ ] Add release tags once the first stable public version is ready.

## Testing

- [x] Add baseline tests for JWT, security documentation access, upload validation, and login behavior.
- [ ] Add controller tests for todos, approvals, departments, and groups.
- [ ] Add integration tests with Testcontainers for MongoDB and Redis Stack.
- [ ] Add RAG tests that avoid real DashScope calls by using deterministic test doubles.

## Developer Experience

- [x] Document quick start and required environment variables.
- [x] Document Swagger and OpenAPI endpoints.
- [x] Add a synthetic demo PDF.
- [ ] Add richer OpenAPI annotations and examples.
- [ ] Add an application service to Docker Compose for one-command local startup.
- [ ] Add a seed script.

## Production Hardening

- [ ] Review JWT expiration and refresh-token strategy.
- [ ] Add request rate limiting guidance.
- [ ] Add structured audit logging for sensitive workflows.
- [ ] Add readiness checks for MongoDB, Redis Stack, and DashScope configuration.
- [ ] Document deployment profiles for staging and production.
