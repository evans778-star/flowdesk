# GitHub Issue Pack

Use this pack from the GitHub page after the release-prep branch is pushed. Codex should not create these labels or issues automatically for the first public release; the maintainer should review the wording and create them manually.

## Recommended Labels

| name | purpose | suggested color |
| --- | --- | --- |
| `type: feature` | New capability or user-visible improvement. | `#0E8A16` |
| `type: bug` | Reproducible defect or regression. | `#D73A4A` |
| `type: docs` | README, docs, examples, screenshots, release notes. | `#0075CA` |
| `type: test` | Unit, integration, smoke, CI, or test fixture work. | `#5319E7` |
| `type: chore` | Maintenance that does not change user-facing behavior. | `#C5DEF5` |
| `area: rag` | RAG retrieval, citations, chunks, vectors, or quality lab. | `#BFDADC` |
| `area: mcp` | MCP adapter, JSON-RPC preview, bridge, tools, schemas. | `#7057FF` |
| `area: security` | Auth, authorization, secrets, audit, upload safety. | `#B60205` |
| `area: demo` | Demo Pack, smoke script, screenshots, walkthroughs. | `#FBCA04` |
| `area: ci` | GitHub Actions, Maven checks, Node bridge tests. | `#1D76DB` |
| `priority: high` | Important before production-oriented adoption. | `#B60205` |
| `priority: medium` | Useful next-stage work. | `#FBCA04` |
| `priority: low` | Nice-to-have or future work. | `#D4C5F9` |
| `good first issue` | Small, bounded issue suitable for a new contributor. | `#7057FF` |
| `help wanted` | Maintainer welcomes external contribution. | `#008672` |
| `release-blocker` | Must be resolved before a named release. | `#000000` |

## Issue 1: Add reviewed screenshots and short demo GIFs

Labels: `type: docs`, `area: demo`, `good first issue`

```markdown
## Goal

Add public-safe screenshots or short GIFs so GitHub visitors can understand Flowdesk's demo path without running the project first.

## Scope

- Capture reviewed local demo assets for:
  - README first screen
  - Swagger/OpenAPI page
  - demo-smoke PASS output
  - MCP tools/list JSON response
  - RAG citations response
- Store approved assets under `docs/assets/`.
- Reference only reviewed assets from README or release notes.

## Safety checklist

- [ ] No real JWT token is visible.
- [ ] No real API key, password, private URL, local user path, or production data is visible.
- [ ] Screenshots use placeholder/demo data only.
- [ ] Screenshots were reviewed by a maintainer before being referenced.

## Acceptance criteria

- README or release notes show at least one reviewed visual asset.
- `docs/demo-assets.md` documents how the asset was captured.
- High-confidence secret scan has no matches.
```

## Issue 2: Add RBAC and permission model for production readiness

Labels: `type: feature`, `area: security`, `priority: high`

```markdown
## Goal

Define and implement a minimal role and permission boundary before Flowdesk is recommended for production-oriented adaptation.

## Scope

- Define roles such as admin, manager, and employee.
- Protect sensitive user, todo, approval, upload, RAG, and MCP operations.
- Prevent userId-based access bypasses.
- Add tests for allowed and denied access paths.
- Document the production security model.

## Non-goals

- No OAuth or SSO integration in this issue.
- No large rewrite of existing business logic.
- No frontend role-management console.

## Acceptance criteria

- Sensitive APIs have explicit authorization checks.
- MCP tools cannot access another user's data by accepting arbitrary `userId` input.
- Tests cover admin success, normal user success, and permission denied paths.
- README or production hardening docs clearly state current RBAC behavior and limits.
```

## Issue 3: Track standard MCP transport compatibility

Labels: `type: feature`, `area: mcp`, `priority: medium`

```markdown
## Goal

Move Flowdesk from HTTP/JSON-RPC MCP previews toward a standard MCP-compatible server path.

## Current status

- HTTP MCP Adapter Preview exists.
- JSON-RPC Preview exists at `/v1/mcp/jsonrpc`.
- stdio bridge preview exists under `tools/mcp-bridge`.
- Flowdesk backend is not yet a full native MCP transport server.

## Scope

- Evaluate stable Java 21 / Spring Boot 3 compatible MCP SDK options.
- Keep current HTTP and JSON-RPC preview APIs backward compatible.
- Add compatibility tests for:
  - initialize
  - ping
  - tools/list
  - tools/call
  - error responses
- Document Claude Desktop / Cursor / Codex bridge setup.

## Acceptance criteria

- Decision documented: standard SDK adoption or continued bridge strategy.
- Existing MCP preview tests still pass.
- No write tools are enabled by default.
- No real tokens or private endpoints appear in examples.
```

## GitHub Page Steps

Complete these from the GitHub page:

1. Create labels from the recommended label table.
2. Create the three issues above and apply the suggested labels.
3. Merge the release-prep PR or confirm the direct push after CI passes.
4. Publish the `v0.1.0` Release using `docs/release-notes-v0.1.0.md`.

## Safety Reminders

- Do not include real token/key/password/private URL values in issue bodies.
- Screenshots must be redacted and reviewed before publication.
- Release notes must not include internal deployment addresses.
- Keep production secrets, private hostnames, and real user data out of comments and attachments.
