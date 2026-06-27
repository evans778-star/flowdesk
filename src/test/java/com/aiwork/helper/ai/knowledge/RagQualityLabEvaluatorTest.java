package com.aiwork.helper.ai.knowledge;

import com.aiwork.helper.ai.knowledge.RagQualityLabEvaluator.EvalCase;
import com.aiwork.helper.ai.knowledge.RagQualityLabEvaluator.EvalResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RagQualityLabEvaluatorTest {

    private final RagQualityLabEvaluator evaluator = new RagQualityLabEvaluator(new RagCitationMapper());

    @Test
    void evaluatesRetrievedDocumentsWithoutExternalServices() {
        EvalCase evalCase = new EvalCase(
                "What is the attendance policy?",
                List.of("attendance", "manager"),
                "sample-employee-handbook.pdf",
                "Synthetic handbook smoke case"
        );

        VectorStoreService.StoredDocument document = new VectorStoreService.StoredDocument();
        document.setId("doc:knowledge_java:test-id");
        document.setSource("docs/examples/sample-employee-handbook.pdf");
        document.setChunkIndex(0);
        document.setContent("Attendance policy requires employees to notify their manager.");

        EvalResult result = evaluator.evaluate(evalCase, List.of(document));

        assertThat(result.isTargetDocumentFound()).isTrue();
        assertThat(result.isExpectedKeywordsFound()).isTrue();
        assertThat(result.isCitationPresent()).isTrue();
        assertThat(result.isPassed()).isTrue();
    }

    @Test
    void loadsExampleDatasetShape() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream inputStream = getClass().getResourceAsStream("/rag/eval-cases.json")) {
            List<EvalCase> cases = objectMapper.readValue(inputStream, new TypeReference<>() {
            });

            assertThat(cases).isNotEmpty();
            assertThat(cases.getFirst().query()).isNotBlank();
            assertThat(cases.getFirst().expectedKeywords()).isNotEmpty();
            assertThat(cases.getFirst().expectedSourceDocument()).isNotBlank();
            assertThat(cases.getFirst().notes()).isNotBlank();
        }
    }
}
