package com.aiwork.helper.mcp.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class McpArgumentUtils {

    private McpArgumentUtils() {
    }

    static String stringValue(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    static Integer intValue(Map<String, Object> arguments, String key, Integer defaultValue) {
        Object value = arguments.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return Integer.parseInt(String.valueOf(value));
    }

    static Long longValue(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.parseLong(String.valueOf(value));
    }

    @SuppressWarnings("unchecked")
    static List<String> stringListValue(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            List<String> result = new ArrayList<>();
            for (Object item : list) {
                result.add(String.valueOf(item));
            }
            return result;
        }
        return List.of(String.valueOf(value));
    }
}
