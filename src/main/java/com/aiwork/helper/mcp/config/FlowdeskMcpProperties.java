package com.aiwork.helper.mcp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "flowdesk.mcp")
public class FlowdeskMcpProperties {

    private boolean enabled = false;

    private boolean writeToolsEnabled = false;
}
