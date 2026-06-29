package com.aiwork.helper.ci;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class CiWorkflowTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void ciWorkflowCoversJavaMatrixBuildAndScans() throws IOException {
        String workflow = read(".github/workflows/ci.yml");
        String normalized = workflow.toLowerCase(Locale.ROOT);

        assertThat(workflow)
                .contains("21")
                .contains("ubuntu-latest")
                .contains("windows-latest")
                .contains("git diff --check")
                .contains("node --test tools/mcp-bridge/test/bridge.test.js");
        assertThat(normalized)
                .contains("cache: maven")
                .contains("test")
                .contains("package")
                .contains("secret scan")
                .contains("node-version: \"20\"");
        assertNoRealSecretPattern(workflow);
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
