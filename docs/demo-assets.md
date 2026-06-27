# Demo Assets

Use this guide when preparing screenshots or short GIFs for GitHub, release notes, or a demo post. Do not create screenshots from production data.

## Recommended Screenshot List

- README first viewport.
- Swagger UI.
- `/v3/api-docs`.
- MCP tools/list JSON.
- JSON-RPC initialize response.
- RAG citation response.
- demo-smoke PASS output.

## Recommended File Names

- `docs/assets/swagger-ui.png`
- `docs/assets/mcp-tools-list.png`
- `docs/assets/rag-citations-response.png`
- `docs/assets/demo-smoke-pass.png`

## Screenshot Safety Rules

- Do not show a real token.
- Do not show a real API key.
- Do not show a real private URL.
- Do not show real user data.
- Do not show real company documents.
- Replace authorization headers with `Bearer <jwt-token>`.
- Use placeholder values such as `<placeholder>` or `demo-local-token`.

## Capture Notes

For Swagger UI, use a local demo backend with placeholder credentials. For MCP tools/list and JSON-RPC initialize, capture local responses from `/v1/mcp/jsonrpc` or `/v1/mcp/tools` with write tools disabled. For RAG citation response screenshots, prefer synthetic sample documents and clearly show that `pageNumber` can be `null` when source page metadata is unavailable.

Do not reference an image from README until the actual asset exists and has been reviewed for secrets.
