package com.aiwork.helper.ai.knowledge;

import com.aiwork.helper.dto.response.CitationResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class RagQualityLabEvaluator {

    private final RagCitationMapper citationMapper;

    public RagQualityLabEvaluator(RagCitationMapper citationMapper) {
        this.citationMapper = citationMapper;
    }

    public EvalResult evaluate(EvalCase evalCase, List<VectorStoreService.StoredDocument> retrievedDocuments) {
        List<VectorStoreService.StoredDocument> documents = retrievedDocuments != null
                ? retrievedDocuments
                : List.of();
        List<CitationResponse> citations = citationMapper.toCitations(documents);

        boolean targetDocumentFound = containsTargetDocument(documents, evalCase.expectedSourceDocument());
        boolean expectedKeywordsFound = containsExpectedKeywords(documents, evalCase.expectedKeywords());
        boolean citationPresent = !citations.isEmpty();

        return new EvalResult(
                evalCase.query(),
                targetDocumentFound,
                expectedKeywordsFound,
                citationPresent,
                targetDocumentFound && expectedKeywordsFound && citationPresent
        );
    }

    private boolean containsTargetDocument(List<VectorStoreService.StoredDocument> documents, String expectedSourceDocument) {
        if (expectedSourceDocument == null || expectedSourceDocument.isBlank()) {
            return false;
        }
        String expected = expectedSourceDocument.toLowerCase(Locale.ROOT);
        return documents.stream()
                .map(VectorStoreService.StoredDocument::getSource)
                .filter(source -> source != null)
                .map(source -> source.toLowerCase(Locale.ROOT))
                .anyMatch(source -> source.contains(expected));
    }

    private boolean containsExpectedKeywords(List<VectorStoreService.StoredDocument> documents, List<String> expectedKeywords) {
        if (expectedKeywords == null || expectedKeywords.isEmpty()) {
            return false;
        }
        String joinedContent = documents.stream()
                .map(VectorStoreService.StoredDocument::getContent)
                .filter(content -> content != null)
                .reduce("", (left, right) -> left + " " + right)
                .toLowerCase(Locale.ROOT);

        return expectedKeywords.stream()
                .filter(keyword -> keyword != null && !keyword.isBlank())
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .allMatch(joinedContent::contains);
    }

    public record EvalCase(
            String query,
            List<String> expectedKeywords,
            String expectedSourceDocument,
            String notes
    ) {
    }

    public static class EvalResult {
        private final String query;
        private final boolean targetDocumentFound;
        private final boolean expectedKeywordsFound;
        private final boolean citationPresent;
        private final boolean passed;

        public EvalResult(String query,
                          boolean targetDocumentFound,
                          boolean expectedKeywordsFound,
                          boolean citationPresent,
                          boolean passed) {
            this.query = query;
            this.targetDocumentFound = targetDocumentFound;
            this.expectedKeywordsFound = expectedKeywordsFound;
            this.citationPresent = citationPresent;
            this.passed = passed;
        }

        public String getQuery() {
            return query;
        }

        public boolean isTargetDocumentFound() {
            return targetDocumentFound;
        }

        public boolean isExpectedKeywordsFound() {
            return expectedKeywordsFound;
        }

        public boolean isCitationPresent() {
            return citationPresent;
        }

        public boolean isPassed() {
            return passed;
        }
    }
}
