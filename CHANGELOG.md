# Changelog

All notable Flowdesk changes are tracked here. This project currently uses beta release notes for public demo and template readiness.

## Unreleased

- Continue hardening the beta release path with clearer documentation, CI checks, and demo verification.
- Keep DashScope and Ollama optional so local verification can run without real model keys.

## v0.1.0-beta

This is a beta/demo/template release. It is suitable for learning, local demos, GitHub review, and internal adaptation. It is not a directly production-ready release without additional RBAC, rate limiting, monitoring, backup, and storage hardening.

Highlights:

- Java 21 / Spring Boot 3 backend template for AI office automation.
- User, department, and group APIs.
- Todo and approval workflow APIs.
- Validated file upload flow.
- AI chat with optional DashScope or Ollama providers.
- Redis Stack / RediSearch local RAG foundation.
- RAG citations for knowledge answers.
- RAG Quality Lab for offline retrieval checks.
- MCP HTTP Adapter Preview.
- MCP JSON-RPC Preview with initialize, ping, tools/list, and tools/call.
- stdio MCP Bridge Preview for local MCP clients.
- Demo Pack with Docker Compose and smoke verification.
- Observability with Request ID propagation.
- Production hardening documentation.

Security and release notes:

- No real secret is included in the release material.
- Demo credentials and tokens are placeholders only.
- Real AI answers require a configured Ollama or DashScope provider.
- Real vector retrieval requires Redis Stack plus an embedding provider.
