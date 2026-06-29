package com.aiwork.helper.docs;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class ReleaseChecklistGitHubPageStepsTest {

    private static final Path ROOT = Path.of("").toAbsolutePath();

    @Test
    void releaseChecklistIncludesGitHubPagePublicationSteps() throws IOException {
        String checklist = read("docs/release-checklist.md");

        assertThat(checklist)
                .contains("labels")
                .contains("issues")
                .contains("GitHub Release")
                .contains("v0.1.0")
                .contains("release notes")
                .contains("CI")
                .contains("secret scan")
                .contains("docs/release-notes-v0.1.0.md")
                .contains("README")
                .contains("Release page")
                .contains("Issues page");
    }

    private String read(String relativePath) throws IOException {
        Path path = ROOT.resolve(relativePath);
        assertThat(path).exists();
        return Files.readString(path);
    }
}
