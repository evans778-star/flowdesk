package com.aiwork.helper.ai.tools;

import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文件记忆工具
 * 提供获取用户上传文件信息的功能
 */
@Slf4j
@Component
@Conditional(DashScopeAiEnabledCondition.class)
public class FileTools {

    /**
     * 用户上传的文件记忆（userId -> 文件列表）
     * 使用ConcurrentHashMap确保线程安全
     */
    private static final Map<String, List<UploadedFile>> userFileMemory = new ConcurrentHashMap<>();

    /**
     * 文件记忆保留时间（毫秒），默认1小时
     */
    private static final long MEMORY_RETENTION_MS = 60 * 60 * 1000;

    /**
     * 上传的文件信息
     */
    @Data
    public static class UploadedFile {
        private String filePath;        // 文件路径
        private String originalName;    // 原始文件名
        private LocalDateTime uploadTime; // 上传时间

        public UploadedFile(String filePath, String originalName) {
            this.filePath = filePath;
            this.originalName = originalName;
            this.uploadTime = LocalDateTime.now();
        }
    }

    /**
     * 保存用户上传的文件信息到记忆中
     *
     * @param userId 用户ID
     * @param filePath 文件路径
     * @param originalName 原始文件名
     */
    public static void saveUploadedFile(String userId, String filePath, String originalName) {
        if (userId == null || filePath == null) {
            return;
        }

        List<UploadedFile> files = userFileMemory.computeIfAbsent(userId, k -> new ArrayList<>());

        // 清理过期的文件记忆
        long now = System.currentTimeMillis();
        files.removeIf(f -> {
            long uploadTimeMs = f.getUploadTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            return (now - uploadTimeMs) > MEMORY_RETENTION_MS;
        });

        // 添加新的文件记忆
        files.add(new UploadedFile(filePath, originalName));

        log.info("文件已保存到记忆: userId={}, filePath={}, originalName={}", userId, filePath, originalName);
    }

    /**
     * 获取用户最近上传的文件列表
     *
     * @param userId 用户ID
     * @return 文件列表
     */
    public static List<UploadedFile> getUploadedFiles(String userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        List<UploadedFile> files = userFileMemory.get(userId);
        if (files == null) {
            return new ArrayList<>();
        }

        // 清理过期的文件记忆
        long now = System.currentTimeMillis();
        files.removeIf(f -> {
            long uploadTimeMs = f.getUploadTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
            return (now - uploadTimeMs) > MEMORY_RETENTION_MS;
        });

        return new ArrayList<>(files);
    }

    /**
     * 清除用户的文件记忆
     *
     * @param userId 用户ID
     */
    public static void clearUserFileMemory(String userId) {
        if (userId != null) {
            userFileMemory.remove(userId);
        }
    }

    @Tool(description = "获取当前用户最近上传的文件列表。当用户提到'我上传的文件'、'刚上传的文件'、'根据上传的文件'时使用此工具获取文件路径。")
    public String getRecentUploadedFiles() {
        String userId = TodoTools.getCurrentUserId();
        log.info("Tool调用 - getRecentUploadedFiles: userId={}", userId);

        if (userId == null) {
            return "无法获取用户信息，请确认已登录。";
        }

        List<UploadedFile> files = getUploadedFiles(userId);

        if (files.isEmpty()) {
            return "您最近没有上传任何文件。请先上传文件后再进行操作。";
        }

        StringBuilder result = new StringBuilder();
        result.append("您最近上传的文件列表:\n\n");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (int i = 0; i < files.size(); i++) {
            UploadedFile file = files.get(i);
            result.append(String.format("%d. 文件名: %s\n", i + 1, file.getOriginalName()));
            result.append(String.format("   路径: %s\n", file.getFilePath()));
            result.append(String.format("   上传时间: %s\n\n", file.getUploadTime().format(formatter)));
        }

        return result.toString();
    }

    @Tool(description = "获取用户最近上传的一个文件的路径。用于需要获取单个文件路径进行处理的场景，如更新知识库。")
    public String getLatestUploadedFilePath() {
        String userId = TodoTools.getCurrentUserId();
        log.info("Tool调用 - getLatestUploadedFilePath: userId={}", userId);

        if (userId == null) {
            return null;
        }

        List<UploadedFile> files = getUploadedFiles(userId);

        if (files.isEmpty()) {
            return null;
        }

        // 返回最近上传的文件路径
        UploadedFile latestFile = files.get(files.size() - 1);
        log.info("获取到最近上传的文件: {}", latestFile.getFilePath());
        return latestFile.getFilePath();
    }
}
