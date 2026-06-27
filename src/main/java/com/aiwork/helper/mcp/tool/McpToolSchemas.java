package com.aiwork.helper.mcp.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class McpToolSchemas {

    private McpToolSchemas() {
    }

    static Map<String, Object> objectSchema(List<String> required, Map<String, Map<String, Object>> properties) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "object");
        schema.put("required", required);
        schema.put("properties", properties);
        schema.put("additionalProperties", false);
        return schema;
    }

    static Map<String, Object> stringProperty(String description) {
        return property("string", description);
    }

    static Map<String, Object> integerProperty(String description) {
        return property("integer", description);
    }

    static Map<String, Object> booleanProperty(String description) {
        return property("boolean", description);
    }

    static Map<String, Object> stringArrayProperty(String description) {
        Map<String, Object> property = property("array", description);
        property.put("items", Map.of("type", "string"));
        return property;
    }

    private static Map<String, Object> property(String type, String description) {
        Map<String, Object> property = new LinkedHashMap<>();
        property.put("type", type);
        property.put("description", description);
        return property;
    }
}
