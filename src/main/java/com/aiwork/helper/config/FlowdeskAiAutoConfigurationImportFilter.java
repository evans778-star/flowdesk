package com.aiwork.helper.config;

import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.boot.autoconfigure.AutoConfigurationMetadata;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

public class FlowdeskAiAutoConfigurationImportFilter
        implements AutoConfigurationImportFilter, EnvironmentAware {

    private static final String DASHSCOPE_AUTO_CONFIGURATION =
            "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration";

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public boolean[] match(String[] autoConfigurationClasses, AutoConfigurationMetadata autoConfigurationMetadata) {
        boolean aiEnabled = environment != null
                && environment.getProperty("flowdesk.ai.enabled", Boolean.class, false);
        String provider = environment != null
                ? environment.getProperty("flowdesk.ai.provider", "ollama")
                : "ollama";
        boolean dashScopeEnabled = aiEnabled && "dashscope".equalsIgnoreCase(provider);

        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String candidate = autoConfigurationClasses[i];
            matches[i] = dashScopeEnabled || !DASHSCOPE_AUTO_CONFIGURATION.equals(candidate);
        }
        return matches;
    }
}
