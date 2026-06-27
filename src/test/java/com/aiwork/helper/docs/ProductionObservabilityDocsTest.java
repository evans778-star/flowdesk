package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionObservabilityDocsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void productionHardeningDocCoversRequiredOperationsTopics() throws IOException {
        String doc = read("docs/production-hardening.md");

        assertThat(doc)
                .contains("JWT_SECRET")
                .contains("CORS")
                .contains("MCP")
                .contains("Actuator")
                .contains("upload")
                .contains("AI provider")
                .contains("Demo Pack");
    }

    @Test
    void observabilityDocCoversRequestIdHealthAndRedaction() throws IOException {
        String doc = read("docs/observability.md");

        assertThat(doc)
                .contains("X-Request-Id")
                .contains("MDC")
                .contains("/actuator/health")
                .contains("secret")
                .contains("token")
                .contains("password");
    }

    @Test
    void readmeLinksProductionAndObservabilityDocs() throws IOException {
        String readme = read("README.md");

        assertThat(readme)
                .contains("docs/production-hardening.md")
                .contains("docs/observability.md");
    }

    @Test
    void productionDocsUseOnlyPlaceholderSecrets() throws IOException {
        String combined = read("README.md")
                + "\n" + read("docs/production-hardening.md")
                + "\n" + read("docs/observability.md");

        assertThat(combined)
                .doesNotContain("sk-live")
                .doesNotContain("jdbc:postgresql://private-host")
                .doesNotContain("super-secret");
        assertThat(Pattern.compile("AKIA[0-9A-Z]{16}").matcher(combined).find()).isFalse();
        assertThat(Pattern.compile("-----BEGIN ([A-Z ]+)?PRIVATE KEY-----").matcher(combined).find()).isFalse();
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }
}
