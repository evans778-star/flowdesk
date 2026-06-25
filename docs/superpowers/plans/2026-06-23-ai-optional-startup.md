# AI Optional Startup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let Flowdesk start local/dev baseline features when DashScope AI is disabled or no real API key is available, while returning clear disabled responses from AI/RAG endpoints.

**Architecture:** Introduce a `flowdesk.ai.enabled` feature flag. When disabled, filter DashScope auto-configuration before Spring creates WebClient/DashScope beans, conditionally skip AI/RAG beans, and provide lightweight fallback services for public startup, Swagger, login, upload, and non-AI modules. When enabled, keep the existing AI/Agent/RAG behavior.

**Tech Stack:** Java 21, Spring Boot 3.2, Spring conditional beans, `AutoConfigurationImportFilter`, JUnit 5, MockMvc.

---

## File Map

- Modify `src/main/resources/application.yml`: add `flowdesk.ai.enabled` default.
- Modify `src/main/resources/application-dev.yml`: default AI disabled for local public quick start.
- Modify `src/main/resources/application-prod.yml`: require explicit `FLOWDESK_AI_ENABLED`.
- Create `src/main/java/com/aiwork/helper/config/FlowdeskAiProperties.java`: bind `flowdesk.ai.enabled`.
- Create `src/main/java/com/aiwork/helper/config/FlowdeskAiAutoConfigurationImportFilter.java`: remove DashScope auto-config when AI is disabled.
- Create or update `src/main/resources/META-INF/spring.factories`: register the auto-configuration import filter.
- Modify `src/main/java/com/aiwork/helper/config/AIConfig.java`: only log DashScope config when AI is enabled.
- Modify `src/main/java/com/aiwork/helper/ai/config/ChatClientConfig.java`: only create `ChatClient` when AI is enabled.
- Modify `src/main/java/com/aiwork/helper/ai/config/ChatMemoryConfig.java`: only create AI memory/advisor beans when AI is enabled.
- Modify `src/main/java/com/aiwork/helper/ai/agent/AgentService.java`: only create Agent when AI is enabled.
- Modify `src/main/java/com/aiwork/helper/ai/knowledge/VectorStoreService.java`: only create vector store when AI is enabled.
- Modify `src/main/java/com/aiwork/helper/ai/tools/KnowledgeTools.java`: only create knowledge tool when AI is enabled.
- Modify `src/main/java/com/aiwork/helper/controller/KnowledgeDiagController.java`: only expose diag controller when AI is enabled.
- Modify `src/main/java/com/aiwork/helper/service/impl/AIServiceImpl.java`: only create real legacy AI service when AI is enabled.
- Create `src/main/java/com/aiwork/helper/service/impl/DisabledAIService.java`: fallback `AIService` for upload compatibility and clear disabled messages.
- Modify `src/main/java/com/aiwork/helper/service/impl/ChatServiceImpl.java`: inject `ObjectProvider<AgentService>` and return a clear disabled message when Agent is unavailable.
- Update `README.md`, `docs/configuration.md`, `docs/demo.md`: document `FLOWDESK_AI_ENABLED=false` local baseline and `true` AI/RAG mode.
- Create `src/test/java/com/aiwork/helper/config/FlowdeskAiAutoConfigurationImportFilterTest.java`.
- Create `src/test/java/com/aiwork/helper/FlowdeskAiDisabledStartupTest.java`.
- Create `src/test/java/com/aiwork/helper/service/impl/DisabledAIServiceTest.java`.
- Create or update `src/test/java/com/aiwork/helper/controller/ChatControllerTest.java`.

---

## Task 1: Add AI Feature Flag Configuration

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/resources/application-dev.yml`
- Modify: `src/main/resources/application-prod.yml`
- Create: `src/main/java/com/aiwork/helper/config/FlowdeskAiProperties.java`

- [ ] **Step 1: Add public-safe common flag**

In `application.yml`, under existing `flowdesk:` add:

```yaml
flowdesk:
  ai:
    enabled: ${FLOWDESK_AI_ENABLED:false}
  user:
    default-password: ${FLOWDESK_USER_DEFAULT_PASSWORD:}
```

Expected: default local behavior disables AI unless explicitly enabled.

- [ ] **Step 2: Make dev baseline explicit**

In `application-dev.yml`, under `flowdesk:` add:

```yaml
flowdesk:
  ai:
    enabled: ${FLOWDESK_AI_ENABLED:false}
  user:
    default-password: ${FLOWDESK_USER_DEFAULT_PASSWORD:}
