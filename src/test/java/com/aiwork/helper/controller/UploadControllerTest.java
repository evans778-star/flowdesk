package com.aiwork.helper.controller;

import com.aiwork.helper.exception.BusinessException;
import com.aiwork.helper.repository.ChatLogRepository;
import com.aiwork.helper.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UploadControllerTest {

    private UploadController uploadController;

    @BeforeEach
    void setUp() {
        uploadController = new UploadController(mock(ChatLogRepository.class), mock(AIService.class));
        ReflectionTestUtils.setField(uploadController, "savePath", "target/test-upload/");
        ReflectionTestUtils.setField(uploadController, "host", "http://127.0.0.1:8888");
        ReflectionTestUtils.setField(uploadController, "maxFileSizeBytes", 1024L);
        ReflectionTestUtils.setField(uploadController, "allowedExtensions", new String[]{".txt", ".pdf"});
        ReflectionTestUtils.setField(uploadController, "allowedContentTypes", new String[]{"text/plain", "application/pdf"});
    }

    @Test
    void rejectsPathTraversalFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../secret.txt",
                "text/plain",
                "demo".getBytes()
        );

        assertThatThrownBy(() -> uploadController.uploadFile(file, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("非法文件名");
    }

    @Test
    void rejectsUnsupportedMimeType() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.txt",
                "application/x-msdownload",
                "demo".getBytes()
        );

        assertThatThrownBy(() -> uploadController.uploadFile(file, null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("不支持的MIME类型");
    }
}
