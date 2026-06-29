package com.aiwork.helper.entity.embedded;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 待办事项操作记录 (嵌入文档)
 * 用于Todo实体中的records字段
 */
@Data
public class TodoRecord {

    /**
     * 操作用户ID
     */
    @Field("userId")
    private String userId;

    /**
     * 操作用户名
     */
    @Field("userName")
    private String userName;

    /**
     * 操作内容
     */
    @Field("content")
    private String content;

    /**
     * 操作相关图片
     */
    @Field("image")
    private String image;

    /**
     * 操作时间
     */
    @Field("createAt")
    private Long createAt;
}