```

Expected: dev startup with placeholder `DASHSCOPE_API_KEY` does not create DashScope/WebClient beans.

- [ ] **Step 3: Require production intent**

In `application-prod.yml`, under `flowdesk:` add:

```yaml
flowdesk:
  ai:
    enabled: ${FLOWDESK_AI_ENABLED}
  user:
    default-password: ${FLOWDESK_USER_DEFAULT_PASSWORD}
```

Expected: production deployment must explicitly choose AI on/off.

- [ ] **Step 4: Add typed properties**

Create `FlowdeskAiProperties.java`:

```java
package com.aiwork.helper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "flowdesk.ai")
public class FlowdeskAiProperties {
    private boolean enabled = false;
}
```

Expected: code can inject the flag without stringly typed lookups.

---

## Task 2: Prevent DashScope Auto-Configuration When AI Is Disabled

**Files:**
- Create: `src/main/java/com/aiwork/helper/config/FlowdeskAiAutoConfigurationImportFilter.java`
- Create or update: `src/main/resources/META-INF/spring.factories`
- Test: `src/test/java/com/aiwork/helper/config/FlowdeskAiAutoConfigurationImportFilterTest.java`

- [ ] **Step 1: Write failing filter test**

Create `FlowdeskAiAutoConfigurationImportFilterTest.java`:

```java
package com.aiwork.helper.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurationImportFilter;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class FlowdeskAiAutoConfigurationImportFilterTest {

    @Test
    void excludesDashScopeAutoConfigurationWhenAiIsDisabled() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "flowdesk.ai.enabled", "false"
        )));

        FlowdeskAiAutoConfigurationImportFilter filter = new FlowdeskAiAutoConfigurationImportFilter();
        filter.setEnvironment(environment);

        String[] candidates = {
                "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration",
                "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration"
        };

        boolean[] matches = filter.match(candidates, AutoConfigurationImportFilter.class);

        assertThat(matches).containsExactly(false, true);
    }

    @Test
    void keepsDashScopeAutoConfigurationWhenAiIsEnabled() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("test", Map.of(
                "flowdesk.ai.enabled", "true"
        )));

        FlowdeskAiAutoConfigurationImportFilter filter = new FlowdeskAiAutoConfigurationImportFilter();
        filter.setEnvironment(environment);

        String[] candidates = {
                "com.alibaba.cloud.ai.autoconfigure.dashscope.DashScopeAutoConfiguration"
        };

        boolean[] matches = filter.match(candidates, AutoConfigurationImportFilter.class);

        assertThat(matches).containsExactly(true);
    }
}
```

- [ ] **Step 2: Run test and verify RED**

Run:

```powershell
.\mvnw.cmd "-Dtest=FlowdeskAiAutoConfigurationImportFilterTest" test
```

Expected: compilation fails because `FlowdeskAiAutoConfigurationImportFilter` does not exist.

- [ ] **Step 3: Add import filter**

Create `FlowdeskAiAutoConfigurationImportFilter.java`:

```java
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

        boolean[] matches = new boolean[autoConfigurationClasses.length];
        for (int i = 0; i < autoConfigurationClasses.length; i++) {
            String candidate = autoConfigurationClasses[i];
            matches[i] = aiEnabled || !DASHSCOPE_AUTO_CONFIGURATION.equals(candidate);
        }
        return matches;
    }
}
```

- [ ] **Step 4: Register filter**

Create or update `src/main/resources/META-INF/spring.factories`:

```properties
org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
com.aiwork.helper.config.FlowdeskAiAutoConfigurationImportFilter
```

- [ ] **Step 5: Verify GREEN**

Run:

```powershell
.\mvnw.cmd "-Dtest=FlowdeskAiAutoConfigurationImportFilterTest" test
```

Expected: 2 tests pass.

---

## Task 3: Guard AI/RAG Beans Behind the Flag

**Files:**
- Modify: `src/main/java/com/aiwork/helper/config/AIConfig.java`
- Modify: `src/main/java/com/aiwork/helper/ai/config/ChatClientConfig.java`
- Modify: `src/main/java/com/aiwork/helper/ai/config/ChatMemoryConfig.java`
- Modify: `src/main/java/com/aiwork/helper/ai/agent/AgentService.java`
- Modify: `src/main/java/com/aiwork/helper/ai/knowledge/VectorStoreService.java`
- Modify: `src/main/java/com/aiwork/helper/ai/tools/KnowledgeTools.java`
- Modify: `src/main/java/com/aiwork/helper/controller/KnowledgeDiagController.java`
- Modify: `src/main/java/com/aiwork/helper/service/impl/AIServiceImpl.java`

- [ ] **Step 1: Add conditional annotation to real AI classes**

Add this import to each listed Java class:

```java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
```

Add this annotation next to the existing stereotype/configuration annotation:

```java
@ConditionalOnProperty(name = "flowdesk.ai.enabled", havingValue = "true")
```

Examples:

```java
@Slf4j
@Configuration
@ConditionalOnProperty(name = "flowdesk.ai.enabled", havingValue = "true")
public class ChatClientConfig {
}
```

```java
@Slf4j
@Service
@ConditionalOnProperty(name = "flowdesk.ai.enabled", havingValue = "true")
public class AgentService {
}
```

Expected: with `flowdesk.ai.enabled=false`, these beans are absent and cannot pull in DashScope/WebClient startup work.

- [ ] **Step 2: Verify compile**

Run:

```powershell
.\mvnw.cmd "-DskipTests" package
```

Expected: compilation succeeds or reveals dependent beans that still require AI-only beans.

---

## Task 4: Add Disabled AI Fallbacks

**Files:**
- Create: `src/main/java/com/aiwork/helper/service/impl/DisabledAIService.java`
- Modify: `src/main/java/com/aiwork/helper/service/impl/ChatServiceImpl.java`
- Test: `src/test/java/com/aiwork/helper/service/impl/DisabledAIServiceTest.java`

- [ ] **Step 1: Write failing fallback service test**

Create `DisabledAIServiceTest.java`:

```java
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
```

- [ ] **Step 2: Run test and verify RED**

Run:

```powershell
.\mvnw.cmd "-Dtest=DisabledAIServiceTest" test
```

Expected: compilation fails because `DisabledAIService` does not exist.

- [ ] **Step 3: Implement fallback service**

Create `DisabledAIService.java`:

```java
package com.aiwork.helper.service.impl;

