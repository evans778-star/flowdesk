package com.aiwork.helper.ai.knowledge;

import com.aiwork.helper.dto.response.CitationResponse;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;

@Component
public class RagCitationMapper {

    private static final int SNIPPET_LIMIT = 360;

    public List<CitationResponse> toCitations(List<VectorStoreService.StoredDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return List.of();
        }

        return documents.stream()
                .map(this::toCitation)
                .toList();
    }

    CitationResponse toCitation(VectorStoreService.StoredDocument document) {
        return CitationResponse.builder()
                .documentId(document.getId())
                .documentName(resolveDocumentName(document.getSource()))
                .chunkId(document.getChunkIndex())
                .pageNumber(null)
                .score(document.getScore())
                .snippet(toSnippet(document.getContent()))
                .build();
    }

    private String resolveDocumentName(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            Path fileName = Path.of(source).getFileName();
            return fileName != null ? fileName.toString() : source;
        } catch (Exception e) {
            int slash = Math.max(source.lastIndexOf('/'), source.lastIndexOf('\\'));
            return slash >= 0 ? source.substring(slash + 1) : source;
        }
    }

    private String toSnippet(String content) {
        if (content == null) {
            return null;
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= SNIPPET_LIMIT) {
            return normalized;
        }
        return normalized.substring(0, SNIPPET_LIMIT).trim();
    }
}
