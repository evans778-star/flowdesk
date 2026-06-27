package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.ai.knowledge.RagCitationMapper;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.mcp.McpToolCallResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SearchKnowledgeMcpToolTest {

    private final VectorStoreService vectorStoreService = mock(VectorStoreService.class);
    private final ObjectProvider<VectorStoreService> vectorStoreProvider = vectorStoreProvider();
    private final SearchKnowledgeMcpTool tool = new SearchKnowledgeMcpTool(
            vectorStoreProvider,
            new RagCitationMapper()
    );

    @Test
    void returnsRetrievedChunksAndCitations() throws Exception {
        when(vectorStoreProvider.getIfAvailable()).thenReturn(vectorStoreService);
        VectorStoreService.StoredDocument document = new VectorStoreService.StoredDocument();
        document.setId("doc:knowledge_java:test-id");
        document.setSource("docs/examples/sample-employee-handbook.pdf");
        document.setChunkIndex(1);
        document.setScore(0.12d);
        document.setContent("Leave requests should be discussed with the manager.");
        when(vectorStoreService.searchSimilar("leave policy", 2)).thenReturn(List.of(document));

        McpToolCallResponse response = tool.call(Map.of("query", "leave policy", "topK", 2));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsEntry("query", "leave policy");
        assertThat(response.getData()).containsKey("citations");
        assertThat(response.getData().get("chunks")).asList().hasSize(1);
    }

    @SuppressWarnings("unchecked")
    private static ObjectProvider<VectorStoreService> vectorStoreProvider() {
        return mock(ObjectProvider.class);
    }
}
