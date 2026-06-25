package com.aiwork.helper.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket聊天消息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * 会话ID
     * - 群聊：每个群有独立的conversationId（支持多群聊）
     * - 私聊：双方用户ID生成的唯一标识
     */
    private String conversationId;

    /**
     * 接收者用户ID，群聊时为空
     */
    private String recvId;

    /**
     * 发送者用户ID，由服务器从JWT Token中提取
     */
    private String sendId;

    /**
     * 聊天类型：1=群聊，2=私聊
     */
    private Integer chatType;

    /**
     * 消息内容文本
     */
    private String content;

    /**
     * 内容类型：1=文字，2=图片，3=表情包等
     */
    private Integer contentType;
}