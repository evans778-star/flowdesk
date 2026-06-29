package com.aiwork.helper.entity;

import com.aiwork.helper.entity.embedded.TodoRecord;
import com.aiwork.helper.entity.embedded.UserTodo;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * 待办事项实体类
 */
@Data
@Document(collection = "todo")
public class Todo {

    @Id
    private String id;

    /**
     * 创建人ID
     */
    @Field("creatorId")
    private String creatorId;

    /**
     * 待办标题
     */
    @Field("title")
    private String title;

    /**
     * 截止时间 (Unix时间戳)
     */
    @Field("deadlineAt")
    private Long deadlineAt;

    /**
     * 待办描述
     */
    @Field("desc")
    private String desc;

    /**
     * 操作记录列表
     */
    @Field("records")
    private List<TodoRecord> records;

    /**
     * 执行人列表
     */
    @Field("executes")
    private List<UserTodo> executes;

    /**
     * 待办状态 (1-待处理，2-进行中，3-已完成，4-已取消，5-已超时)
     */
    @Field("todo_status")
    private Integer todoStatus;

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