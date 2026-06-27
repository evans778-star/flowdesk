package com.aiwork.helper.mcp.audit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class McpAuditSanitizer {

    private static final int MAX_VALUE_LENGTH = 48;

    private McpAuditSanitizer() {
    }

    public static Map<String, Object> sanitizeArguments(Map<String, Object> arguments) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        if (arguments == null) {
            return sanitized;
        }
        arguments.forEach((key, value) -> sanitized.put(key, sanitizeValue(key, value)));
        return sanitized;
    }

    private static Object sanitizeValue(String key, Object value) {
        if (isSensitiveKey(key)) {
            return "[REDACTED]";
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> nested = new LinkedHashMap<>();
            map.forEach((nestedKey, nestedValue) ->
                    nested.put(String.valueOf(nestedKey), sanitizeValue(String.valueOf(nestedKey), nestedValue)));
            return nested;
        }
        if (value instanceof List<?> list) {
            return list.stream()
                    .map(item -> sanitizeValue(key, item))
                    .toList();
        }
        if (value instanceof String text) {
            return truncate(text);
        }
        return value;
    }

    private static boolean isSensitiveKey(String key) {
        String normalized = key == null ? "" : key.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("secret")
                || normalized.contains("token")
                || normalized.contains("apikey")
                || normalized.contains("api_key")
                || normalized.contains("authorization")
                || normalized.contains("credential");
    }

    private static String truncate(String value) {
        if (value.length() <= MAX_VALUE_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_VALUE_LENGTH - 3) + "...";
    }
}
