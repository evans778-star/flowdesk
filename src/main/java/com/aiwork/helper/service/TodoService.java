package com.aiwork.helper.service;

import com.aiwork.helper.dto.request.FinishTodoRequest;
import com.aiwork.helper.dto.request.TodoListRequest;
import com.aiwork.helper.dto.request.TodoRecordRequest;
import com.aiwork.helper.dto.request.TodoRequest;
import com.aiwork.helper.dto.response.TodoInfoResponse;
import com.aiwork.helper.dto.response.TodoListResponse;

/**
 * 待办事项服务接口
 */
public interface TodoService {

    /**
     * ���取待办详情
     */
    TodoInfoResponse info(String id);

    /**
     * 创建待办
     */
    String create(TodoRequest request);

    /**
     * 编辑待办
     */
    void edit(TodoRequest request);

    /**
     * 删除待办
     */
    void delete(String id);

    /**
     * 完成待办
     */
    void finish(FinishTodoRequest request);

    /**
     * 创建待办操作记录
     */
    void createRecord(TodoRecordRequest request);

    /**
     * 待办列表
     */
    TodoListResponse list(TodoListRequest request);
}
