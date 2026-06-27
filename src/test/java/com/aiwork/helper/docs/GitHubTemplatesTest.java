package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class GitHubTemplatesTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void issueAndPullRequestTemplatesCoverMaintenanceSignals() throws IOException {
        String bugReport = read(".github/ISSUE_TEMPLATE/bug_report.md");
        String featureRequest = read(".github/ISSUE_TEMPLATE/feature_request.md");
        String pullRequest = read(".github/pull_request_template.md");

        assertThat(bugReport)
                .contains("Request ID")
                .contains("AI/MCP/RAG enabled")
                .contains("Logs with secrets removed");
        assertThat(featureRequest)
                .contains("Problem")
                .contains("Proposed solution")
                .contains("Alternatives")
                .contains("Security impact")
                .contains("Compatibility impact");
        assertThat(pullRequest)
                .contains("Tests run")
                .contains("Docs updated")
                .contains("Security checklist")
                .contains("No real secrets")
                .contains("Backward compatibility");

        assertNoRealSecretPattern(bugReport + featureRequest + pullRequest);
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
