package com.aiwork.helper.controller;

import com.aiwork.helper.ai.tools.FileTools;
import com.aiwork.helper.common.Result;
import com.aiwork.helper.dto.response.FileResponse;
import com.aiwork.helper.entity.ChatLog;
import com.aiwork.helper.exception.BusinessException;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.AIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * Validated file upload endpoints for chat and knowledge workflows.
 */
@Slf4j
@RestController
@RequestMapping("/v1/upload")
@RequiredArgsConstructor
@Tag(name = "File Upload", description = "Validated file upload APIs used by chat and knowledge workflows")
public class UploadController {

    @Value("${upload.save-path:upload/}")
    private String savePath;

    @Value("${upload.host:http://127.0.0.1:8888}")
    private String host;

    @Value("${upload.max-file-size-bytes:52428800}")
    private long maxFileSizeBytes;

    @Value("${upload.allowed-extensions:.pdf,.txt,.md,.csv,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.png,.jpg,.jpeg,.gif,.webp}")
    private String[] allowedExtensions;

    @Value("${upload.allowed-content-types:application/pdf,text/plain,text/markdown,text/csv,application/msword,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-powerpoint,application/vnd.openxmlformats-officedocument.presentationml.presentation,image/png,image/jpeg,image/gif,image/webp}")
    private String[] allowedContentTypes;

    private final ChatLogRepository chatLogRepository;
    private final AIService aiService;

    @PostMapping("/file")
    @Operation(summary = "Upload one file", description = "Uploads a validated file without accepting arbitrary server-side paths.")
    public Result<FileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "chat", required = false) String chat) {

        validateFile(file);

        try {
            Path uploadDir = Paths.get(savePath).toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String originalFilename = file.getOriginalFilename();
            String extension = getExtension(originalFilename);

            String filename = UUID.randomUUID().toString().replace("-", "") + extension;

            Path targetPath = uploadDir.resolve(filename).normalize();
            if (!targetPath.startsWith(uploadDir)) {
                throw new BusinessException("Invalid file path");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("file upload success: {}", filename);

            FileResponse response = FileResponse.builder()
                    .host(host)
                    .file(buildFilePath(filename))
                    .filename(filename)
                    .build();

            if (StringUtils.hasText(chat)) {
                log.info("file upload with chat context: {}", chat);
                saveFileToMemory(chat, response, originalFilename);
            }

            return Result.ok(response);

        } catch (IOException e) {
            log.error("file upload failed", e);
            throw new BusinessException("File upload failed: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Uploaded file must not be empty");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException("File size exceeds the configured limit");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("Filename is required");
        }

        String cleanFilename = StringUtils.cleanPath(originalFilename);
        if (cleanFilename.contains("..") || cleanFilename.contains("/") || cleanFilename.contains("\\")) {
            throw new BusinessException("Invalid filename");
        }

        String extension = getExtension(cleanFilename);
        if (!isAllowed(extension, allowedExtensions)) {
            throw new BusinessException("Unsupported file extension");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)
                || !isAllowed(contentType.toLowerCase(Locale.ROOT), allowedContentTypes)) {
            throw new BusinessException("Unsupported MIME type");
        }
    }

    private String getExtension(String filename) {
        String cleanFilename = StringUtils.cleanPath(filename);
        int extensionIndex = cleanFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == cleanFilename.length() - 1) {
            throw new BusinessException("File extension is required");
        }
        return cleanFilename.substring(extensionIndex).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowed(String value, String[] allowedValues) {
        return Arrays.stream(allowedValues)
                .map(item -> item.trim().toLowerCase(Locale.ROOT))
                .anyMatch(item -> item.equals(value));
    }

    private String buildFilePath(String filename) {
        String normalizedSavePath = savePath.replace('\\', '/');
        if (!normalizedSavePath.endsWith("/")) {
            normalizedSavePath += "/";
        }
        return normalizedSavePath + filename;
    }

    @PostMapping("/multiplefiles")
    @Operation(summary = "Upload multiple files", description = "Multiple file upload is not currently supported.")
    public Result<Void> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        throw new BusinessException("Multiple file upload is not currently supported");
    }

    private void saveFileToMemory(String conversationId, FileResponse fileResp, String originalFilename) {
        try {
            String userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                log.warn("Cannot save uploaded file to chat memory because the current user is unavailable");
                return;
            }

            String message = String.format("File \"%s\" uploaded successfully. File path: %s",
                    originalFilename, fileResp.getFile());

            FileTools.saveUploadedFile(userId, fileResp.getFile(), originalFilename);

            ChatLog chatLog = new ChatLog();
            chatLog.setConversationId(conversationId != null ? conversationId : "knowledge");
            chatLog.setSendId(userId);
            chatLog.setRecvId("");
            chatLog.setChatType(1);
            chatLog.setMsgContent(message);
            chatLog.setSendTime(System.currentTimeMillis() / 1000);
            chatLogRepository.save(chatLog);

            aiService.addMessageToHistory(userId, "user", message);

            log.info("Uploaded file saved to chat memory: userId={}, file={}", userId, fileResp.getFile());

        } catch (Exception e) {
            log.error("Failed to save uploaded file to chat memory", e);
        }
    }
}