import com.aiwork.helper.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "flowdesk.ai.enabled", havingValue = "false", matchIfMissing = true)
public class DisabledAIService implements AIService {

    static final String DISABLED_MESSAGE =
            "AI is disabled. Set FLOWDESK_AI_ENABLED=true and configure DASHSCOPE_API_KEY to enable AI features.";

    @Override
    public String chat(String userId, String message, String conversationId) {
        log.info("AI chat skipped because AI is disabled: userId={}, conversationId={}", userId, conversationId);
        return DISABLED_MESSAGE;
    }

    @Override
    public void clearHistory(String conversationId) {
        log.debug("AI history clear skipped because AI is disabled: conversationId={}", conversationId);
    }

    @Override
    public void addMessageToHistory(String conversationId, String role, String content) {
        log.debug("AI history append skipped because AI is disabled: conversationId={}, role={}", conversationId, role);
    }
}
```

- [ ] **Step 4: Make ChatService tolerate missing AgentService**

Change `ChatServiceImpl` constructor field from:

```java
private final AgentService agentService;
```

to:

```java
private final org.springframework.beans.factory.ObjectProvider<AgentService> agentServiceProvider;
```

Change `handleAIChat` to obtain the agent:

```java
AgentService agentService = agentServiceProvider.getIfAvailable();
if (agentService == null) {
    return "AI is disabled. Set FLOWDESK_AI_ENABLED=true and configure DASHSCOPE_API_KEY to enable AI chat.";
}
String aiResponse = agentService.chat(userId, content, relationId, startTime, endTime);
```

Expected: chat endpoint can respond clearly without creating DashScope/WebClient beans.

- [ ] **Step 5: Verify fallback tests**

Run:

```powershell
.\mvnw.cmd "-Dtest=DisabledAIServiceTest" test
```

Expected: tests pass.

---

## Task 5: Add Disabled Startup and Controller Coverage

**Files:**
- Create: `src/test/java/com/aiwork/helper/FlowdeskAiDisabledStartupTest.java`
- Create or update: `src/test/java/com/aiwork/helper/controller/ChatControllerTest.java`

- [ ] **Step 1: Write disabled startup test**

Create `FlowdeskAiDisabledStartupTest.java`:

```java
package com.aiwork.helper;

