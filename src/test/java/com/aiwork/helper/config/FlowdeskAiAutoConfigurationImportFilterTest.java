package com.aiwork.helper.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FlowdeskAiAutoConfigurationImportFilterTest {

    @Test
    void excludesDashScopeAutoConfigurationWhenAiIsDisabled() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "flowdesk.ai.enabled", "false"
        )));

        FlowdeskAiAutoConfigurationImportFilter filter = new FlowdeskAiAutoConfigurationImportFilter();
        filter.setEnvironment(environment);

        String[] candidates = {
                "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration",
                "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
        };

        boolean[] matches = filter.match(candidates, null);

        assertThat(matches).containsExactly(false, true);
    }

    @Test
    void excludesDashScopeAutoConfigurationWhenOllamaProviderIsEnabled() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "flowdesk.ai.enabled", "true",
                "flowdesk.ai.provider", "ollama"
        )));

        FlowdeskAiAutoConfigurationImportFilter filter = new FlowdeskAiAutoConfigurationImportFilter();
        filter.setEnvironment(environment);

        String[] candidates = {
                "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration",
                "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
        };

        boolean[] matches = filter.match(candidates, null);

        assertThat(matches).containsExactly(false, true);
    }

    @Test
    void keepsDashScopeAutoConfigurationWhenDashScopeProviderIsEnabled() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "flowdesk.ai.enabled", "true",
                "flowdesk.ai.provider", "dashscope"
        )));

        FlowdeskAiAutoConfigurationImportFilter filter = new FlowdeskAiAutoConfigurationImportFilter();
        filter.setEnvironment(environment);

        String[] candidates = {
                "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration"
        };

        boolean[] matches = filter.match(candidates, null);

        assertThat(matches).containsExactly(true);
    }
}
