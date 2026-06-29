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
 * Extracts and chunks PDF text for knowledge-base indexing.
 */
@Slf4j
@Component
public class PDFProcessor {

    public String extractTextFromPDF(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File does not exist: " + filePath);
        }

        try (PDDocument document = Loader.loadPDF(file)) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text == null || text.trim().isEmpty()) {
                throw new IOException("PDF does not contain extractable text");
            }

            String cleanedText = cleanText(text);

            log.info("PDF text extraction succeeded, pages={}, length={}",
                    document.getNumberOfPages(), cleanedText.length());

            return cleanedText;
        }
    }

    private String cleanText(String text) {
        text = text.trim();

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

    public List<DocumentChunk> loadAndSplitPDF(String filePath, int chunkSize, int chunkOverlap) throws IOException {
        String text = extractTextFromPDF(filePath);
        List<String> chunks = splitText(text, chunkSize, chunkOverlap);

        List<DocumentChunk> documents = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk doc = new DocumentChunk();
            doc.setContent(chunks.get(i));
            doc.setSource(filePath);
            doc.setChunkIndex(i);
            documents.add(doc);
        }

        log.info("PDF split completed, chunks={}", documents.size());
        return documents;
    }

    private List<String> splitText(String text, int chunkSize, int chunkOverlap) {
        if (chunkSize <= 0) {
            chunkSize = 1000;
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

    public static class DocumentChunk {
        private String content;
        private String source;
        private int chunkIndex;
        private float[] embedding;

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
