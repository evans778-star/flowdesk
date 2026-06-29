package com.aiwork.helper.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ComposeDemoConfigTest {

    @Test
    void demoComposeDefinesDependenciesAndBackendWithSafeDefaults() throws IOException {
        Path composePath = Path.of("docker-compose.demo.yml");

        assertThat(composePath).exists().isRegularFile();

        String compose = Files.readString(composePath);
        assertThat(compose)
                .contains("mongodb:")
                .contains("redis-stack:")
                .contains("flowdesk:")
                .contains("FLOWDESK_AI_ENABLED=false")
                .contains("FLOWDESK_MCP_ENABLED=true")
                .contains("FLOWDESK_MCP_WRITE_TOOLS_ENABLED=false")
                .contains("JWT_SECRET=")
                .contains("FLOWDESK_ADMIN_USER=flowdesk-local-owner")
                .contains("FLOWDESK_ADMIN_PASSWORD=local-only-bootstrap-password");
        assertThat(compose)
                .doesNotContainPattern("sk-[A-Za-z0-9]{20,}")
                .doesNotContain("AKIA")
                .doesNotContain("BEGIN PRIVATE KEY");
    }
}
