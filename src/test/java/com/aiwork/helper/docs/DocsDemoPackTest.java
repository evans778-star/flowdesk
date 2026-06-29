package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DocsDemoPackTest {

    @Test
    void readmeAdvertisesDemoPackAndNoKeyCapabilities() throws IOException {
        String readme = Files.readString(Path.of("README.md"));

        assertThat(readme)
                .contains("Run local demo in 10 minutes")
                .contains(".\\mvnw.cmd package")
                .contains("docker compose -f docker-compose.demo.yml up -d --build")
                .contains(".\\scripts\\demo-smoke.ps1")
                .contains("Works without API keys")
                .contains("Health/login/upload/MCP metadata")
                .contains("requires Ollama or DashScope")
                .contains("requires Redis Stack + embedding provider");
    }

    @Test
    void mcpClientDocsIncludeBridgeConfiguration() throws IOException {
        String docs = Files.readString(Path.of("docs", "mcp-client-examples.md"));

        assertThat(docs)
                .contains("stdio bridge preview")
                .contains("tools/mcp-bridge/flowdesk-mcp-bridge.js")
                .contains("FLOWDESK_MCP_BRIDGE_BASE_URL")
                .contains("FLOWDESK_MCP_BRIDGE_TOKEN")
                .contains("Claude Desktop")
                .contains("Cursor")
                .contains("Codex");
        assertThat(docs)
                .doesNotContainPattern("sk-[A-Za-z0-9]{20,}")
                .doesNotContain("BEGIN PRIVATE KEY");
    }
}
