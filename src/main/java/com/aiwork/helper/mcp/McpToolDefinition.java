package com.aiwork.helper.mcp;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class McpToolDefinition {

    private String name;

    private String title;

    private String description;

    private boolean readOnly;

    private boolean write;

    private List<String> requiredPermissions;

    private Map<String, Object> inputSchema;

    private Map<String, Object> outputSchema;

    private Map<String, Object> annotations;

    private String resultType;

    public McpToolDefinition(String name,
                             String title,
                             String description,
                             boolean readOnly,
                             boolean write,
                             List<String> requiredPermissions,
                             Map<String, Object> inputSchema,
                             String resultType) {
        this(
                name,
                title,
                description,
                readOnly,
                write,
                requiredPermissions,
                inputSchema,
                defaultOutputSchema(resultType),
                defaultAnnotations(readOnly, write),
                resultType
        );
    }

    public McpToolDefinition(String name,
                             String title,
                             String description,
                             boolean readOnly,
                             boolean write,
                             List<String> requiredPermissions,
                             Map<String, Object> inputSchema,
                             Map<String, Object> outputSchema,
                             Map<String, Object> annotations,
                             String resultType) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.readOnly = readOnly;
        this.write = write;
        this.requiredPermissions = requiredPermissions != null ? requiredPermissions : List.of();
        this.inputSchema = inputSchema != null ? inputSchema : Map.of("type", "object");
        this.outputSchema = outputSchema != null ? outputSchema : defaultOutputSchema(resultType);
        this.annotations = annotations != null ? annotations : defaultAnnotations(readOnly, write);
        this.resultType = resultType != null ? resultType : "object";
    }

    public McpToolDefinition(String name, String description, Map<String, Object> inputSchema, boolean write) {
        this(
                name,
                name,
                description,
                !write,
                write,
                List.of(),
                inputSchema,
                "object"
        );
    }

    private static Map<String, Object> defaultOutputSchema(String resultType) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("success", Map.of("type", "boolean"));
        properties.put("toolName", Map.of("type", "string"));
        properties.put("data", Map.of("type", "object", "description", resultType != null ? resultType : "object"));
        properties.put("error", Map.of("type", List.of("object", "null")));
        properties.put("metadata", Map.of("type", "object"));
        return Map.of(
                "type", "object",
                "properties", properties
        );
    }

    private static Map<String, Object> defaultAnnotations(boolean readOnly, boolean write) {
        return Map.of(
                "readOnlyHint", readOnly,
                "destructiveHint", write,
                "idempotentHint", !write,
                "openWorldHint", false
        );
    }
}
