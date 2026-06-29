package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class DemoAssetsDocsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void demoAssetsGuideListsSafeScreenshotTargets() throws IOException {
        String doc = read("docs/demo-assets.md");

        assertThat(doc)
                .contains("Swagger UI")
                .contains("MCP tools/list")
                .contains("RAG citation response")
                .contains("demo-smoke PASS")
                .contains("real token")
                .contains("API key")
                .contains("private URL");
        assertNoRealSecretPattern(doc);
    }

    @Test
    void assetsDirectoryDoesNotUseSecretLikeNames() throws IOException {
        Path assets = ROOT.resolve("docs/assets");
        if (Files.exists(assets)) {
            try (var paths = Files.walk(assets)) {
                String names = paths
                        .map(path -> path.getFileName().toString())
                        .reduce("", (left, right) -> left + "\n" + right);
                assertNoRealSecretPattern(names);
            }
        }
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
