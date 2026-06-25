package com.aiwork.helper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 聊天记录实体类
 */
@Data
@Document(collection = "chat_log")
public class ChatLog {

    @Id
    private String id;

    /**
     * 会话ID
     * - 群聊：每个群有独立的conversationId（支���多群聊）
     * - 私聊：双方用户ID生成的唯一标识
     */
    @Indexed
    @Field("conversationId")
    private String conversationId;

    /**
     * 发送者用户ID
     */
    @Field("sendId")
    private String sendId;

    /**
     * 接收者用户ID (群聊时为空)
     */
    @Field("recvId")
    private String recvId;

    /**
     * 聊天类型 (1-群聊，2-私聊)
     */
    @Field("chatType")
    private Integer chatType;

    /**
     * 消息内容
     */
    @Field("msgContent")
    private String msgContent;

    /**
     * 发送时间戳
     */
    @Field("sendTime")
    private Long sendTime;

    /**
     * 更新时间戳
     */
    @Field("updateAt")
    private Long updateAt;

    /**
     * 创建时间戳
     */
    @Field("createAt")
    private Long createAt;
}