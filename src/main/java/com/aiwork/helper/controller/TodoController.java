package com.aiwork.helper.controller;

import com.aiwork.helper.common.Result;
import com.aiwork.helper.dto.request.FinishTodoRequest;
import com.aiwork.helper.dto.request.TodoListRequest;
import com.aiwork.helper.dto.request.TodoRecordRequest;
import com.aiwork.helper.dto.request.TodoRequest;
import com.aiwork.helper.dto.response.TodoInfoResponse;
import com.aiwork.helper.dto.response.TodoListResponse;
import com.aiwork.helper.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 待办事项控制器
 */
@RestController
@RequestMapping("/v1/todo")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /**
     * 获取待办详情
     */
    @GetMapping("/{id}")
    public Result<TodoInfoResponse> info(@PathVariable String id) {
        TodoInfoResponse response = todoService.info(id);
        return Result.ok(response);
    }

    /**
     * 创建待办
     */
    @PostMapping
    public Result<String> create(@Valid @RequestBody TodoRequest request) {
        String id = todoService.create(request);
        return Result.ok(id);
    }

    /**
     * 编辑待办
     */
    @PutMapping
    public Result<Void> edit(@Valid @RequestBody TodoRequest request) {
        todoService.edit(request);
        return Result.ok();
    }

    /**
     * 删除待办
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        todoService.delete(id);
        return Result.ok();
    }

    /**
     * 完成待办
     */
    @PostMapping("/finish")
    public Result<Void> finish(@Valid @RequestBody FinishTodoRequest request) {
        todoService.finish(request);
        return Result.ok();
    }

    /**
     * 创建操作记录
     */
    @PostMapping("/record")
    public Result<Void> createRecord(@Valid @RequestBody TodoRecordRequest request) {
        todoService.createRecord(request);
        return Result.ok();
    }

    /**
     * 获取待办列表
     */
    @GetMapping("/list")
    public Result<TodoListResponse> list(@Valid TodoListRequest request) {
        TodoListResponse response = todoService.list(request);
        return Result.ok(response);
    }
}
