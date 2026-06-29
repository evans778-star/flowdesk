package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class RepositoryHygieneTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    private static final List<String> SOURCE_ATTRIBUTION_MARKERS = List.of(
            "IT杨秀才",
            "golangstar.cn",
            "对应Go版本",
            "公众号",
            "鍏紬",
            "瀵瑰簲Go",
            "Go鐗堟湰"
    );

    @Test
    void javaSourcesDoNotContainIncorrectAttributionOrGoMigrationMarkers() throws IOException {
        List<String> violations = findTextViolations(ROOT.resolve("src/main/java"), SOURCE_ATTRIBUTION_MARKERS);

        assertThat(violations).isEmpty();
    }

    @Test
    void readmeDoesNotContainIncorrectAttributionMarkers() throws IOException {
        String readme = Files.readString(ROOT.resolve("README.md"), StandardCharsets.UTF_8);

        assertThat(findViolations("README.md", readme, SOURCE_ATTRIBUTION_MARKERS)).isEmpty();
    }

    @Test
    void publicDocsDoNotExposeInternalAgentProcessPlans() throws IOException {
        assertThat(ROOT.resolve("docs/superpowers")).doesNotExist();

        List<String> processMarkers = List.of("agentic workers", "REQUIRED SUB-SKILL", "superpowers:subagent-driven-development");
        List<String> violations = findTextViolations(ROOT.resolve("docs"), processMarkers);

        assertThat(violations).isEmpty();
    }

    private List<String> findTextViolations(Path root, List<String> markers) throws IOException {
        if (!Files.exists(root)) {
            return List.of();
        }

        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isTextFile)
                    .filter(path -> !path.toString().contains("target"))
                    .flatMap(path -> {
                        try {
                            String text = Files.readString(path, StandardCharsets.UTF_8);
                            String relative = ROOT.relativize(path).toString();
                            return findViolations(relative, text, markers).stream();
                        } catch (IOException e) {
                            throw new IllegalStateException(e);
                        }
                    })
                    .toList();
        }
    }

    private boolean isTextFile(Path path) {
        String filename = path.getFileName().toString().toLowerCase();
        return filename.endsWith(".java")
                || filename.endsWith(".md")
                || filename.endsWith(".yml")
                || filename.endsWith(".yaml")
                || filename.endsWith(".http")
                || filename.endsWith(".json")
                || filename.endsWith(".txt");
    }

    private List<String> findViolations(String path, String text, List<String> markers) {
        return markers.stream()
                .filter(text::contains)
                .map(marker -> path + " contains " + marker)
                .toList();
    }
}
