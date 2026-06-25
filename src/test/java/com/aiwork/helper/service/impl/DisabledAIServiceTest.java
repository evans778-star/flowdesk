package com.aiwork.helper.service.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DisabledAIServiceTest {

    @Test
    void returnsClearMessageWhenChatIsCalled() {
        DisabledAIService service = new DisabledAIService();

        String response = service.chat("user-1", "hello", "conversation-1");

        assertThat(response).contains("AI is disabled");
    }

    @Test
    void historyOperationsAreNoOps() {
        DisabledAIService service = new DisabledAIService();

        service.addMessageToHistory("conversation-1", "user", "uploaded file");
        service.clearHistory("conversation-1");
    }
}
