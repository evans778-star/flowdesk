package com.aiwork.helper.websocket;

import com.aiwork.helper.dto.websocket.ChatMessage;
import com.aiwork.helper.entity.enums.ChatType;
import com.aiwork.helper.service.ChatService;
import com.aiwork.helper.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;



/**
 * WebSocket聊天处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ChatService chatService;
    private final GroupService groupService;

    /**
     * 用户ID到WebSocket会话的映射
     */
    private final Map<String, WebSocketSession> uidToSession = new ConcurrentHashMap<>();

    /**
     * WebSocket会话到用户ID的映射
     */
    private final Map<String, String> sessionToUid = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // 从会话属性中获取用户ID（在握手拦截器中设置）
        String userId = (String) session.getAttributes().get("userId");
        if (userId == null) {
            log.warn("WebSocket连接建立失败: 缺少userId");
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭WebSocket连接失败", e);
            }
            return;
        }

        addConnection(session, userId);
        log.info("WebSocket连接已建立: userId={}, sessionId={}", userId, session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, org.springframework.web.socket.WebSocketMessage<?> message) {
        String userId = sessionToUid.get(session.getId());
        if (userId == null) {
            log.warn("收到消息但找不到对应的用户ID: sessionId={}", session.getId());
            return;
        }

        try {
            // 获取原始payload
            String payload = ((TextMessage) message).getPayload();

            // 如果是空消息或心跳包，直接忽略（不打印警告）
            if (payload == null || payload.trim().isEmpty() || "{}".equals(payload.trim())) {
                log.debug("收到空消息或心跳包，已忽略: userId={}", userId);
                return;
            }

            // 解析客户端发送的JSON消息
            ChatMessage wsMessage = objectMapper.readValue(payload, ChatMessage.class);

            // 设置发送者ID（从Token中获取，防止伪造）
            wsMessage.setSendId(userId);

            log.info("收到WebSocket消息: userId={}, chatType={}, content={}",
                    userId, wsMessage.getChatType(), wsMessage.getContent());

            // 根据聊天类型分发消息处理
            if (wsMessage.getChatType() == null) {
                // 降低日志级别为debug，因为这可能是前端的心跳包或其他非聊天消息
                log.debug("收到非聊天消息（chatType为null），原始消息: {}", payload);
                return;
            }

            if (wsMessage.getChatType().equals(ChatType.GROUP.getValue())) {
                // 群聊消息 (chatType = 1)
                handleGroupChat(wsMessage);
            } else if (wsMessage.getChatType().equals(ChatType.SINGLE.getValue())) {
                // 私聊消息 (chatType = 2)
                handlePrivateChat(wsMessage);
            } else {
                log.warn("未知的聊天类型: {}", wsMessage.getChatType());
            }

        } catch (Exception e) {
            log.error("处理WebSocket消息失败: userId={}", userId, e);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String userId = sessionToUid.get(session.getId());
        log.error("WebSocket传输错误: userId={}, sessionId={}", userId, session.getId(), exception);
        closeConnection(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) {
        String userId = sessionToUid.get(session.getId());
        log.info("WebSocket连接已关闭: userId={}, sessionId={}, status={}",
                userId, session.getId(), closeStatus);
        closeConnection(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 添加WebSocket连接到管理器
     */
    private void addConnection(WebSocketSession session, String userId) {
        // 如果用户已有连接，先关闭旧连接（实现单点登录）
        WebSocketSession existingSession = uidToSession.get(userId);
        if (existingSession != null && existingSession.isOpen()) {
            try {
                existingSession.close();
                log.info("关闭用户的旧连接: userId={}", userId);
            } catch (IOException e) {
                log.error("关闭旧连接失败: userId={}", userId, e);
            }
        }

        // 建立双向映射关系
        uidToSession.put(userId, session);
        sessionToUid.put(session.getId(), userId);
    }

    /**
     * 关闭WebSocket连接并清理相关资源
     */
    private void closeConnection(WebSocketSession session) {
        String userId = sessionToUid.remove(session.getId());
        if (userId != null) {
            uidToSession.remove(userId);
            log.info("清理连接映射: userId={}, sessionId={}", userId, session.getId());
        }

        if (session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.error("关闭WebSocket会话失败", e);
            }
        }
    }

    /**
     * 发送消息到指定的WebSocket会话
     */
    private void sendMessage(WebSocketSession session, ChatMessage message) {
        if (!session.isOpen()) {
            log.warn("尝试向已关闭的会话发送消息: sessionId={}", session.getId());
            return;
        }

        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
        } catch (IOException e) {
            log.error("发送WebSocket消息失败: sessionId={}", session.getId(), e);
        }
    }

    /**
     * 根据用户ID列表发送消息
     *
     * @param message 要发送的消息
     * @param userIds 用户ID列表，如果为空则广播给所有在线用户
     */
    private void sendByUserIds(ChatMessage message, String... userIds) {
        if (userIds == null || userIds.length == 0) {
            // 广播给所有在线用户
            uidToSession.values().forEach(session -> sendMessage(session, message));
        } else {
            // 发送给指定的用户
            for (String userId : userIds) {
                WebSocketSession session = uidToSession.get(userId);
                if (session != null) {
                    sendMessage(session, message);
                } else {
                    log.debug("用户不在线: userId={}", userId);
                }
            }
        }
    }

    /**
     * 处理私聊消息
     */
    private void handlePrivateChat(ChatMessage message) {
        // 保存私聊消息到数据库
        try {
            chatService.savePrivateChat(message);
        } catch (Exception e) {
            log.error("保存私聊消息失败", e);
        }

        // 将消息发送给接收者
        if (message.getRecvId() != null && !message.getRecvId().isEmpty()) {
            sendByUserIds(message, message.getRecvId());
            log.info("私聊消息已发送: from={}, to={}", message.getSendId(), message.getRecvId());
        } else {
            log.warn("私聊消息缺少接收者ID");
        }
    }

    /**
     * 处理群聊消息
     *
     * 支持多群聊：根据conversationId从数据库获取群成员列表，只向该群的成员推送消息
     * 自动添加发送者到群成员（如果还不是成员），实现兼容性
     * AI处理需要在用户@AI助手时单独触发（通过HTTP API）
     */
    private void handleGroupChat(ChatMessage message) {
        // 保存群聊消息到数据库
        try {
            chatService.saveGroupChat(message);
        } catch (Exception e) {
            log.error("保存群聊消息失败", e);
        }

        // 获取群聊ID (conversationId)
        String groupId = message.getConversationId();
        if (groupId == null || groupId.isEmpty()) {
            log.warn("群聊消息缺少conversationId，无法广播: from={}", message.getSendId());
            return;
        }

        // 自动将发送者添加到群成员（如果还不是成员）
        // 这样可以兼容前端直接发送群聊消息而不调用API创建群的情况
        String senderId = message.getSendId();
        if (senderId != null && !senderId.isEmpty()) {
            try {
                if (!groupService.isMember(groupId, senderId)) {
                    groupService.addMember(groupId, senderId);
                    log.info("自动添加发送者到群: groupId={}, userId={}", groupId, senderId);
                }
            } catch (Exception e) {
                log.error("自动添加群成员失败: groupId={}, userId={}", groupId, senderId, e);
            }
        }

        // 从数据库获取该群的所有成员ID
        List<String> memberIds = groupService.getGroupMemberIds(groupId);

        if (memberIds.isEmpty()) {
            log.warn("群聊{}没有成员记录，将广播给所有在线用户: groupId={}", groupId, groupId);
            // 如果数据库中没有群成员记录，回退到广播给所有人（兼容旧数据）
            sendByUserIds(message);
        } else {
            // 只向该群的成员发送消息
            sendByUserIds(message, memberIds.toArray(new String[0]));
            log.info("群聊消息已发送给群成员: groupId={}, from={}, memberCount={}, onlineMemberCount={}",
                    groupId, message.getSendId(), memberIds.size(),
                    memberIds.stream().filter(this::isUserOnline).count());
        }
    }

    /**
     * 获取在线用户数量
     */
    public int getOnlineUserCount() {
        return uidToSession.size();
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String userId) {
        return uidToSession.containsKey(userId);
    }
}
