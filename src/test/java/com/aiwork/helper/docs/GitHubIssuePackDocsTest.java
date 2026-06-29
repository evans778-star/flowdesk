package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubIssuePackDocsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void issuePackDocumentsLabelsIssuesAndSafetyBoundaries() throws IOException {
        String doc = read("docs/github-issue-pack.md");

        assertThat(doc)
                .contains("type: feature")
                .contains("type: bug")
                .contains("type: docs")
                .contains("type: test")
                .contains("type: chore")
                .contains("area: rag")
                .contains("area: mcp")
                .contains("area: security")
                .contains("area: demo")
                .contains("area: ci")
                .contains("priority: high")
                .contains("priority: medium")
                .contains("priority: low")
                .contains("good first issue")
                .contains("help wanted")
                .contains("release-blocker")
                .contains("Add reviewed screenshots and short demo GIFs")
                .contains("Add RBAC and permission model for production readiness")
                .contains("Track standard MCP transport compatibility")
                .contains("Acceptance criteria")
                .contains("Safety checklist")
                .contains("GitHub page")
                .contains("Do not include real");
        assertNoRealSecretPattern(doc);
    }

    private String read(String relativePath) throws IOException {
        Path path = ROOT.resolve(relativePath);
        assertThat(path).exists();
        return Files.readString(path);
    }

    private void assertNoRealSecretPattern(String text) {
        assertThat(Pattern.compile("AKIA[0-9A-Z]{16}").matcher(text).find()).isFalse();
        assertThat(Pattern.compile("-----BEGIN ([A-Z ]+)?PRIVATE KEY-----").matcher(text).find()).isFalse();
        assertThat(Pattern.compile("sk-[A-Za-z0-9]{20,}").matcher(text).find()).isFalse();
    }
}
