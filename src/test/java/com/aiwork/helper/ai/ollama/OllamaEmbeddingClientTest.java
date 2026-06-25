package com.aiwork.helper.ai.ollama;

import com.aiwork.helper.config.FlowdeskAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaEmbeddingClientTest {

    @Test
    void sendsEmbeddingRequestAndParsesVector() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OllamaClient client = new OllamaClient(restTemplate, properties(), new ObjectMapper());
        OllamaEmbeddingClient embeddingClient = new OllamaEmbeddingClient(client);

        server.expect(once(), requestTo("http://localhost:11434/api/embeddings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("nomic-embed-text"))
                .andExpect(jsonPath("$.prompt").value("hello"))
                .andRespond(withSuccess("""
                        {"embedding":[0.1,0.2,0.3]}
                        """, MediaType.APPLICATION_JSON));

        float[] embedding = embeddingClient.embed("hello");

        assertThat(embedding).containsExactly(0.1f, 0.2f, 0.3f);
        server.verify();
    }

    @Test
    void rejectsEmptyEmbeddingResponse() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OllamaClient client = new OllamaClient(restTemplate, properties(), new ObjectMapper());
        OllamaEmbeddingClient embeddingClient = new OllamaEmbeddingClient(client);

        server.expect(once(), requestTo("http://localhost:11434/api/embeddings"))
                .andRespond(withSuccess("""
                        {"embedding":[]}
                        """, MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> embeddingClient.embed("hello"))
                .isInstanceOf(OllamaUnavailableException.class)
                .hasMessageContaining("empty embedding");
    }

    private FlowdeskAiProperties properties() {
        FlowdeskAiProperties properties = new FlowdeskAiProperties();
        properties.setEnabled(true);
        properties.setProvider(FlowdeskAiProperties.Provider.OLLAMA);
        properties.getOllama().setBaseUrl("http://localhost:11434");
        properties.getOllama().setChatModel("qwen2.5:7b");
        properties.getOllama().setEmbeddingModel("nomic-embed-text");
        properties.getOllama().setTimeout(Duration.ofSeconds(1));
        return properties;
    }
}
