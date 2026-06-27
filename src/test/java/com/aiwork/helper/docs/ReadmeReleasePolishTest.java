package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ReadmeReleasePolishTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void readmeFrontMatterExplainsDemoBetaAndReleaseDocs() throws IOException {
        String readme = read("README.md");

        assertThat(readme)
                .contains("Try It In 10 Minutes")
                .contains("docker compose -f docker-compose.demo.yml up -d --build")
                .contains("scripts\\demo-smoke.ps1")
                .contains("beta")
                .contains("demo")
                .contains("template")
                .contains("production")
                .contains("docs/demo-walkthrough.md")
                .contains("docs/release-checklist.md")
                .contains("docs/demo-assets.md")
                .contains("CHANGELOG.md");
        assertNoRealSecretPattern(readme);
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
