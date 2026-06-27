package com.aiwork.helper.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class DemoPackFilesTest {

    @Test
    void demoSmokeScriptExistsAndCoversRequiredChecks() throws IOException {
        Path scriptPath = Path.of("scripts", "demo-smoke.ps1");

        assertThat(scriptPath).exists().isRegularFile();

        String script = Files.readString(scriptPath);
        assertThat(script)
                .contains("/actuator/health")
                .contains("/v1/user/login")
                .contains("initialize")
                .contains("ping")
                .contains("tools/list")
                .contains("flowdesk_search_knowledge")
                .contains("flowdesk_upload_document_metadata")
                .contains("flowdesk_create_todo")
                .contains("WRITE_TOOLS_DISABLED")
                .contains("/v1/knowledge/chat-with-citations")
                .contains("citations")
                .contains("exit 1");
        assertThat(script)
                .doesNotContainPattern("sk-[A-Za-z0-9]{20,}")
                .doesNotContain("BEGIN PRIVATE KEY");
    }

    @Test
    void demoPackDocumentationExistsAndDescribesSafetyBoundaries() throws IOException {
        Path docPath = Path.of("docs", "demo-pack.md");

        assertThat(docPath).exists().isRegularFile();

        String doc = Files.readString(docPath);
        assertThat(doc)
                .contains("docker compose -f docker-compose.demo.yml up -d --build")
                .contains(".\\scripts\\demo-smoke.ps1")
                .contains("FLOWDESK_AI_ENABLED=false")
                .contains("FLOWDESK_MCP_ENABLED=true")
                .contains("FLOWDESK_MCP_WRITE_TOOLS_ENABLED=false")
                .contains("local demo")
                .contains("not a production deployment")
                .contains("Do not expose");
        assertThat(doc)
                .doesNotContainPattern("sk-[A-Za-z0-9]{20,}")
                .doesNotContain("BEGIN PRIVATE KEY");
    }
}
