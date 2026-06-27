#!/usr/bin/env node

const http = require('node:http');
const https = require('node:https');
const readline = require('node:readline');

function buildJsonRpcEndpoint(baseUrl) {
  const normalized = (baseUrl || 'http://localhost:8888').replace(/\/+$/, '');
  if (normalized.endsWith('/v1/mcp/jsonrpc')) {
    return normalized;
  }
  if (normalized.endsWith('/v1/mcp')) {
    return `${normalized}/jsonrpc`;
  }
  return `${normalized}/v1/mcp/jsonrpc`;
}

function buildAuthorizationHeader(token) {
  return token ? `Bearer ${token}` : undefined;
}

function sanitizeError(error, token) {
  let message = error && error.message ? String(error.message) : 'Flowdesk MCP bridge request failed';
  if (token) {
    message = message.split(token).join('[REDACTED]');
  }
  return message;
}

async function forwardJsonRpc(request, options = {}) {
  const endpoint = buildJsonRpcEndpoint(options.baseUrl || process.env.FLOWDESK_MCP_BRIDGE_BASE_URL);
  const token = options.token || process.env.FLOWDESK_MCP_BRIDGE_TOKEN || '';
  const body = JSON.stringify(request);
  const url = new URL(endpoint);
  const client = url.protocol === 'https:' ? https : http;

  return new Promise((resolve, reject) => {
    const headers = {
      'Content-Type': 'application/json',
      'Content-Length': Buffer.byteLength(body),
    };
    const authorization = buildAuthorizationHeader(token);
    if (authorization) {
      headers.Authorization = authorization;
    }

    const req = client.request(
      {
        protocol: url.protocol,
        hostname: url.hostname,
        port: url.port,
        path: `${url.pathname}${url.search}`,
        method: 'POST',
        headers,
      },
      (res) => {
        let raw = '';
        res.setEncoding('utf8');
        res.on('data', (chunk) => {
          raw += chunk;
        });
        res.on('end', () => {
          if (res.statusCode < 200 || res.statusCode >= 300) {
            reject(new Error(`Flowdesk returned HTTP ${res.statusCode}`));
            return;
          }
          try {
            resolve(JSON.parse(raw));
          } catch (error) {
            reject(new Error(`Flowdesk returned invalid JSON: ${error.message}`));
          }
        });
      }
    );

    req.on('error', reject);
    req.write(body);
    req.end();
  });
}

function jsonRpcError(id, code, message) {
  return {
    jsonrpc: '2.0',
    id: id ?? null,
    error: {
      code,
      message,
    },
  };
}

async function handleLine(line, options = {}) {
  let request;
  try {
    request = JSON.parse(line);
  } catch {
    return jsonRpcError(null, -32700, 'Parse error');
  }

  try {
    return await forwardJsonRpc(request, options);
  } catch (error) {
    const token = options.token || process.env.FLOWDESK_MCP_BRIDGE_TOKEN || '';
    return jsonRpcError(request.id, -32603, sanitizeError(error, token));
  }
}

async function runStdioBridge(options = {}) {
  const rl = readline.createInterface({
    input: process.stdin,
    crlfDelay: Infinity,
  });

  for await (const line of rl) {
    if (!line.trim()) {
      continue;
    }
    const response = await handleLine(line, options);
    process.stdout.write(`${JSON.stringify(response)}\n`);
  }
}

module.exports = {
  buildJsonRpcEndpoint,
  buildAuthorizationHeader,
  sanitizeError,
  forwardJsonRpc,
  handleLine,
  runStdioBridge,
};

if (require.main === module) {
  runStdioBridge().catch((error) => {
    const token = process.env.FLOWDESK_MCP_BRIDGE_TOKEN || '';
    process.stderr.write(`${sanitizeError(error, token)}\n`);
    process.exit(1);
  });
}
