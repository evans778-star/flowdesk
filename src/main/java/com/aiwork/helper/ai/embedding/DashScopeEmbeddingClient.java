package com.aiwork.helper.ai.embedding;

import com.aiwork.helper.config.DashScopeProperties;
import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
@RequiredArgsConstructor
@Conditional(DashScopeAiEnabledCondition.class)
public class DashScopeEmbeddingClient implements FlowdeskEmbeddingClient {

    private final DashScopeProperties dashScopeProperties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public float[] embed(String text) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", dashScopeProperties.getEmbedding().getModel());
            requestBody.put("input", text);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(dashScopeProperties.getApiKey());

            HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            String url = dashScopeProperties.getBaseUrl() + "/embeddings";
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode embeddingNode = objectMapper.readTree(response.getBody())
                    .path("data")
                    .path(0)
                    .path("embedding");

            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new IllegalStateException("DashScope returned an empty embedding");
            }

            ArrayNode arrayNode = (ArrayNode) embeddingNode;
            float[] embedding = new float[arrayNode.size()];
            for (int i = 0; i < arrayNode.size(); i++) {
                embedding[i] = (float) arrayNode.get(i).asDouble();
            }
            return embedding;
        } catch (Exception e) {
            log.error("DashScope embedding request failed", e);
            throw new IllegalStateException("DashScope embedding is unavailable. Check DASHSCOPE_API_KEY and network access.", e);
        }
    }
}
