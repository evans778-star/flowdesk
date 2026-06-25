package com.aiwork.helper.entity.embedded;

import com.aiwork.helper.entity.enums.TodoStatus;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * 用户待办关联 (嵌入文档)
 * 用于Todo实体中的executes字段
 */
@Data
public class UserTodo {

    /**
     * ID (唯一标识)
     */
    @Field("id")
    private String id;

    /**
     * 用户ID
     */
    @Field("userId")
    private String userId;

    /**
     * 用户名
     */
    @Field("userName")
    private String userName;

    /**
     * 待办ID
     */
    @Field("todoId")
    private String todoId;

    /**
     * 待办状态
     */
    @Field("todoStatus")
    private Integer todoStatus;

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