import com.aiwork.helper.ai.agent.AgentService;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.testsupport.CiFriendlySpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@CiFriendlySpringBootTest
@TestPropertySource(properties = {
        "flowdesk.ai.enabled=false",
        "spring.ai.dashscope.api-key=",
        "dashscope.api-key="
})
class FlowdeskAiDisabledStartupTest {

    @Autowired
    private ApplicationContext context;

    @Test
    void contextStartsWithoutAiBeans() {
        assertThat(context.getBeansOfType(ChatClient.class)).isEmpty();
        assertThat(context.getBeansOfType(AgentService.class)).isEmpty();
        assertThat(context.getBeansOfType(VectorStoreService.class)).isEmpty();
    }
}
```

- [ ] **Step 2: Run test and verify RED before implementation is complete**

Run:

```powershell
.\mvnw.cmd "-Dtest=FlowdeskAiDisabledStartupTest" test
```

Expected before full implementation: failure due existing AI bean creation or unwanted bean presence.

- [ ] **Step 3: Write chat disabled controller test**

If no existing `ChatControllerTest` exists, create it:

```java
package com.aiwork.helper.controller;

import com.aiwork.helper.dto.request.ChatRequest;
import com.aiwork.helper.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChatService chatService;

    @Test
    void returnsClearDisabledMessageFromChatService() throws Exception {
        when(chatService.handleAIChat(any(), eq("hello"), any(), any(), any()))
                .thenReturn("AI is disabled. Set FLOWDESK_AI_ENABLED=true and configure DASHSCOPE_API_KEY to enable AI chat.");

        ChatRequest request = new ChatRequest();
        request.setPrompts("hello");
        request.setChatType(0);

        mockMvc.perform(post("/v1/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.data").value(org.hamcrest.Matchers.containsString("AI is disabled")));
    }
}
```

- [ ] **Step 4: Verify disabled startup and controller tests**

Run:

```powershell
.\mvnw.cmd "-Dtest=FlowdeskAiDisabledStartupTest,ChatControllerTest" test
```

Expected: tests pass.

---

## Task 6: Documentation Updates

**Files:**
- Modify: `README.md`
- Modify: `docs/configuration.md`
- Modify: `docs/demo.md`

- [ ] **Step 1: Document baseline startup**

Add to README quick start:

```powershell
$env:FLOWDESK_AI_ENABLED="false"
$env:DASHSCOPE_API_KEY="test-dashscope-api-key"
```

Text:

```text
For local baseline startup, keep FLOWDESK_AI_ENABLED=false. Health, Swagger, login, upload, users, todos, approvals, and WebSocket can start without a real DashScope key. Set FLOWDESK_AI_ENABLED=true and provide a real DASHSCOPE_API_KEY to enable Agent and RAG features.
```

- [ ] **Step 2: Document variable**

Add to `docs/configuration.md`:

```markdown
| `FLOWDESK_AI_ENABLED` | Enables DashScope Agent/RAG beans. Use `false` for local baseline startup, `true` for AI features. | `false` in dev |
```

- [ ] **Step 3: Update demo caveat**

Add to `docs/demo.md`:

```markdown
The baseline demo uses `FLOWDESK_AI_ENABLED=false` so a new contributor can verify startup, Swagger, login, and upload without a real DashScope key. RAG and Agent requests require `FLOWDESK_AI_ENABLED=true` plus a real `DASHSCOPE_API_KEY`.
```

---

## Strict Acceptance Test Plan

### A. Static Safety

Run:

```powershell
rg -n "sk-|api-key|secret|password|token|AKIA|BEGIN PRIVATE KEY" .
rg --hidden --no-ignore -g '!target/**' -g '!.git/**' -g '!logs/**' -n "AKIA[0-9A-Z]{16}|-----BEGIN (RSA |DSA |EC |OPENSSH |)PRIVATE KEY-----|sk-[A-Za-z0-9]{20,}" .
git diff --check
```

Accept:
- Broad scan has only placeholders, code identifiers, docs examples, or test values.
- High-confidence scan has no matches.
- `git diff --check` exits 0.

### B. Unit and Context Tests

Run:

```powershell
.\mvnw.cmd "-Dtest=FlowdeskAiAutoConfigurationImportFilterTest,DisabledAIServiceTest,FlowdeskAiDisabledStartupTest,ChatControllerTest" test
.\mvnw.cmd test
.\mvnw.cmd package
```

Accept:
- All targeted tests pass.
- Full test suite passes.
- Package succeeds.
- Test logs do not show `NacosConfigManager`, `DashScopeAutoConfiguration` bean creation, MongoDB connection attempts, or Redis connection attempts in CI-friendly context.

### C. Docker Dependency Check

Run:

```powershell
docker compose up -d
docker compose ps
```

Accept:
- `flowdesk-mongodb` is healthy.
- `flowdesk-redis-stack` is healthy.

### D. Local Baseline Jar Startup With AI Disabled

Run:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
$env:FLOWDESK_AI_ENABLED="false"
$env:DASHSCOPE_API_KEY="test-dashscope-api-key"
$env:JWT_SECRET="test-jwt-secret-value-with-at-least-32-bytes"
$env:FLOWDESK_ADMIN_USER="flowdesk-local-owner"
$env:FLOWDESK_ADMIN_PASSWORD="local-only-bootstrap-password"
$env:FLOWDESK_USER_DEFAULT_PASSWORD="local-only-user-password"
$env:MONGODB_DATABASE="FlowdeskReleaseVerify"
$env:UPLOAD_SAVE_PATH="target/local-startup/upload/"
$env:LOG_FILE="target/local-startup/flowdesk.log"
java -jar target/flowdesk-1.0.0-SNAPSHOT.jar
```

