package com.aiwork.helper.ai.knowledge;

import com.aiwork.helper.ai.embedding.FlowdeskEmbeddingClient;
import com.aiwork.helper.config.AiEnabledCondition;
import com.aiwork.helper.config.FlowdeskAiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.commands.ProtocolCommand;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Redis Stack / RediSearch backed vector store for local RAG experiments.
 */
@Slf4j
@Service
@Conditional(AiEnabledCondition.class)
public class VectorStoreService {

    private final FlowdeskEmbeddingClient embeddingClient;
    private final FlowdeskAiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    private JedisPooled jedis;
    private volatile boolean available;
    private volatile String unavailableReason = "Vector store has not been initialized";

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    private String indexName;
    private String docPrefix;
    private int vectorDim;

    public VectorStoreService(FlowdeskEmbeddingClient embeddingClient,
                              FlowdeskAiProperties aiProperties,
                              ObjectMapper objectMapper,
                              StringRedisTemplate redisTemplate) {
        this.embeddingClient = embeddingClient;
        this.aiProperties = aiProperties;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        indexName = aiProperties.resolveVectorIndexName();
        docPrefix = "doc:" + indexName + ":";
        vectorDim = aiProperties.resolveEmbeddingDimension();

        String redisUri = String.format("redis://%s:%d", redisHost, redisPort);
        try {
            jedis = new JedisPooled(redisHost, redisPort);
            jedis.ping();
            log.info("Jedis connection created: {}, index={}, dimension={}", redisUri, indexName, vectorDim);

            createIndexIfNotExists();
            available = true;
            unavailableReason = null;
        } catch (Exception e) {
            available = false;
            unavailableReason = e.getMessage();
            closeJedisResources();
            log.warn("Redis vector store is unavailable; AI chat can still start, but knowledge-base RAG is disabled. reason={}",
                    e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        closeJedisResources();
        available = false;
        unavailableReason = "Vector store has been closed";
        log.info("Jedis connection closed");
    }

    private void closeJedisResources() {
        if (jedis != null) {
            jedis.close();
            jedis = null;
        }
    }

    private void createIndexIfNotExists() {
        try {
            try {
                jedis.sendCommand(RediSearchCommand.FT_INFO, bytes(indexName));
                log.info("Redis vector index {} already exists", indexName);
                return;
            } catch (Exception e) {
                log.info("Redis vector index {} does not exist; creating it", indexName);
            }

            jedis.sendCommand(RediSearchCommand.FT_CREATE,
                    bytes(indexName),
                    bytes("ON"),
                    bytes("HASH"),
                    bytes("PREFIX"),
                    bytes("1"),
                    bytes(docPrefix),
                    bytes("SCHEMA"),
                    bytes("content"),
                    bytes("TEXT"),
                    bytes("source"),
                    bytes("TEXT"),
                    bytes("page"),
                    bytes("NUMERIC"),
                    bytes("content_vector"),
                    bytes("VECTOR"),
                    bytes("FLAT"),
                    bytes("6"),
                    bytes("TYPE"),
                    bytes("FLOAT32"),
                    bytes("DIM"),
                    bytes(String.valueOf(vectorDim)),
                    bytes("DISTANCE_METRIC"),
                    bytes("COSINE"));
            log.info("Redis vector index {} created", indexName);
        } catch (Exception e) {
            log.warn("Redis vector index initialization failed: {}", e.getMessage());
        }
    }

    public void addDocuments(List<PDFProcessor.DocumentChunk> chunks) throws Exception {
        if (chunks.isEmpty()) {
            log.warn("No document chunks were provided for vector indexing");
            return;
        }

        if (isUnavailable("addDocuments")) {
            return;
        }

        String source = chunks.get(0).getSource();
        log.info("Adding {} document chunks to Redis vector store, source={}", chunks.size(), source);

        int deletedCount = deleteBySource(source);
        if (deletedCount > 0) {
            log.info("Replaced {} existing vector documents for source={}", deletedCount, source);
        }

        for (PDFProcessor.DocumentChunk chunk : chunks) {
            float[] embedding = generateEmbedding(chunk.getContent());
            String docId = docPrefix + UUID.randomUUID();
            byte[] vectorBytes = floatArrayToBytes(embedding);

            java.util.Map<byte[], byte[]> fields = new java.util.HashMap<>();
            fields.put(bytes("content"), chunk.getContent().getBytes(StandardCharsets.UTF_8));
            fields.put(bytes("source"), chunk.getSource().getBytes(StandardCharsets.UTF_8));
            fields.put(bytes("page"), String.valueOf(chunk.getChunkIndex()).getBytes(StandardCharsets.UTF_8));
            fields.put(bytes("content_vector"), vectorBytes);

            jedis.hset(bytes(docId), fields);

            log.debug("Stored vector document: id={}", docId);
        }

        log.info("Vector document indexing completed; totalStored={}", size());
    }

    public int deleteBySource(String source) {
        if (isUnavailable("deleteBySource")) {
            return 0;
        }
        int deletedCount = 0;
        try {
            Set<byte[]> keys = jedis.keys(bytes(docPrefix + "*"));
            if (keys == null || keys.isEmpty()) {
                return 0;
            }

            for (byte[] key : keys) {
                byte[] sourceValue = jedis.hget(key, bytes("source"));
                if (sourceValue != null) {
                    String docSource = new String(sourceValue, StandardCharsets.UTF_8);
                    if (source.equals(docSource)) {
                        jedis.del(key);
                        deletedCount++;
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete vector documents by source: source={}, error={}", source, e.getMessage());
        }
        return deletedCount;
    }

    public List<StoredDocument> searchSimilar(String query, int topK) throws Exception {
        if (isUnavailable("searchSimilar")) {
            return new ArrayList<>();
        }
        int currentSize = size();
        log.info("Starting vector search: query={}, topK={}, size={}", query, topK, currentSize);

        if (currentSize == 0) {
            log.warn("Vector store is empty; search skipped");
            return new ArrayList<>();
        }

        float[] queryEmbedding = generateEmbedding(query);
        byte[] queryVectorBytes = floatArrayToBytes(queryEmbedding);
        String searchQuery = "*=>[KNN " + topK + " @content_vector $query_vec AS score]";

        try {
            Object result = jedis.sendCommand(RediSearchCommand.FT_SEARCH,
                    bytes(indexName),
                    bytes(searchQuery),
                    bytes("PARAMS"),
                    bytes("2"),
                    bytes("query_vec"),
                    queryVectorBytes,
                    bytes("SORTBY"),
                    bytes("score"),
                    bytes("RETURN"),
                    bytes("4"),
                    bytes("content"),
                    bytes("source"),
                    bytes("page"),
                    bytes("score"),
                    bytes("DIALECT"),
                    bytes("2"));

            List<StoredDocument> results = parseSearchResults(result);
            log.info("Vector search returned {} documents", results.size());
            return results;
        } catch (Exception e) {
            log.error("Vector search failed: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @SuppressWarnings("unchecked")
    private List<StoredDocument> parseSearchResults(Object result) {
        List<StoredDocument> documents = new ArrayList<>();

        if (result == null) {
            log.warn("Vector search result was null");
            return documents;
        }

        try {
            List<Object> resultList = (List<Object>) result;
            log.debug("Raw vector search result size={}", resultList.size());

            if (resultList.isEmpty()) {
                log.warn("Vector search result was empty");
                return documents;
            }

            for (int i = 0; i < Math.min(resultList.size(), 5); i++) {
                Object item = resultList.get(i);
                String itemStr = item instanceof byte[] ? new String((byte[]) item) : String.valueOf(item);
                log.debug("Vector search raw item[{}]: type={}, value={}", i, item.getClass().getSimpleName(),
                        itemStr.length() > 100 ? itemStr.substring(0, 100) + "..." : itemStr);
            }

            Object firstElement = resultList.get(0);
            String firstStr = firstElement instanceof byte[] ? new String((byte[]) firstElement) : String.valueOf(firstElement);

            if (firstStr.equals("attributes") || firstStr.equals("total_results") || firstStr.equals("results")) {
                log.debug("Detected RESP3 search result format");
                parseResp3Results(resultList, documents);
            } else {
                log.debug("Detected RESP2 search result format");
                parseResp2Results(resultList, documents);
            }
        } catch (Exception e) {
            log.error("Failed to parse vector search results: {}", e.getMessage(), e);
        }

        log.info("Parsed {} stored documents", documents.size());
        return documents;
    }

    @SuppressWarnings("unchecked")
    private void parseResp2Results(List<Object> resultList, List<StoredDocument> documents) {
        Object countObj = resultList.get(0);
        long matchCount = countObj instanceof Long ? (Long) countObj :
                (countObj instanceof byte[] ? Long.parseLong(new String((byte[]) countObj)) : 0);
        log.debug("RESP2 vector search matched {} documents", matchCount);

        for (int i = 1; i < resultList.size(); i += 2) {
            if (i + 1 >= resultList.size()) {
                break;
            }

            Object keyObj = resultList.get(i);
            String docKey = keyObj instanceof byte[] ? new String((byte[]) keyObj) : keyObj.toString();

            Object fieldsObj = resultList.get(i + 1);
            if (!(fieldsObj instanceof List)) {
                continue;
            }

            List<Object> fields = (List<Object>) fieldsObj;
            StoredDocument doc = parseDocumentFields(docKey, fields);
            if (doc != null) {
                documents.add(doc);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseResp3Results(List<Object> resultList, List<StoredDocument> documents) {
        for (int i = 0; i < resultList.size() - 1; i += 2) {
            Object keyObj = resultList.get(i);
            String key = keyObj instanceof byte[] ? new String((byte[]) keyObj) : String.valueOf(keyObj);

            if ("total_results".equals(key)) {
                Object countObj = resultList.get(i + 1);
                long count = countObj instanceof Long ? (Long) countObj :
                        (countObj instanceof byte[] ? Long.parseLong(new String((byte[]) countObj)) : 0);
                log.debug("RESP3 vector search matched {} documents", count);
            } else if ("results".equals(key)) {
                Object resultsObj = resultList.get(i + 1);
                if (resultsObj instanceof List) {
                    List<Object> results = (List<Object>) resultsObj;
                    log.debug("RESP3 vector search returned {} result rows", results.size());
                    parseResp3DocumentList(results, documents);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void parseResp3DocumentList(List<Object> results, List<StoredDocument> documents) {
        for (Object docObj : results) {
            if (!(docObj instanceof List)) {
                continue;
            }

            List<Object> docMap = (List<Object>) docObj;
            String docId = null;
            List<Object> extraAttrs = null;

            for (int i = 0; i < docMap.size() - 1; i += 2) {
                Object keyObj = docMap.get(i);
                String key = keyObj instanceof byte[] ? new String((byte[]) keyObj) : String.valueOf(keyObj);

                if ("id".equals(key)) {
                    Object idObj = docMap.get(i + 1);
                    docId = idObj instanceof byte[] ? new String((byte[]) idObj) : String.valueOf(idObj);
                } else if ("extra_attributes".equals(key)) {
                    Object attrsObj = docMap.get(i + 1);
                    if (attrsObj instanceof List) {
                        extraAttrs = (List<Object>) attrsObj;
                    }
                }
            }

            if (docId != null && extraAttrs != null) {
                StoredDocument doc = parseDocumentFields(docId, extraAttrs);
                if (doc != null) {
                    documents.add(doc);
                }
            }
        }
    }

    private StoredDocument parseDocumentFields(String docId, List<Object> fields) {
        StoredDocument doc = new StoredDocument();
        doc.setId(docId);

        for (int j = 0; j < fields.size() - 1; j += 2) {
            Object fieldNameObj = fields.get(j);
            Object fieldValueObj = fields.get(j + 1);

            String fieldName = fieldNameObj instanceof byte[]
                    ? new String((byte[]) fieldNameObj)
                    : fieldNameObj.toString();
            String fieldValue = fieldValueObj instanceof byte[]
                    ? new String((byte[]) fieldValueObj)
                    : fieldValueObj.toString();

            switch (fieldName) {
                case "content":
                    doc.setContent(fieldValue);
                    break;
                case "source":
                    doc.setSource(fieldValue);
                    break;
                case "page":
                    try {
                        doc.setChunkIndex(Integer.parseInt(fieldValue));
                    } catch (NumberFormatException e) {
                        doc.setChunkIndex(0);
                    }
                    break;
                case "score":
                    try {
                        doc.setScore(Double.parseDouble(fieldValue));
                    } catch (NumberFormatException e) {
                        doc.setScore(null);
                    }
                    break;
                default:
                    break;
            }
        }

        if (doc.getContent() != null && !doc.getContent().isEmpty()) {
            log.debug("Parsed vector document: id={}, contentLength={}", doc.getId(), doc.getContent().length());
            return doc;
        }
        log.warn("Vector search result did not contain document content: id={}", docId);
        return null;
    }

    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    private float[] generateEmbedding(String text) {
        float[] embedding = embeddingClient.embed(text);
        if (embedding.length != vectorDim) {
            throw new IllegalStateException("Embedding dimension mismatch. Expected "
                    + vectorDim + " but got " + embedding.length
                    + ". Rebuild the knowledge index or update flowdesk.ai.embedding-dimension.");
        }
        return embedding;
    }

    public void clear() {
        if (isUnavailable("clear")) {
            return;
        }
        try {
            try {
                jedis.sendCommand(RediSearchCommand.FT_DROPINDEX, bytes(indexName));
                log.info("Dropped Redis vector index {}", indexName);
            } catch (Exception e) {
                log.warn("Failed to drop Redis vector index: {}", e.getMessage());
            }

            Set<byte[]> keys = jedis.keys(bytes(docPrefix + "*"));
            if (keys != null && !keys.isEmpty()) {
                jedis.del(keys.toArray(new byte[0][]));
                log.info("Deleted {} stored documents", keys.size());
            }

            createIndexIfNotExists();
            log.info("Vector store reset completed");
        } catch (Exception e) {
            log.error("Failed to clear vector store: {}", e.getMessage());
        }
    }

    public int size() {
        if (isUnavailable("size")) {
            return 0;
        }
        try {
            Set<byte[]> keys = jedis.keys(bytes(docPrefix + "*"));
            int count = keys != null ? keys.size() : 0;
            log.debug("Vector store size: {}", count);
            return count;
        } catch (Exception e) {
            log.warn("Failed to count vector documents: {}", e.getMessage());
        }
        return 0;
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private boolean isUnavailable(String operation) {
        if (available && jedis != null) {
            return false;
        }
        log.warn("Vector store is unavailable; {} skipped. reason={}", operation, unavailableReason);
        return true;
    }

    private enum RediSearchCommand implements ProtocolCommand {
        FT_INFO("FT.INFO"),
        FT_CREATE("FT.CREATE"),
        FT_SEARCH("FT.SEARCH"),
        FT_DROPINDEX("FT.DROPINDEX");

        private final byte[] bytes;

        RediSearchCommand(String name) {
            this.bytes = name.getBytes(StandardCharsets.US_ASCII);
        }

        @Override
        public byte[] getRaw() {
            return bytes;
        }
    }

    public static class StoredDocument {
        private String id;
        private String content;
        private String source;
        private int chunkIndex;
        private Double score;
        private float[] embedding;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

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

        public Double getScore() {
            return score;
        }

        public void setScore(Double score) {
            this.score = score;
        }

        public float[] getEmbedding() {
            return embedding;
        }

        public void setEmbedding(float[] embedding) {
            this.embedding = embedding;
        }
    }
}
