package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ReleaseDocsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void changelogDocumentsBetaReleaseScope() throws IOException {
        String changelog = read("CHANGELOG.md");

        assertThat(changelog)
                .contains("Unreleased")
                .contains("v0.1.0-beta")
                .contains("MCP")
                .contains("RAG citations")
                .contains("Quality Lab")
                .contains("Demo Pack")
                .contains("Request ID");
        assertNoRealSecretPattern(changelog);
    }

    @Test
    void releaseChecklistDocumentsVerificationCommands() throws IOException {
        String checklist = read("docs/release-checklist.md");

        assertThat(checklist)
                .contains("git diff --check")
                .contains("mvnw.cmd test")
                .contains("mvnw.cmd package")
                .contains("node --test")
                .contains("rg -n");
        assertNoRealSecretPattern(checklist);
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