Accept:
- Application reaches `Started FlowdeskApplication`.
- Logs do not contain `Unable to establish loopback connection`.
- Logs do not contain `DashScopeAutoConfiguration` failure.
- Logs do not contain `NacosConfigManager` warning.
- Java process is stopped after checks.

### E. Local HTTP Smoke With AI Disabled

Run while the jar is alive:

```powershell
Invoke-RestMethod http://localhost:8888/actuator/health
Invoke-WebRequest http://localhost:8888/swagger-ui/index.html
```

Login:

```powershell
$loginBody = @{ name="flowdesk-local-owner"; password="local-only-bootstrap-password" } | ConvertTo-Json
$login = Invoke-RestMethod -Uri http://localhost:8888/v1/user/login -Method Post -ContentType "application/json" -Body $loginBody
$token = $login.data.token
```

Upload:

```powershell
curl.exe -s -X POST "http://localhost:8888/v1/upload/file" `
  -H "Authorization: Bearer $token" `
  -F "file=@docs/examples/sample-employee-handbook.pdf;type=application/pdf"
```

AI disabled behavior:

```powershell
$chatBody = @{ prompts="hello"; chatType=0 } | ConvertTo-Json
Invoke-RestMethod -Uri http://localhost:8888/v1/chat -Method Post `
  -Headers @{ Authorization="Bearer $token" } `
  -ContentType "application/json" `
  -Body $chatBody
```

Accept:
- Health endpoint returns UP or expected actuator health payload.
- Swagger returns HTTP 200.
- Login returns a non-empty JWT token.
- Upload returns success with a generated file path.
- Chat returns a clear message containing `AI is disabled`.
- No real DashScope call is attempted.

### F. Optional AI Enabled Verification

Only run when a real DashScope key is available:

```powershell
$env:FLOWDESK_AI_ENABLED="true"
$env:DASHSCOPE_API_KEY="<real-key-from-secret-manager>"
java -jar target/flowdesk-1.0.0-SNAPSHOT.jar
```

Accept:
- Application starts in an environment with a healthy Java network stack.
- `/v1/chat` can call the Agent path.
- RAG calls requiring embeddings work only with a real key.
- Do not paste or commit the real key.

---

## Rollback Rules

- If baseline startup with `FLOWDESK_AI_ENABLED=false` still creates DashScope/WebClient beans, first fix `FlowdeskAiAutoConfigurationImportFilter`.
- If non-AI APIs fail because a fallback bean is missing, add a disabled fallback instead of making AI mandatory again.
- If AI enabled behavior regresses, remove only the conditional/fallback change causing the regression and keep the feature flag architecture.
- Do not revert unrelated repository cleanup, docs, tests, or user edits.
