package com.aiwork.helper.controller;

import com.aiwork.helper.ai.knowledge.VectorStoreService;
import com.aiwork.helper.config.AiEnabledCondition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 知识库诊断 Controller
 * 用于排查知识库问题
 */
@Slf4j
@Profile("dev")
@RestController
@Conditional(AiEnabledCondition.class)
@RequestMapping("/api/knowledge/diag")
@RequiredArgsConstructor
public class KnowledgeDiagController {

    private final VectorStoreService vectorStoreService;

    /**
     * 获取知识库状态
     */
    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        try {
            result.put("size", vectorStoreService.size());
            result.put("status", "ok");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            log.error("获取知识库状态失败", e);
        }
        return result;
    }

    /**
     * 测试向量搜索
     */
    @GetMapping("/search")
    public Map<String, Object> testSearch(@RequestParam String query) {
        Map<String, Object> result = new HashMap<>();
        result.put("query", query);

        try {
            long startTime = System.currentTimeMillis();

            // 先检查大小
            int size = vectorStoreService.size();
            result.put("vectorStoreSize", size);

            if (size == 0) {
                result.put("status", "empty");
                result.put("message", "向量库为空");
                return result;
            }

            // 执行搜索
            List<VectorStoreService.StoredDocument> docs = vectorStoreService.searchSimilar(query, 3);

            long endTime = System.currentTimeMillis();
            result.put("searchTimeMs", endTime - startTime);
            result.put("resultCount", docs.size());

            if (docs.isEmpty()) {
                result.put("status", "no_results");
                result.put("message", "搜索无结果");
            } else {
                result.put("status", "ok");
                // 返回文档摘要
                List<Map<String, Object>> docSummaries = docs.stream().map(doc -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("id", doc.getId());
                    summary.put("source", doc.getSource());
                    summary.put("chunkIndex", doc.getChunkIndex());
                    summary.put("contentPreview", doc.getContent() != null ?
                            doc.getContent().substring(0, Math.min(200, doc.getContent().length())) : null);
                    return summary;
                }).toList();
                result.put("documents", docSummaries);
            }

        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getClass().getName() + ": " + e.getMessage());
            log.error("测试搜索失败", e);
        }

        return result;
    }

    /**
     * 清空知识库
     */
    @PostMapping("/clear")
    public Map<String, Object> clear() {
        Map<String, Object> result = new HashMap<>();
        try {
            int sizeBefore = vectorStoreService.size();
            vectorStoreService.clear();
            int sizeAfter = vectorStoreService.size();

            result.put("status", "ok");
            result.put("sizeBefore", sizeBefore);
            result.put("sizeAfter", sizeAfter);
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            log.error("清空知识库失败", e);
        }
        return result;
    }
}
