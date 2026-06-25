package com.aiwork.helper.ai.knowledge;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF文件处理器
 *
 * 功能:
 * - 提取PDF文本
 * - 文本清理
 * - 文本分块
 */
@Slf4j
@Component
public class PDFProcessor {

    /**
     * 从PDF文件提取文本
     *
     * @param filePath PDF文件路径
     * @return 提取的文本
     */
    public String extractTextFromPDF(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("文件不存在: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new IOException("PDF文件中没有提取到有效文本内容");
            }

            // 清理文本
            String cleanedText = cleanText(text);

            log.info("PDF文本提取���功，总页数: {}, 总长度: {} 字符",
                    document.getNumberOfPages(), cleanedText.length());

            return cleanedText;
        }
    }

    /**
     * 清理提取的文本
     */
    private String cleanText(String text) {
        // 去除多余的空白字符
        text = text.trim();

        // 将多个连续的空行合并为单个空行
        String[] lines = text.split("\n");
        List<String> cleanedLines = new ArrayList<>();
        boolean prevEmpty = false;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                if (!prevEmpty) {
                    cleanedLines.add("");
                    prevEmpty = true;
                }
            } else {
                cleanedLines.add(line);
                prevEmpty = false;
            }
        }

        return String.join("\n", cleanedLines);
    }

    /**
     * 加载PDF并分割成文档块
     *
     * @param filePath PDF文件路径
     * @param chunkSize 块大小(字符数)
     * @param chunkOverlap 块重叠大小
     * @return 文档块列表
     */
    public List<DocumentChunk> loadAndSplitPDF(String filePath, int chunkSize, int chunkOverlap) throws IOException {
        // 提取PDF文本
        String text = extractTextFromPDF(filePath);

        // 分割文本
        List<String> chunks = splitText(text, chunkSize, chunkOverlap);

        // 转换为文档块
        List<DocumentChunk> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk doc = new DocumentChunk();
            doc.setContent(chunks.get(i));
            doc.setSource(filePath);
            doc.setChunkIndex(i);
            documents.add(doc);
        }

        log.info("PDF分割完成，共生成 {} 个文档块", documents.size());
        return documents;
    }

    /**
     * 将文本分割成指定大小的块
     */
    private List<String> splitText(String text, int chunkSize, int chunkOverlap) {
        if (chunkSize <= 0) {
            chunkSize = 1000; // 默认块大小
        }
        if (chunkOverlap < 0) {
            chunkOverlap = 0;
        }

        List<String> chunks = new ArrayList<>();
        int textLen = text.length();
        int start = 0;

        while (start < textLen) {
            int end = Math.min(start + chunkSize, textLen);
            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            // 计算下一个块的起始位置
            if (end >= textLen) {
                break;
            }
            start = end - chunkOverlap;
            if (start < 0) {
                start = 0;
            }
        }

        return chunks;
    }

    /**
     * 文档块
     */
    public static class DocumentChunk {
        private String content;      // 文档内容
        private String source;       // 来源文件路径
        private int chunkIndex;      // 块索引
        private float[] embedding;   // 向量（可选）

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public int getChunkIndex() {
            return chunkIndex;
        }

        public void setChunkIndex(int chunkIndex) {
            this.chunkIndex = chunkIndex;
        }

        public float[] getEmbedding() {
            return embedding;
        }

        public void setEmbedding(float[] embedding) {
            this.embedding = embedding;
        }
    }
}
