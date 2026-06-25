package com.aiwork.helper.ai.knowledge;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PDFProcessorTest {

    private final PDFProcessor pdfProcessor = new PDFProcessor();

    @Test
    void extractsAndSplitsSyntheticHandbookPdf() throws Exception {
        Path samplePdf = Path.of("docs", "examples", "sample-employee-handbook.pdf");

        String text = pdfProcessor.extractTextFromPDF(samplePdf.toString());
        List<PDFProcessor.DocumentChunk> chunks = pdfProcessor.loadAndSplitPDF(samplePdf.toString(), 300, 50);

        assertThat(text).contains("欢迎");
        assertThat(chunks).isNotEmpty();
        assertThat(chunks.getFirst().getChunkIndex()).isZero();
        assertThat(chunks.getFirst().getSource()).isEqualTo(samplePdf.toString());
        assertThat(chunks.getFirst().getContent()).isNotBlank();
    }
}
