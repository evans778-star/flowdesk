package com.aiwork.helper.ai.ollama;

import com.aiwork.helper.config.FlowdeskAiProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@RequiredArgsConstructor
public class OllamaClient {

    private final RestTemplate restTemplate;
    private final FlowdeskAiProperties properties;
    private final ObjectMapper objectMapper;

    public String chat(List<Message> messages) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", properties.getOllama().getChatModel());
            requestBody.put("stream", false);

            ArrayNode messageArray = objectMapper.createArrayNode();
            for (Message message : messages) {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("role", message.role());
                node.put("content", message.content());
                messageArray.add(node);
            }
            requestBody.set("messages", messageArray);

            JsonNode response = post("/api/chat", requestBody);
            String content = response.path("message").path("content").asText(null);
            if (content == null || content.isBlank()) {
                throw new OllamaUnavailableException("Ollama returned an empty chat response.");
            }
            return content;
        } catch (OllamaUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw unavailable(e);
        }
    }

    public float[] embed(String text) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", properties.getOllama().getEmbeddingModel());
            requestBody.put("prompt", text);

            JsonNode embeddingNode = post("/api/embeddings", requestBody).path("embedding");
            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new OllamaUnavailableException("Ollama returned an empty embedding response.");
            }

            float[] embedding = new float[embeddingNode.size()];
            for (int i = 0; i < embeddingNode.size(); i++) {
                embedding[i] = (float) embeddingNode.get(i).asDouble();
            }
            return embedding;
        } catch (OllamaUnavailableException e) {
            throw e;
        } catch (Exception e) {
            throw unavailable(e);
        }
    }

    private JsonNode post(String path, ObjectNode requestBody) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
        String url = properties.getOllama().getBaseUrl().replaceAll("/+$", "") + path;
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        return objectMapper.readTree(response.getBody());
    }

    private OllamaUnavailableException unavailable(Exception cause) {
        if (cause instanceof RestClientException) {
            return new OllamaUnavailableException(
                    "Ollama is unavailable. Start Ollama and pull the configured model.", cause);
        }
        return new OllamaUnavailableException("Ollama request failed: " + cause.getMessage(), cause);
    }

    public record Message(String role, String content) {
    }
}
