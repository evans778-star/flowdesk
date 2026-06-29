package com.aiwork.helper.observability;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class RequestIdFilterTest {

    @Test
    void generatesRequestIdHeaderAndClearsMdc() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdDuringChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                requestIdDuringChain.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).isNotBlank();
        assertThat(requestIdDuringChain.get()).isEqualTo(response.getHeader(RequestIdFilter.HEADER_NAME));
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }

    @Test
    void propagatesIncomingRequestIdAndClearsMdc() throws Exception {
        RequestIdFilter filter = new RequestIdFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/v1/chat");
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader(RequestIdFilter.HEADER_NAME, "client-request-123");
        AtomicReference<String> requestIdDuringChain = new AtomicReference<>();
        FilterChain chain = (servletRequest, servletResponse) ->
                requestIdDuringChain.set(MDC.get(RequestIdFilter.MDC_KEY));

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(RequestIdFilter.HEADER_NAME)).isEqualTo("client-request-123");
        assertThat(requestIdDuringChain.get()).isEqualTo("client-request-123");
        assertThat(MDC.get(RequestIdFilter.MDC_KEY)).isNull();
    }
}
