package com.aiwork.helper.ai.ollama;

import com.aiwork.helper.ai.embedding.FlowdeskEmbeddingClient;
import com.aiwork.helper.config.OllamaAiEnabledCondition;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Conditional(OllamaAiEnabledCondition.class)
public class OllamaEmbeddingClient implements FlowdeskEmbeddingClient {

    private final OllamaClient ollamaClient;

    @Override
    public float[] embed(String text) {
        return ollamaClient.embed(text);
    }
}
