package com.aiwork.helper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Persisted chat message metadata.
 */
@Data
@Document(collection = "chat_log")
public class ChatLog {

    @Id
    private String id;

    /**
     * Conversation identifier. Group chats use a group-specific id; private chats use a stable participant id.
     */
    @Indexed
    @Field("conversationId")
    private String conversationId;

    @Field("sendId")
    private String sendId;

    @Field("recvId")
    private String recvId;

    @Field("chatType")
    private Integer chatType;

    @Field("msgContent")
    private String msgContent;

    @Field("sendTime")
    private Long sendTime;

    @Field("updateAt")
    private Long updateAt;

    @Field("createAt")
    private Long createAt;
}
