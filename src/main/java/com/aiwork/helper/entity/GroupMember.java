package com.aiwork.helper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 群聊成员关联实体类
 * 用于管理群聊的成员关系,支持多个群聊的成员管理
 */
@Data
@Document(collection = "group_member")
@CompoundIndex(name = "group_user_idx", def = "{'groupId': 1, 'userId': 1}", unique = true)
public class GroupMember {

    @Id
    private String id;

    /**
     * 群ID (conversationId)
     */
    @Indexed
    @Field("groupId")
    private String groupId;

    /**
     * 用户ID
     */
    @Indexed
    @Field("userId")
    private String userId;

    /**
     * 更新时间
     */
    @Field("updateAt")
    private Long updateAt;

    /**
     * 创建时间
     */
    @Field("createAt")
    private Long createAt;
}
