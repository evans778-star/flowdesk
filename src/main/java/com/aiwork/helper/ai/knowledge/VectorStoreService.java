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
import org.springframework.data.redis.connection.RedisConnection;
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
 * 鍚戦噺瀛樺偍鏈嶅姟
 *
 * 鍔熻兘:
 * - 鏂囨湰鍚戦噺鍖?浣跨敤DashScope Embedding API)
 * - 鍚戦噺瀛樺偍(浣跨敤Redis + RediSearch鍚戦噺绱㈠紩)
 * - 鐩镐技搴︽绱?浣跨敤RediSearch FT.SEARCH杩涜KNN鎼滅储)
 */
@Slf4j
@Service
@Conditional(AiEnabledCondition.class)
public class VectorStoreService {

    private final FlowdeskEmbeddingClient embeddingClient;
    private final FlowdeskAiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * Jedis connection used for RediSearch commands.
     */
    private JedisPooled jedis;
    private volatile boolean available;
    private volatile String unavailableReason = "Vector store has not been initialized";

    @Value("${spring.data.redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    /**
     * Redis 鍚戦噺绱㈠紩閰嶇疆锛圝ava 鐗堟湰鐙珛绱㈠紩锛?
     */
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

    /**
     * Initialize Redis connection and vector index.
     */
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

    /**
     * Close Redis resources.
     */
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

    /**
     * 鍒涘缓 Redis 鍚戦噺绱㈠紩锛堝鏋滀笉瀛樺湪锛?
     */
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

    /**
     * 娣诲姞鏂囨。鍒板悜閲忓簱
     * 濡傛灉鍚屼竴鏉ユ簮鐨勬枃妗ｅ凡瀛樺湪锛屽垯鍏堝垹闄ゆ棫鏂囨。鍐嶆坊鍔犳柊鏂囨。
     *
     * @param chunks 鏂囨。鍧楀垪琛?
     */
    public void addDocuments(List<PDFProcessor.DocumentChunk> chunks) throws Exception {
        if (chunks.isEmpty()) {
            log.warn("鏂囨。鍧楀垪琛ㄤ负绌猴紝璺宠繃娣诲姞");
            return;
        }

        // 鑾峰彇鏂囨。鏉ユ簮锛堟墍鏈?chunk 鏉ユ簮鐩稿悓锛?
        if (isUnavailable("addDocuments")) {
            return;
        }

        String source = chunks.get(0).getSource();
        log.info("寮€濮嬫坊鍔?{} 涓枃妗ｅ潡鍒?Redis 鍚戦噺搴擄紝鏉ユ簮: {}", chunks.size(), source);

        // 鍏堝垹闄ゅ悓涓€鏉ユ簮鐨勬棫鏂囨。锛堝疄鐜拌鐩栨洿鏂帮級
        int deletedCount = deleteBySource(source);
        if (deletedCount > 0) {
            log.info("宸插垹闄ゆ潵婧?{} 鐨?{} 涓棫鏂囨。", source, deletedCount);
        }

        for (PDFProcessor.DocumentChunk chunk : chunks) {
            // 1. 鐢熸垚鏂囨。鍚戦噺
            float[] embedding = generateEmbedding(chunk.getContent());

            // 2. 鐢熸垚鏂囨。 ID
            String docId = docPrefix + UUID.randomUUID().toString();

            // 3. 灏嗗悜閲忚浆鎹负浜岃繘鍒舵牸寮?
            byte[] vectorBytes = floatArrayToBytes(embedding);

            // 4. Store into Redis Hash.
            String content = chunk.getContent();
            String chunkSource = chunk.getSource();
            int page = chunk.getChunkIndex();

            // 浣跨敤 HashMap 鏋勫缓瀛楁锛堝洜涓?byte[] 涓嶈兘鐩存帴鐢ㄤ簬 Map.of锛?
            java.util.Map<byte[], byte[]> fields = new java.util.HashMap<>();
            fields.put("content".getBytes(StandardCharsets.UTF_8), content.getBytes(StandardCharsets.UTF_8));
            fields.put("source".getBytes(StandardCharsets.UTF_8), chunkSource.getBytes(StandardCharsets.UTF_8));
            fields.put("page".getBytes(StandardCharsets.UTF_8), String.valueOf(page).getBytes(StandardCharsets.UTF_8));
            fields.put("content_vector".getBytes(StandardCharsets.UTF_8), vectorBytes);

            jedis.hset(docId.getBytes(StandardCharsets.UTF_8), fields);

            log.debug("鏂囨。宸插瓨鍌? {}", docId);
        }

        log.info("鏂囨。娣诲姞瀹屾垚锛屽綋鍓嶅悜閲忓簱澶у皬: {}", size());
    }

    /**
     * 鏍规嵁鏉ユ簮鍒犻櫎鏂囨。
     * 鐢ㄤ簬鍦ㄦ洿鏂版枃妗ｅ墠鍒犻櫎鍚屼竴鏉ユ簮鐨勬棫鏂囨。
     *
     * @param source 鏂囨。鏉ユ簮锛堟枃浠惰矾寰勶級
     * @return 鍒犻櫎鐨勬枃妗ｆ暟閲?
     */
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

    /**
     * 鎼滅储鐩镐技鏂囨。
     *
     * @param query 鏌ヨ鏂囨湰
     * @param topK 杩斿洖鍓岾涓渶鐩镐技鐨勬枃妗?
     * @return 鐩镐技鏂囨。鍒楄〃
     */
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

    /**
     * 瑙ｆ瀽 FT.SEARCH 杩斿洖鐨勭粨鏋?
     * 鏀寔 RESP2 鍜?RESP3 鏍煎紡
     */
    @SuppressWarnings("unchecked")
    private List<StoredDocument> parseSearchResults(Object result) {
        List<StoredDocument> documents = new ArrayList<>();

        if (result == null) {
            log.warn("鎼滅储缁撴灉涓?null");
            return documents;
        }

        try {
            List<Object> resultList = (List<Object>) result;
            log.info("缁撴灉鍒楄〃澶у皬: {}", resultList.size());

            if (resultList.isEmpty()) {
                log.warn("缁撴灉鍒楄〃涓虹┖");
                return documents;
            }

            // 鎵撳嵃璇︾粏鐨勭粨鏋滅粨鏋勭敤浜庤皟璇?
            for (int i = 0; i < Math.min(resultList.size(), 5); i++) {
                Object item = resultList.get(i);
                String itemStr = item instanceof byte[] ? new String((byte[]) item) : String.valueOf(item);
                log.info("缁撴灉鍏冪礌[{}]: type={}, value={}", i, item.getClass().getSimpleName(),
                        itemStr.length() > 100 ? itemStr.substring(0, 100) + "..." : itemStr);
            }

            // 妫€娴嬫槸 RESP2 杩樻槸 RESP3 鏍煎紡
            Object firstElement = resultList.get(0);
            String firstStr = firstElement instanceof byte[] ? new String((byte[]) firstElement) : String.valueOf(firstElement);

            // RESP3 鏍煎紡锛氱涓€涓厓绱犳槸 "attributes" 鎴栧叾浠栧瓧绗︿覆 key
            if (firstStr.equals("attributes") || firstStr.equals("total_results") || firstStr.equals("results")) {
                log.info("妫€娴嬪埌 RESP3 鏍煎紡锛屼娇鐢?Map 瑙ｆ瀽");
                parseResp3Results(resultList, documents);
            } else {
                // RESP2 鏍煎紡锛氱涓€涓厓绱犳槸鍖归厤鏁伴噺
                log.info("Detected RESP2 search result format");
                parseResp2Results(resultList, documents);
            }
        } catch (Exception e) {
            log.error("瑙ｆ瀽鎼滅储缁撴灉澶辫触: {}", e.getMessage(), e);
        }

        log.info("Parsed {} stored documents", documents.size());
        return documents;
    }

    /**
     * 瑙ｆ瀽 RESP2 鏍煎紡鐨勭粨鏋?
     */
    @SuppressWarnings("unchecked")
    private void parseResp2Results(List<Object> resultList, List<StoredDocument> documents) {
        // 绗竴涓厓绱犳槸鍖归厤鐨勬枃妗ｆ暟閲?
        Object countObj = resultList.get(0);
        long matchCount = countObj instanceof Long ? (Long) countObj :
                (countObj instanceof byte[] ? Long.parseLong(new String((byte[]) countObj)) : 0);
        log.info("RESP2 鎼滅储鍖归厤鏁伴噺: {}", matchCount);

        // 涔嬪悗姣忎袱涓厓绱犱负涓€缁勶細鏂囨。key, [瀛楁鍒楄〃]
        for (int i = 1; i < resultList.size(); i += 2) {
            if (i + 1 >= resultList.size()) break;

            Object keyObj = resultList.get(i);
            String docKey = keyObj instanceof byte[] ? new String((byte[]) keyObj) : keyObj.toString();

            Object fieldsObj = resultList.get(i + 1);
            if (!(fieldsObj instanceof List)) continue;

            List<Object> fields = (List<Object>) fieldsObj;
            StoredDocument doc = parseDocumentFields(docKey, fields);
            if (doc != null) {
                documents.add(doc);
            }
        }
    }

    /**
     * 瑙ｆ瀽 RESP3 鏍煎紡鐨勭粨鏋滐紙map 缁撴瀯锛?
     */
    @SuppressWarnings("unchecked")
    private void parseResp3Results(List<Object> resultList, List<StoredDocument> documents) {
        // RESP3 杩斿洖鐨勬槸鎵佸钩鍖栫殑 map: [key1, value1, key2, value2, ...]
        // 闇€瑕佹壘鍒?"results" 瀵瑰簲鐨勫€?
        for (int i = 0; i < resultList.size() - 1; i += 2) {
            Object keyObj = resultList.get(i);
            String key = keyObj instanceof byte[] ? new String((byte[]) keyObj) : String.valueOf(keyObj);

            if ("total_results".equals(key)) {
                Object countObj = resultList.get(i + 1);
                long count = countObj instanceof Long ? (Long) countObj :
                        (countObj instanceof byte[] ? Long.parseLong(new String((byte[]) countObj)) : 0);
                log.info("RESP3 鎬诲尮閰嶆暟閲? {}", count);
            } else if ("results".equals(key)) {
                Object resultsObj = resultList.get(i + 1);
                if (resultsObj instanceof List) {
                    List<Object> results = (List<Object>) resultsObj;
                    log.info("RESP3 缁撴灉鍒楄〃澶у皬: {}", results.size());
                    parseResp3DocumentList(results, documents);
                }
            }
        }
    }

    /**
     * 瑙ｆ瀽 RESP3 鏍煎紡鐨勬枃妗ｅ垪琛?
     */
    @SuppressWarnings("unchecked")
    private void parseResp3DocumentList(List<Object> results, List<StoredDocument> documents) {
        for (Object docObj : results) {
            if (!(docObj instanceof List)) continue;

            List<Object> docMap = (List<Object>) docObj;
            String docId = null;
            List<Object> extraAttrs = null;

            // 瑙ｆ瀽鏂囨。鐨?map 缁撴瀯: [key1, value1, key2, value2, ...]
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

    /**
     * 瑙ｆ瀽鏂囨。瀛楁
     */
    private StoredDocument parseDocumentFields(String docId, List<Object> fields) {
        StoredDocument doc = new StoredDocument();
        doc.setId(docId);

        // 瑙ｆ瀽瀛楁锛堝瓧娈靛悕鍜屽€间氦鏇垮嚭鐜帮級
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
            }
        }

        if (doc.getContent() != null && !doc.getContent().isEmpty()) {
            log.info("鎴愬姛瑙ｆ瀽鏂囨。: id={}, contentLength={}", doc.getId(), doc.getContent().length());
            return doc;
        } else {
            log.warn("鏂囨。鍐呭涓虹┖锛岃烦杩? {}", docId);
            return null;
        }
    }

    /**
     * 灏?float 鏁扮粍杞崲涓哄瓧鑺傛暟缁勶紙灏忕搴忥紝涓?Redis 鍚戦噺鏍煎紡鍏煎锛?
     */
    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (float f : floats) {
            buffer.putFloat(f);
        }
        return buffer.array();
    }

    /**
     * 浣跨敤DashScope Embedding API鐢熸垚鏂囨湰鍚戦噺
     *
     * @param text 鏂囨湰
     * @return 鍚戦噺
     */
    private float[] generateEmbedding(String text) {
        float[] embedding = embeddingClient.embed(text);
        if (embedding.length != vectorDim) {
            throw new IllegalStateException("Embedding dimension mismatch. Expected "
                    + vectorDim + " but got " + embedding.length
                    + ". Rebuild the knowledge index or update flowdesk.ai.embedding-dimension.");
        }
        return embedding;
    }

    /**
     * 娓呯┖鍚戦噺搴?
     * 鍒犻櫎鎵€鏈夋枃妗ｅ苟閲嶅缓绱㈠紩
     */
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

    /**
     * 鑾峰彇鍚戦噺搴撳ぇ灏?
     * Count stored vector documents.
     */
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

    /**
     * 瀛樺偍鐨勬枃妗?
     */
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
