package com.aiwork.helper.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MultiPortConfigTest {

    @Test
    void appliesConfiguredProtocolToHttpAndWebSocketConnectors() {
        MultiPortConfig config = new MultiPortConfig();
        ReflectionTestUtils.setField(config, "websocketPort", 9100);
        ReflectionTestUtils.setField(config, "tomcatProtocol", "org.apache.coyote.http11.Http11Nio2Protocol");

        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer = config.servletContainer();

        customizer.customize(factory);

        assertThat(ReflectionTestUtils.getField(factory, "protocol"))
                .isEqualTo("org.apache.coyote.http11.Http11Nio2Protocol");
        assertThat(factory.getAdditionalTomcatConnectors())
                .singleElement()
                .satisfies(connector -> {
                    assertThat(connector.getProtocol()).isEqualTo("org.apache.coyote.http11.Http11Nio2Protocol");
                    assertThat(connector.getPort()).isEqualTo(9100);
                });
    }
}
