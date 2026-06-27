package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.ai.knowledge.RagCitationMapper;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.dto.response.CitationResponse;
import com.aiwork.helper.mcp.McpToolCallError;
import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.McpToolDefinition;
import org.springframework.beans.factory.ObjectProvider;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SearchKnowledgeMcpTool implements FlowdeskMcpTool {

    private final ObjectProvider<VectorStoreService> vectorStoreServiceProvider;
    private final RagCitationMapper citationMapper;

    public SearchKnowledgeMcpTool(ObjectProvider<VectorStoreService> vectorStoreServiceProvider,
                                  RagCitationMapper citationMapper) {
        this.vectorStoreServiceProvider = vectorStoreServiceProvider;
        this.citationMapper = citationMapper;
    }

    @Override
    public McpToolDefinition definition() {
        Map<String, Map<String, Object>> properties = new LinkedHashMap<>();
        properties.put("query", McpToolSchemas.stringProperty("Natural-language knowledge search query."));
        properties.put("topK", McpToolSchemas.integerProperty("Maximum number of chunks to retrieve. Default 3, maximum 10."));
        return new McpToolDefinition(
                "flowdesk_search_knowledge",
                "Search knowledge",
                "Search Flowdesk RAG knowledge chunks for the authenticated user context.",
                true,
                false,
                List.of("mcp:knowledge:read"),
                McpToolSchemas.objectSchema(List.of("query"), properties),
                "knowledgeSearchResult"
        );
    }

    @Override
    public McpToolCallResponse call(Map<String, Object> arguments) {
        String query = McpArgumentUtils.stringValue(arguments, "query");
        if (query == null || query.isBlank()) {
            return McpToolCallResponse.error(definition().getName(), McpToolCallError.VALIDATION_ERROR, "query is required", false);
        }

        VectorStoreService vectorStoreService = vectorStoreServiceProvider.getIfAvailable();
        if (vectorStoreService == null) {
            return McpToolCallResponse.error(
                    definition().getName(),
                    McpToolCallError.TOOL_EXECUTION_FAILED,
                    "RAG vector store is unavailable. Enable Flowdesk AI/RAG before using this tool.",
                    true
            );
        }

        int topK = Math.max(1, Math.min(10, McpArgumentUtils.intValue(arguments, "topK", 3)));
        try {
            List<VectorStoreService.StoredDocument> documents = vectorStoreService.searchSimilar(query, topK);
            List<CitationResponse> citations = citationMapper.toCitations(documents);
            List<Map<String, Object>> chunks = documents.stream()
                    .map(document -> {
                        Map<String, Object> chunk = new LinkedHashMap<>();
                        chunk.put("documentId", document.getId());
                        chunk.put("source", document.getSource());
                        chunk.put("chunkId", document.getChunkIndex());
                        chunk.put("score", document.getScore());
                        chunk.put("snippet", document.getContent());
                        return chunk;
                    })
                    .toList();
            return McpToolCallResponse.success(definition().getName(), Map.of(
                    "query", query,
                    "topK", topK,
                    "chunks", chunks,
                    "citations", citations
            ));
        } catch (Exception e) {
            return McpToolCallResponse.error(
                    definition().getName(),
                    McpToolCallError.TOOL_EXECUTION_FAILED,
                    "Knowledge search failed.",
                    true
            );
        }
    }
}
