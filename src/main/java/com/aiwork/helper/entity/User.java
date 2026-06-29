package com.aiwork.helper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 用户实体类
 */
@Data
@Document(collection = "user")
public class User {

    @Id
    private String id;

    /**
     * 用户名 (唯一索引)
     */
    @Indexed(unique = true)
    @Field("name")
    private String name;

    /**
     * 密码 (BCrypt加密)
     */
    @Field("Password")
    private String password;

    /**
     * 状态 (0-正常，1-禁用)
     */
    @Field("status")
    private Integer status;

    /**
     * 是否为管理员
     */
    @Field("isAdmin")
    private Boolean isAdmin;

    /**
     * 更新时间 (Unix时间戳)
     */
    @Field("updateAt")
    private Long updateAt;

    /**
     * 创建时间 (Unix时间戳)
     */
    @Field("createAt")
    private Long createAt;
}