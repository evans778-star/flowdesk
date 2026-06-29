package com.aiwork.helper.testsupport;

import org.springframework.boot.test.context.SpringBootTest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude="
                + "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration,"
                + "com.alibaba.cloud.nacos.NacosConfigAutoConfiguration,"
                + "com.alibaba.cloud.nacos.endpoint.NacosConfigEndpointAutoConfiguration",
        "jwt.secret=test-jwt-secret-value-with-at-least-32-bytes",
        "spring.security.user.name=test-admin",
        "spring.security.user.password=test-admin-password",
        "spring.ai.dashscope.api-key=test-dashscope-api-key",
        "spring.ai.dashscope.chat.api-key=test-dashscope-api-key",
        "dashscope.api-key=test-dashscope-api-key",
        "spring.cloud.nacos.config.enabled=false",
        "spring.cloud.nacos.discovery.enabled=false",
        "logging.file.name=target/test-logs/flowdesk-test.log"
})
public @interface CiFriendlySpringBootTest {
}
