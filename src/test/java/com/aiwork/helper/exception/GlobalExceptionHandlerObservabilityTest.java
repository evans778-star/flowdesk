package com.aiwork.helper.exception;

import com.aiwork.helper.observability.RequestIdFilter;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerObservabilityTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new TestErrorController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .addFilters(new RequestIdFilter())
            .build();

    @Test
    void businessExceptionKeepsCompatibleResultShapeAndRequestIdHeader() throws Exception {
        mockMvc.perform(get("/test/business-error")
                        .header(RequestIdFilter.HEADER_NAME, "biz-request-1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string(RequestIdFilter.HEADER_NAME, "biz-request-1"))
                .andExpect(jsonPath("$.code").value(409))
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.msg").value("business rule failed"));
    }

    @Test
    void unknownExceptionDoesNotLeakStackTraceOrSensitiveDetails() throws Exception {
        String body = mockMvc.perform(get("/test/unexpected-error")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists(RequestIdFilter.HEADER_NAME))
                .andExpect(jsonPath("$.code").value(500))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(body)
                .doesNotContain("jdbc:postgresql://private-host")
                .doesNotContain("password=super-secret")
                .doesNotContain("java.lang.IllegalStateException")
                .doesNotContain("stacktrace-marker");
    }

    @RestController
    static class TestErrorController {

        @GetMapping("/test/business-error")
        void businessError() {
            throw new BusinessException(409, "business rule failed");
        }

        @GetMapping("/test/unexpected-error")
        void unexpectedError() {
            throw new IllegalStateException(
                    "jdbc:postgresql://private-host failed password=super-secret stacktrace-marker"
            );
        }
    }
}
