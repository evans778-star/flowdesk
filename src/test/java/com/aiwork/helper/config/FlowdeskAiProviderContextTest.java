package com.aiwork.helper.config;

import com.aiwork.helper.ai.embedding.DashScopeEmbeddingClient;
import com.aiwork.helper.ai.embedding.FlowdeskEmbeddingClient;
import com.aiwork.helper.ai.ollama.OllamaAiConfiguration;
import com.aiwork.helper.ai.ollama.OllamaClient;
import com.aiwork.helper.ai.ollama.OllamaEmbeddingClient;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.service.AIService;
import com.aiwork.helper.service.impl.AIServiceImpl;
import com.aiwork.helper.service.impl.DisabledAIService;
import com.aiwork.helper.service.impl.OllamaAIService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

class FlowdeskAiProviderContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withBean(ObjectMapper.class, ObjectMapper::new)
            .withBean(RestTemplateBuilder.class, RestTemplateBuilder::new)
            .withBean(ChatLogRepository.class, () -> Mockito.mock(ChatLogRepository.class))
            .withUserConfiguration(
                    FlowdeskAiProperties.class,
                    DashScopeProperties.class,
                    DisabledAIService.class,
                    AIServiceImpl.class,
                    DashScopeEmbeddingClient.class,
                    OllamaAiConfiguration.class,
                    OllamaAIService.class,
                    OllamaEmbeddingClient.class
            );

    @Test
    void disabledModeUsesDisabledAiServiceOnly() {
        contextRunner
                .withPropertyValues("flowdesk.ai.enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context).hasSingleBean(DisabledAIService.class);
                    assertThat(context).doesNotHaveBean(OllamaClient.class);
                    assertThat(context).doesNotHaveBean(FlowdeskEmbeddingClient.class);
                });
    }

    @Test
    void ollamaModeLoadsOllamaBeansWithoutDashScopeBeans() {
        contextRunner
                .withPropertyValues(
                        "flowdesk.ai.enabled=true",
                        "flowdesk.ai.provider=ollama"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context).hasSingleBean(OllamaAIService.class);
                    assertThat(context).hasSingleBean(OllamaClient.class);
                    assertThat(context).hasSingleBean(OllamaEmbeddingClient.class);
                    assertThat(context).doesNotHaveBean(AIServiceImpl.class);
                    assertThat(context).doesNotHaveBean(DashScopeEmbeddingClient.class);
                });
    }

    @Test
    void dashScopeModeLoadsDashScopeBeansWithoutOllamaBeans() {
        contextRunner
                .withPropertyValues(
                        "flowdesk.ai.enabled=true",
                        "flowdesk.ai.provider=dashscope",
                        "dashscope.api-key=test-dashscope-api-key",
                        "dashscope.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AIService.class);
                    assertThat(context).hasSingleBean(AIServiceImpl.class);
                    assertThat(context).hasSingleBean(DashScopeEmbeddingClient.class);
                    assertThat(context).doesNotHaveBean(OllamaClient.class);
                    assertThat(context).doesNotHaveBean(OllamaAIService.class);
                });
    }
}
