package com.aiwork.helper.repository;

import com.aiwork.helper.entity.ChatLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 聊天记录数据访问接口
 */
@Repository
public interface ChatLogRepository extends MongoRepository<ChatLog, String> {

    /**
     * 根据会话ID查找聊天记录 (分页)
     */
    Page<ChatLog> findByConversationId(String conversationId, Pageable pageable);

    /**
     * 根据会话ID和时间范围查找聊天记录
     */
    @Query("{'conversationId': ?0, 'sendTime': {$gte: ?1, $lte: ?2}}")
    Page<ChatLog> findByConversationIdAndTimeRange(String conversationId, Long startTime, Long endTime, Pageable pageable);

    /**
     * 根据发送者ID查找聊天记录
     */
    List<ChatLog> findBySendId(String sendId);

    /**
     * 根据接收者ID查找聊天记录
     */
    List<ChatLog> findByRecvId(String recvId);

    /**
     * 根据聊天类型查找聊天记录
     */
    List<ChatLog> findByChatType(Integer chatType);

    /**
     * 根据发送者ID和接收者ID查找私聊记录
     */
    @Query("{'sendId': ?0, 'recvId': ?1, 'chatType': 2}")
    List<ChatLog> findPrivateChatBetweenUsers(String sendId, String recvId);

    /**
     * 统计会话的消息数量
     */
    Long countByConversationId(String conversationId);

    /**
     * 删除会话的所有消息
     */
    void deleteByConversationId(String conversationId);

    /**
     * 根据会话ID和关系ID查找 (用于AI对话历史)
     */
    @Query("{'conversationId': ?0}")
    List<ChatLog> findByConversationIdOrderBySendTimeAsc(String conversationId);
}