package com.aiwork.helper.ai.knowledge;

import com.aiwork.helper.dto.response.CitationResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagCitationMapperTest {

    private final RagCitationMapper mapper = new RagCitationMapper();

    @Test
    void mapsStoredDocumentMetadataToCitation() {
        VectorStoreService.StoredDocument document = new VectorStoreService.StoredDocument();
        document.setId("doc:knowledge_java:test-id");
        document.setSource("docs/examples/sample-employee-handbook.pdf");
        document.setChunkIndex(2);
        document.setScore(0.27d);
        document.setContent(" Attendance policy says employees should notify managers before leave. ");

        List<CitationResponse> citations = mapper.toCitations(List.of(document));

        assertThat(citations).hasSize(1);
        CitationResponse citation = citations.getFirst();
        assertThat(citation.getDocumentId()).isEqualTo("doc:knowledge_java:test-id");
        assertThat(citation.getDocumentName()).isEqualTo("sample-employee-handbook.pdf");
        assertThat(citation.getChunkId()).isEqualTo(2);
        assertThat(citation.getPageNumber()).isNull();
        assertThat(citation.getScore()).isEqualTo(0.27d);
        assertThat(citation.getSnippet()).isEqualTo("Attendance policy says employees should notify managers before leave.");
    }
}
