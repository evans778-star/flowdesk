package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class RoadmapReleaseWorkflowTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void roadmapUsesVersionedReleaseWorkflowSections() throws IOException {
        String roadmap = read("ROADMAP.md");

        assertThat(roadmap)
                .contains("v0.1.0 Beta Release")
                .contains("Demo Pack")
                .contains("MCP Adapter Preview")
                .contains("JSON-RPC Preview")
                .contains("stdio MCP bridge preview")
                .contains("RAG citations")
                .contains("RAG Quality Lab")
                .contains("CI / docs / release checklist")
                .contains("v0.2.0 Production Readiness")
                .contains("RBAC / permission model")
                .contains("permission-aware MCP tools")
                .contains("persistent audit logs")
                .contains("upload storage hardening")
                .contains("rate limiting")
                .contains("backup / deployment guidance")
                .contains("v0.3.0 Agent / MCP Expansion")
                .contains("standard MCP transport evaluation")
                .contains("more office tools")
                .contains("richer client examples")
                .contains("MCP compatibility tests")
                .contains("Future")
                .contains("frontend/admin console")
                .contains("Testcontainers integration tests")
                .contains("multi-tenant support")
                .contains("advanced RAG evaluation")
                .contains("not recommended for direct production use");
    }

    private String read(String relativePath) throws IOException {
        Path path = ROOT.resolve(relativePath);
        assertThat(path).exists();
        return Files.readString(path);
    }
}
