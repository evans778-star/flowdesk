package com.aiwork.helper.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 部门实体类
 */
@Data
@Document(collection = "department")
public class Department {

    @Id
    private String id;

    /**
     * 部门名称
     */
    @Field("name")
    private String name;

    /**
     * 父部门ID
     */
    @Field("parentId")
    private String parentId;

    /**
     * 父部门路径 (用冒号分隔的ID链)
     */
    @Field("parentPath")
    private String parentPath;

    /**
     * 部门层级
     */
    @Field("level")
    private Integer level;

    /**
     * 部门负责人ID
     */
    @Field("leaderId")
    private String leaderId;

    /**
     * 部门负责人姓名
     */
    @Field("leader")
    private String leader;

    /**
     * 部门人数
     */
    @Field("count")
    private Long count;

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