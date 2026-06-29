package com.aiwork.helper.ai.ollama;

import com.aiwork.helper.config.FlowdeskAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OllamaClientTest {

    @Test
    void sendsNonStreamingChatRequestAndParsesAssistantContent() {
        RestTemplate restTemplate = new RestTemplate(requestFactory());
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        OllamaClient client = new OllamaClient(restTemplate, properties(), new ObjectMapper());

        server.expect(once(), requestTo("http://localhost:11434/api/chat"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.model").value("qwen2.5:7b"))
                .andExpect(jsonPath("$.stream").value(false))
                .andExpect(jsonPath("$.messages[0].role").value("user"))
                .andExpect(jsonPath("$.messages[0].content").value("hello"))
                .andRespond(withSuccess("""
                        {"message":{"role":"assistant","content":"hi from ollama"},"done":true}
                        """, MediaType.APPLICATION_JSON));

        String response = client.chat(List.of(new OllamaClient.Message("user", "hello")));

        assertThat(response).isEqualTo("hi from ollama");
        server.verify();
    }

    @Test
    void wrapsConnectionFailuresWithClearMessage() {
        RestTemplate restTemplate = new RestTemplate(requestFactory());
        OllamaClient client = new OllamaClient(restTemplate, properties(), new ObjectMapper());

        assertThatThrownBy(() -> client.chat(List.of(new OllamaClient.Message("user", "hello"))))
                .isInstanceOf(OllamaUnavailableException.class)
                .hasMessageContaining("Ollama is unavailable");
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

    private ClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(100));
        requestFactory.setReadTimeout(Duration.ofMillis(100));
        return requestFactory;
    }
}
