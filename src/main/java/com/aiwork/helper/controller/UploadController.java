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
import org.springframework.web.bind.annotation.*;
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
 * 文件上传控制器
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

    /**
     * 单文件上传
     *
     * @param file 上传的文件
     * @param chat 可选参数，如果指定则将文件信息写入聊天记忆
     */
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
                throw new BusinessException("非法文件路径");
            }

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            log.info("file upload success: {}", filename);

            FileResponse response = FileResponse.builder()
                    .host(host)
                    .file(buildFilePath(filename))
                    .filename(filename)
                    .build();

            // 如果指定了chat参数，将文件信息写入聊天记忆（内存+数据库）
            if (chat != null && !chat.isEmpty()) {
                log.info("file upload with chat context: {}", chat);
                saveFileToMemory(chat, response, originalFilename);
            }

            return Result.ok(response);

        } catch (IOException e) {
            log.error("file upload failed", e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw new BusinessException("文件大小超过限制");
        }

        String originalFilename = file.getOriginalFilename();
        if (!StringUtils.hasText(originalFilename)) {
            throw new BusinessException("文件名不能为空");
        }

        String cleanFilename = StringUtils.cleanPath(originalFilename);
        if (cleanFilename.contains("..") || cleanFilename.contains("/") || cleanFilename.contains("\\")) {
            throw new BusinessException("非法文件名");
        }

        String extension = getExtension(cleanFilename);
        if (!isAllowed(extension, allowedExtensions)) {
            throw new BusinessException("不支持的文件类型");
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType)
                || !isAllowed(contentType.toLowerCase(Locale.ROOT), allowedContentTypes)) {
            throw new BusinessException("不支持的MIME类型");
        }
    }

    private String getExtension(String filename) {
        String cleanFilename = StringUtils.cleanPath(filename);
        int extensionIndex = cleanFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == cleanFilename.length() - 1) {
            throw new BusinessException("文件缺少扩展名");
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

    /**
     * 多文件上传
     *
     * @param files 上传的文件数组
     */
    @PostMapping("/multiplefiles")
    @Operation(summary = "Upload multiple files", description = "Placeholder endpoint; currently returns a business error.")
    public Result<Void> uploadMultipleFiles(@RequestParam("files") MultipartFile[] files) {
        // TODO: 实现多文件上传功能
        throw new BusinessException("多文件上传功能暂未实现");
    }

    /**
     * 将文件信息保存到聊天记忆中（内存缓存 + 数据库）
     */
    private void saveFileToMemory(String conversationId, FileResponse fileResp, String originalFilename) {
        try {
            // 获取当前用户ID
            String userId = SecurityUtils.getCurrentUserId();
            if (userId == null) {
                log.warn("无法获取当前用户ID，跳过保存文件到聊天记忆");
                return;
            }

            // 构建文件上传成功的消息
            String message = String.format("文件 \"%s\" 已上传成功，文件路径: %s",
                    originalFilename, fileResp.getFile());

            // 1. 保存到FileTools的内存缓存（关键！供Agent的Tool使用）
            FileTools.saveUploadedFile(userId, fileResp.getFile(), originalFilename);

            // 2. 保存到数据库（持久化）
            ChatLog chatLog = new ChatLog();
            chatLog.setConversationId(conversationId != null ? conversationId : "knowledge");
            chatLog.setSendId(userId);
            chatLog.setRecvId("");
            chatLog.setChatType(1);
            chatLog.setMsgContent(message);
            chatLog.setSendTime(System.currentTimeMillis() / 1000);
            chatLogRepository.save(chatLog);

            // 3. 同时保存到旧的AIService内存（兼容旧逻辑）
            aiService.addMessageToHistory(userId, "user", message);

            log.info("文件信息已保存到聊天记忆: userId={}, file={}", userId, fileResp.getFile());

        } catch (Exception e) {
            log.error("保存文件到聊天记忆失败", e);
            // 不抛出异常，避免影响文件上传功能
        }
    }
}
