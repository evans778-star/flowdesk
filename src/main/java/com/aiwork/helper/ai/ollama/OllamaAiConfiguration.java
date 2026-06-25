package com.aiwork.helper.ai.ollama;

import com.aiwork.helper.config.FlowdeskAiProperties;
import com.aiwork.helper.config.OllamaAiEnabledCondition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@Conditional(OllamaAiEnabledCondition.class)
public class OllamaAiConfiguration {

    @Bean
    public RestTemplate ollamaRestTemplate(FlowdeskAiProperties properties, RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(properties.getOllama().getTimeout())
                .setReadTimeout(properties.getOllama().getTimeout())
                .build();
    }

    @Bean
    public OllamaClient ollamaClient(RestTemplate ollamaRestTemplate,
                                     FlowdeskAiProperties properties,
                                     ObjectMapper objectMapper) {
        return new OllamaClient(ollamaRestTemplate, properties, objectMapper);
    }
}
