package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class DemoWalkthroughDocsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void demoWalkthroughCoversTenMinuteFlow() throws IOException {
        String doc = read("docs/demo-walkthrough.md");

        assertThat(doc)
                .contains("docker compose -f docker-compose.demo.yml up -d --build")
                .satisfies(content -> assertThat(content.contains("scripts\\demo-smoke.ps1")
                        || content.contains("scripts/demo-smoke.ps1")).isTrue())
                .contains("/v1/mcp/jsonrpc")
                .contains("/v1/knowledge/chat-with-citations")
                .contains("<jwt-token>");
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
