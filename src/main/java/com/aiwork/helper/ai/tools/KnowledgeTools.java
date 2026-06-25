package com.aiwork.helper.ai.tools;

import com.aiwork.helper.ai.knowledge.PDFProcessor;
import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.config.DashScopeAiEnabledCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 知识库工具
 * 提供知识库查询和更新功能
 */
@Slf4j
@Component
@Conditional(DashScopeAiEnabledCondition.class)
@RequiredArgsConstructor
public class KnowledgeTools {

    private final VectorStoreService vectorStoreService;
    private final PDFProcessor pdfProcessor;

    @Tool(description = "从知识库查询答案。当用户询问公司制度、规章、员工手册、请假政策、考勤制度、审批流程等知识相关问题时使用。")
    public String queryKnowledge(
            @ToolParam(description = "用户的问题") String question
    ) {
        log.info("Tool调用 - queryKnowledge: question={}", question);

        try {
            // 1. 从向量库检索相关文档
            List<VectorStoreService.StoredDocument> similarDocs =
                    vectorStoreService.searchSimilar(question, 3);

            if (similarDocs.isEmpty()) {
                return "知识库中没有找到相关信息。请先上传PDF文档来构建知识库。";
            }

            // 2. 构建上下文返回
            StringBuilder context = new StringBuilder();
            context.append("以下是从知识库中检索到的相关信息:\n\n");

            for (int i = 0; i < similarDocs.size(); i++) {
                VectorStoreService.StoredDocument doc = similarDocs.get(i);
                context.append(String.format("【文档%d】\n%s\n\n", i + 1, doc.getContent()));
            }

            return context.toString();

        } catch (Exception e) {
            log.error("知识库查询失败", e);
            return "知识库查询失败: " + e.getMessage();
        }
    }

    @Tool(description = "更新知识库。当用户要根据上传的PDF文件更新知识库时使用。如果用户说'根据我上传的文件更新知识库'，先调用getLatestUploadedFilePath获取文件路径，然后再调用此工具。")
    public String updateKnowledge(
            @ToolParam(description = "PDF文件路径，如果用户没有提供具体路径，请先调用getLatestUploadedFilePath获取", required = false) String filePath
    ) {
        log.info("Tool调用 - updateKnowledge: filePath={}", filePath);

        try {
            // 如果没有提供文件路径，尝试获取最近上传的文件
            if (filePath == null || filePath.isEmpty()) {
                String userId = TodoTools.getCurrentUserId();
                if (userId != null) {
                    var files = FileTools.getUploadedFiles(userId);
                    if (!files.isEmpty()) {
                        filePath = files.get(files.size() - 1).getFilePath();
                        log.info("自动获取最近上传的文件: {}", filePath);
                    }
                }
            }

            if (filePath == null || filePath.isEmpty()) {
                return "请先上传PDF文件，或提供PDF文件路径。示例: upload/员工手册.pdf";
            }

            // 转换为绝对路径
            File file = new File(filePath);
            if (!file.isAbsolute()) {
                String workDir = System.getProperty("user.dir");
                filePath = new File(workDir, filePath).getAbsolutePath();
            }

            // 检查文件是否存在
            if (!new File(filePath).exists()) {
                return "文件不存在: " + filePath;
            }

            log.info("开始处理PDF文件: {}", filePath);

            // 1. 处理PDF文件
            List<PDFProcessor.DocumentChunk> chunks =
                    pdfProcessor.loadAndSplitPDF(filePath, 500, 50);

            log.info("PDF分块完成，共 {} 个块", chunks.size());

            // 2. 添加到向量库
            vectorStoreService.addDocuments(chunks);

            return String.format("知识库更新成功!\n文件: %s\n文档块数: %d\n当前知识库大小: %d",
                    filePath, chunks.size(), vectorStoreService.size());

        } catch (Exception e) {
            log.error("知识库更新失败", e);
            return "知识库更新失败: " + e.getMessage();
        }
    }

    @Tool(description = "清空知识库。当用户要清空或重置知识库时使用。")
    public String clearKnowledge() {
        log.info("Tool调用 - clearKnowledge");

        try {
            int sizeBefore = vectorStoreService.size();
            vectorStoreService.clear();
            return String.format("知识库已清空!\n清空前文档数: %d\n当前知识库大小: %d",
                    sizeBefore, vectorStoreService.size());
        } catch (Exception e) {
            log.error("清空知识库失败", e);
            return "清空知识库失败: " + e.getMessage();
        }
    }
}
