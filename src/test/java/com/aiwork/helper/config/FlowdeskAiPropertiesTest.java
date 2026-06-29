package com.aiwork.helper.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FlowdeskAiPropertiesTest {

    @Test
    void defaultsToDisabledOllamaProvider() {
        FlowdeskAiProperties properties = bind(Map.of());

        assertThat(properties.isEnabled()).isFalse();
        assertThat(properties.getProvider()).isEqualTo(FlowdeskAiProperties.Provider.OLLAMA);
        assertThat(properties.getOllama().getBaseUrl()).isEqualTo("http://localhost:11434");
        assertThat(properties.getOllama().getChatModel()).isEqualTo("qwen2.5:7b");
        assertThat(properties.getOllama().getEmbeddingModel()).isEqualTo("nomic-embed-text");
        assertThat(properties.resolveEmbeddingDimension()).isEqualTo(768);
    }

    @Test
    void bindsOllamaProviderSettings() {
        FlowdeskAiProperties properties = bind(Map.of(
                "flowdesk.ai.enabled", "true",
                "flowdesk.ai.provider", "ollama",
                "flowdesk.ai.ollama.base-url", "http://localhost:11434",
                "flowdesk.ai.ollama.chat-model", "qwen2.5:7b",
                "flowdesk.ai.ollama.embedding-model", "nomic-embed-text",
                "flowdesk.ai.ollama.timeout", "45s",
                "flowdesk.ai.embedding-dimension", "768"
        ));

        assertThat(properties.isEnabled()).isTrue();
        assertThat(properties.getProvider()).isEqualTo(FlowdeskAiProperties.Provider.OLLAMA);
        assertThat(properties.getOllama().getBaseUrl()).isEqualTo("http://localhost:11434");
        assertThat(properties.getOllama().getChatModel()).isEqualTo("qwen2.5:7b");
        assertThat(properties.getOllama().getEmbeddingModel()).isEqualTo("nomic-embed-text");
        assertThat(properties.getOllama().getTimeout()).hasSeconds(45);
        assertThat(properties.resolveEmbeddingDimension()).isEqualTo(768);
    }

    @Test
    void dashScopeProviderDefaultsToExistingEmbeddingDimension() {
        FlowdeskAiProperties properties = bind(Map.of(
                "flowdesk.ai.enabled", "true",
                "flowdesk.ai.provider", "dashscope"
        ));

        assertThat(properties.getProvider()).isEqualTo(FlowdeskAiProperties.Provider.DASHSCOPE);
        assertThat(properties.resolveEmbeddingDimension()).isEqualTo(1024);
    }

    private FlowdeskAiProperties bind(Map<String, Object> properties) {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", properties));
        return Binder.get(environment)
                .bind("flowdesk.ai", FlowdeskAiProperties.class)
                .orElseGet(FlowdeskAiProperties::new);
    }
}
