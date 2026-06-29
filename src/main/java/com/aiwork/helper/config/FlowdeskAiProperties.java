package com.aiwork.helper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "flowdesk.ai")
public class FlowdeskAiProperties {

    public enum Provider {
        OLLAMA,
        DASHSCOPE
    }

    private boolean enabled = false;
    private Provider provider = Provider.OLLAMA;
    private Integer embeddingDimension;
    private String vectorIndexName;
    private Ollama ollama = new Ollama();

    public int resolveEmbeddingDimension() {
        if (embeddingDimension != null && embeddingDimension > 0) {
            return embeddingDimension;
        }
        return provider == Provider.OLLAMA ? 768 : 1024;
    }

    public String resolveVectorIndexName() {
        if (vectorIndexName != null && !vectorIndexName.isBlank()) {
            return vectorIndexName;
        }
        if (provider == Provider.DASHSCOPE) {
            return "knowledge_java";
        }
        return "knowledge_java_ollama_" + resolveEmbeddingDimension();
    }

    @Data
    public static class Ollama {
        private String baseUrl = "http://localhost:11434";
        private String chatModel = "qwen2.5:7b";
        private String embeddingModel = "nomic-embed-text";
        private Duration timeout = Duration.ofSeconds(30);
    }
}
