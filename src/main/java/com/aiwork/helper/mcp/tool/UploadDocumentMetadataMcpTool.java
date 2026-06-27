package com.aiwork.helper.mcp.tool;

import com.aiwork.helper.mcp.McpToolCallError;
import com.aiwork.helper.mcp.McpToolCallResponse;
import com.aiwork.helper.mcp.McpToolDefinition;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UploadDocumentMetadataMcpTool implements FlowdeskMcpTool {

    @Override
    public McpToolDefinition definition() {
        Map<String, Map<String, Object>> properties = new LinkedHashMap<>();
        properties.put("fileName", McpToolSchemas.stringProperty("Original upload file name."));
        properties.put("contentType", McpToolSchemas.stringProperty("Client-reported MIME type."));
        properties.put("sizeBytes", McpToolSchemas.integerProperty("Client-reported file size in bytes."));
        return new McpToolDefinition(
                "flowdesk_upload_document_metadata",
                "Upload document metadata",
                "Return safe upload guidance for a document. This tool never reads server-local file paths.",
                true,
                false,
                List.of("mcp:document:metadata"),
                McpToolSchemas.objectSchema(List.of(), properties),
                "documentUploadMetadataResult"
        );
    }

    @Override
    public McpToolCallResponse call(Map<String, Object> arguments) {
        if (arguments.containsKey("filePath") || arguments.containsKey("path")) {
            return McpToolCallResponse.error(
                    definition().getName(),
                    McpToolCallError.VALIDATION_ERROR,
                    "Server-local file paths are not accepted. Upload files through /v1/upload/file.",
                    false
            );
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("fileName", McpArgumentUtils.stringValue(arguments, "fileName"));
        data.put("contentType", McpArgumentUtils.stringValue(arguments, "contentType"));
        data.put("sizeBytes", McpArgumentUtils.longValue(arguments, "sizeBytes"));
        data.put("message", "Upload binary content through POST /v1/upload/file, then ask Flowdesk to index the uploaded document.");
        return McpToolCallResponse.success(definition().getName(), data);
    }
}
