package com.aiwork.helper.ai.knowledge;

import com.aiwork.helper.ai.embedding.FlowdeskEmbeddingClient;
import com.aiwork.helper.config.FlowdeskAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class VectorStoreServiceUnavailableTest {

    private final FlowdeskEmbeddingClient embeddingClient = mock(FlowdeskEmbeddingClient.class);
    private final VectorStoreService vectorStoreService = new VectorStoreService(
            embeddingClient,
            new FlowdeskAiProperties(),
            new ObjectMapper(),
            mock(StringRedisTemplate.class)
    );

    @Test
    void unavailableVectorStoreSkipsWritesAndReturnsEmptyResults() {
        PDFProcessor.DocumentChunk chunk = new PDFProcessor.DocumentChunk();
        chunk.setContent("Synthetic handbook content");
        chunk.setSource("docs/examples/sample-employee-handbook.pdf");
        chunk.setChunkIndex(0);

        assertThatCode(() -> vectorStoreService.addDocuments(List.of(chunk))).doesNotThrowAnyException();
        assertThatCode(vectorStoreService::clear).doesNotThrowAnyException();

        assertThat(vectorStoreService.deleteBySource(chunk.getSource())).isZero();
        assertThat(vectorStoreService.size()).isZero();
        assertThatCode(() -> assertThat(vectorStoreService.searchSimilar("leave policy", 3)).isEmpty())
                .doesNotThrowAnyException();
        verifyNoInteractions(embeddingClient);
    }
}
