package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ReleaseNotesV010DocsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void releaseNotesDraftDocumentsBetaScopeAndLimitations() throws IOException {
        String notes = read("docs/release-notes-v0.1.0.md");

        assertThat(notes)
                .contains("Flowdesk v0.1.0 Beta")
                .contains("Highlights")
                .contains("What works without API keys")
                .contains("AI/RAG requirements")
                .contains("MCP status")
                .contains("Demo Pack")
                .contains("Security notes")
                .contains("Verification")
                .contains("Known limitations")
                .contains("Recommended next issues")
                .contains("beta")
                .contains("not recommended for direct production use")
                .contains("Ollama or DashScope")
                .contains("Redis Stack + embedding provider")
                .contains("HTTP/JSON-RPC/stdio bridge preview")
                .contains("write tools")
                .contains("No real secrets");
        assertNoRealSecretPattern(notes);
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
