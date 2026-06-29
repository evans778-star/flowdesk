# Contributing to Flowdesk

Thanks for considering a contribution. Flowdesk is still a compact backend template, so small, focused changes are easier to review than broad rewrites.

## Development Setup

1. Install JDK 21.
2. Start local dependencies:

   ```powershell
   docker compose up -d
   ```

3. Configure local environment variables:

   ```powershell
   $env:SPRING_PROFILES_ACTIVE="dev"
   $env:DASHSCOPE_API_KEY="your-dashscope-api-key"
   $env:JWT_SECRET="replace-with-at-least-32-bytes-secret"
   ```

4. Verify the project:

   ```powershell
   git diff --check
   .\mvnw.cmd test
   .\mvnw.cmd package
   ```

## Contribution Guidelines

- Keep changes small and focused.
- Do not commit real secrets, local paths, logs, uploads, or production URLs.
- Prefer constructor injection and existing Spring Boot package conventions.
- Avoid business logic rewrites unless the issue requires them.
- Add tests for new behavior or bug fixes.
- Update README or demo docs when public behavior changes.

## Pull Request Checklist

- [ ] I ran `git diff --check`.
- [ ] I ran `.\mvnw.cmd test`.
- [ ] I ran `.\mvnw.cmd package`.
- [ ] I did not commit API keys, passwords, JWT secrets, tokens, private URLs, or local-only files.
- [ ] I updated documentation when relevant.
