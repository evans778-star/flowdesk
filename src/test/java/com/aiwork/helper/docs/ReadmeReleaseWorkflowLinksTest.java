package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReadmeReleaseWorkflowLinksTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void readmeLinksReleaseWorkflowDocsAndKeepsDemoMcpRagEntries() throws IOException {
        String readme = read("README.md");

        assertThat(readme)
                .contains("docs/github-issue-pack.md")
                .contains("docs/release-notes-v0.1.0.md")
                .contains("ROADMAP.md")
                .contains("docker compose -f docker-compose.demo.yml up -d --build")
                .contains("scripts\\demo-smoke.ps1")
                .contains("MCP")
                .contains("RAG citation")
                .contains("Quality Lab");
    }

    private String read(String relativePath) throws IOException {
        Path path = ROOT.resolve(relativePath);
        assertThat(path).exists();
        return Files.readString(path);
    }
}
