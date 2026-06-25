package com.aiwork.helper.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OllamaAiEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean enabled = context.getEnvironment().getProperty("flowdesk.ai.enabled", Boolean.class, false);
        String provider = context.getEnvironment().getProperty("flowdesk.ai.provider", "ollama");
        return enabled && "ollama".equalsIgnoreCase(provider);
    }
}
