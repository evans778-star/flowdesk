package com.aiwork.helper.service;

import com.aiwork.helper.dto.request.FinishTodoRequest;
import com.aiwork.helper.dto.request.TodoListRequest;
import com.aiwork.helper.dto.request.TodoRecordRequest;
import com.aiwork.helper.dto.request.TodoRequest;
import com.aiwork.helper.dto.response.TodoInfoResponse;
import com.aiwork.helper.dto.response.TodoListResponse;

/**
 * Todo workflow service.
 */
public interface TodoService {

    TodoInfoResponse info(String id);

    String create(TodoRequest request);

    void edit(TodoRequest request);

    void delete(String id);

    void finish(FinishTodoRequest request);

    void createRecord(TodoRecordRequest request);

    TodoListResponse list(TodoListRequest request);
}
