# Security Policy

## Supported Versions

Flowdesk is currently a template project. Security fixes should target the main development branch unless a stable release branch is introduced.

## Reporting a Vulnerability

Please do not open a public issue with exploit details or real secrets.

Preferred private reporting link:

https://github.com/evans778-star/flowdesk/security/advisories/new

If you find a vulnerability:

1. Create a private report through GitHub Security Advisories if available.
2. If private advisories are not available, contact the maintainer privately.
3. Include a concise description, affected area, reproduction steps, and suggested mitigation if known.

## Secret Handling

Never commit:

- DashScope API keys
- JWT secrets
- database credentials
- Redis passwords
- private URLs
- production hostnames
- local absolute paths
- `.env` files
- local `application-*.yml` files with real values

If a real secret was committed, rotate or revoke it immediately. Rewriting repository history may also be required depending on the exposure.

## Production Notes

- Use strong admin credentials.
- Set explicit CORS origins.
- Protect or disable Swagger UI in production if it should not be public.
- Review upload limits and allowed MIME types.
- Add rate limiting, audit logging, and monitoring before production use.
