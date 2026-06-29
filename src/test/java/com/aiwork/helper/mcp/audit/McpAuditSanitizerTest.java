package com.aiwork.helper.mcp.audit;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class McpAuditSanitizerTest {

    @Test
    void masksSensitiveArgumentsAndTruncatesLongValues() {
        Map<String, Object> sanitized = McpAuditSanitizer.sanitizeArguments(Map.of(
                "query", "How do I configure citations for a long internal handbook query?",
                "apiKey", "fake-api-key-value-that-should-never-appear",
                "password", "plain-text-password",
                "nested", Map.of("jwtToken", "ey.fake.jwt", "title", "Visible title")
        ));

        assertThat(sanitized.get("query").toString()).endsWith("...");
        assertThat(sanitized.get("apiKey")).isEqualTo("[REDACTED]");
        assertThat(sanitized.get("password")).isEqualTo("[REDACTED]");
        assertThat(sanitized.toString()).doesNotContain("fake-api-key-value");
        assertThat(sanitized.toString()).doesNotContain("plain-text-password");
        assertThat(sanitized.toString()).doesNotContain("ey.fake.jwt");
        assertThat(sanitized.toString()).contains("Visible title");
    }
}
