package com.aiwork.helper.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MultiPortConfig {

    @Value("${websocket.server.port:9000}")
    private int websocketPort;

    @Value("${flowdesk.tomcat.protocol:org.apache.coyote.http11.Http11NioProtocol}")
    private String tomcatProtocol;

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> servletContainer() {
        return factory -> {
            factory.setProtocol(tomcatProtocol);
            factory.addAdditionalTomcatConnectors(createWebSocketConnector());
            log.info("Configured additional WebSocket connector on port {} with protocol {}", websocketPort, tomcatProtocol);
        };
    }

    private Connector createWebSocketConnector() {
        Connector connector = new Connector(tomcatProtocol);
        connector.setScheme("http");
        connector.setPort(websocketPort);
        return connector;
    }
}
