const assert = require('node:assert/strict');
const http = require('node:http');
const test = require('node:test');

const bridge = require('../flowdesk-mcp-bridge');

test('buildJsonRpcEndpoint appends the Flowdesk JSON-RPC path', () => {
  assert.equal(
    bridge.buildJsonRpcEndpoint('http://localhost:8888'),
    'http://localhost:8888/v1/mcp/jsonrpc'
  );
  assert.equal(
    bridge.buildJsonRpcEndpoint('http://localhost:8888/v1/mcp/jsonrpc'),
    'http://localhost:8888/v1/mcp/jsonrpc'
  );
});

test('buildAuthorizationHeader uses a bearer token without logging it', () => {
  assert.equal(
    bridge.buildAuthorizationHeader('demo-local-token'),
    'Bearer demo-local-token'
  );
});

test('sanitizeError removes the full token from error messages', () => {
  const message = bridge.sanitizeError(
    new Error('request failed with demo-local-token in header'),
    'demo-local-token'
  );

  assert.match(message, /request failed/);
  assert.doesNotMatch(message, /demo-local-token/);
});

test('forwardJsonRpc forwards initialize requests to Flowdesk', async () => {
  await withMockFlowdesk(async ({ baseUrl, requests }) => {
    const response = await bridge.forwardJsonRpc(
      { jsonrpc: '2.0', id: 'init-1', method: 'initialize', params: {} },
      { baseUrl, token: 'demo-local-token' }
    );

    assert.equal(requests.length, 1);
    assert.equal(requests[0].url, '/v1/mcp/jsonrpc');
    assert.equal(requests[0].headers.authorization, 'Bearer demo-local-token');
    assert.equal(requests[0].body.method, 'initialize');
    assert.equal(response.result.serverInfo.name, 'flowdesk');
  });
});

test('forwardJsonRpc forwards tools/call requests to Flowdesk', async () => {
  await withMockFlowdesk(async ({ baseUrl, requests }) => {
    const response = await bridge.forwardJsonRpc(
      {
        jsonrpc: '2.0',
        id: 'call-1',
        method: 'tools/call',
        params: {
          name: 'flowdesk_upload_document_metadata',
          arguments: { fileName: 'sample.pdf' },
        },
      },
      { baseUrl, token: 'demo-local-token' }
    );

    assert.equal(requests.length, 1);
    assert.equal(requests[0].body.method, 'tools/call');
    assert.equal(response.result.structuredContent.toolName, 'flowdesk_upload_document_metadata');
  });
});

async function withMockFlowdesk(callback) {
  const requests = [];
  const server = http.createServer((request, response) => {
    let rawBody = '';
    request.setEncoding('utf8');
    request.on('data', (chunk) => {
      rawBody += chunk;
    });
    request.on('end', () => {
      const body = JSON.parse(rawBody);
      requests.push({ url: request.url, headers: request.headers, body });

      if (body.method === 'initialize') {
        writeJson(response, {
          jsonrpc: '2.0',
          id: body.id,
          result: {
            serverInfo: { name: 'flowdesk', version: 'test' },
            capabilities: { tools: { listChanged: false } },
          },
        });
        return;
      }

      writeJson(response, {
        jsonrpc: '2.0',
        id: body.id,
        result: {
          isError: false,
          structuredContent: {
            success: true,
            toolName: body.params.name,
            data: {},
          },
        },
      });
    });
  });

  await new Promise((resolve) => server.listen(0, '127.0.0.1', resolve));
  const { port } = server.address();
  try {
    await callback({ baseUrl: `http://127.0.0.1:${port}`, requests });
  } finally {
    await new Promise((resolve) => server.close(resolve));
  }
}

function writeJson(response, body) {
  response.writeHead(200, { 'Content-Type': 'application/json' });
  response.end(JSON.stringify(body));
}
