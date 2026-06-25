/**
 * @author: 公众号：IT杨秀才
 * @doc:后端，AI知识进阶，后端面试场景题大全：https://golangstar.cn/
 */
package com.aiwork.helper.service.impl;

import com.aiwork.helper.dto.request.FinishTodoRequest;
import com.aiwork.helper.dto.request.TodoListRequest;
import com.aiwork.helper.dto.request.TodoRecordRequest;
import com.aiwork.helper.dto.request.TodoRequest;
import com.aiwork.helper.dto.response.*;
import com.aiwork.helper.entity.Todo;
import com.aiwork.helper.entity.User;
import com.aiwork.helper.entity.embedded.TodoRecord;
import com.aiwork.helper.entity.embedded.UserTodo;
import com.aiwork.helper.entity.enums.TodoStatus;
import com.aiwork.helper.exception.BusinessException;
import com.aiwork.helper.repository.TodoRepository;
import com.aiwork.helper.repository.UserRepository;
import com.aiwork.helper.security.SecurityUtils;
import com.aiwork.helper.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * author:  公众号：IT杨秀才
 * 后端，AI知识进阶，后端面试场景题大全：https://golangstar.cn/
 * 待办事项服务实现
 * 对应Go版本: internal/logic/todo.go
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Override
    public TodoInfoResponse info(String id) {
        // 查询待办事项
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("待办事项不存在"));

        // 收集所有用户ID（执行人 + 创建人）
        Set<String> userIds = new HashSet<>();
        userIds.add(todo.getCreatorId());
        if (todo.getExecutes() != null) {
            todo.getExecutes().forEach(exec -> userIds.add(exec.getUserId()));
        }

        // 批量查询用户信息
        List<User> users = userRepository.findByIdIn(new ArrayList<>(userIds));
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 检查是否超时
        long currentTime = System.currentTimeMillis() / 1000;
        Integer todoStatus = todo.getTodoStatus();
        if (currentTime > todo.getDeadlineAt()) {
            todoStatus = TodoStatus.TIMEOUT.getValue();
        }

        // 获取创建人信息
        User creator = userMap.get(todo.getCreatorId());
        if (creator == null) {
            throw new BusinessException("用户信息查询失败");
        }

        // 构建执行人详细信息列表
        List<UserTodoResponse> executeIds = new ArrayList<>();
        if (todo.getExecutes() != null) {
            for (UserTodo exec : todo.getExecutes()) {
                User user = userMap.get(exec.getUserId());
                String userName = user != null ? user.getName() : "";

                executeIds.add(UserTodoResponse.builder()
                        .id(exec.getId())
                        .userId(exec.getUserId())
                        .userName(userName)
                        .todoId(id)
                        .todoStatus(exec.getTodoStatus())
                        .build());
            }
        }

        // 构建操作记录列表
        List<TodoRecordResponse> records = new ArrayList<>();
        if (todo.getRecords() != null) {
            for (TodoRecord record : todo.getRecords()) {
                User recordUser = userMap.get(record.getUserId());
                String userName = recordUser != null ? recordUser.getName() : "";

                records.add(TodoRecordResponse.builder()
                        .todoId(id)
                        .userId(record.getUserId())
                        .userName(userName)
                        .content(record.getContent())
                        .image(record.getImage())
                        .createAt(record.getCreateAt())
                        .build());
            }
        }

        return TodoInfoResponse.builder()
                .id(todo.getId())
                .creatorId(todo.getCreatorId())
                .creatorName(creator.getName())
                .title(todo.getTitle())
                .deadlineAt(todo.getDeadlineAt())
                .desc(todo.getDesc())
                .status(todoStatus)
                .todoStatus(todoStatus)
                .executeIds(executeIds)
                .records(records)
                .build();
    }

    @Override
    public String create(TodoRequest request) {
        log.info("create todo: {}", request);

        // 获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 创建执行人列表
        List<UserTodo> executes = new ArrayList<>();
        if (request.getExecuteIds() != null && !request.getExecuteIds().isEmpty()) {
            for (String executeId : request.getExecuteIds()) {
                UserTodo userTodo = new UserTodo();
                userTodo.setId(UUID.randomUUID().toString());
                userTodo.setUserId(executeId);
                userTodo.setTodoStatus(TodoStatus.PENDING.getValue());
                executes.add(userTodo);
            }
        } else {
            // 如果没有指定执行人，默认为创建人
            UserTodo userTodo = new UserTodo();
            userTodo.setId(UUID.randomUUID().toString());
            userTodo.setUserId(currentUserId);
            userTodo.setTodoStatus(TodoStatus.PENDING.getValue());
            executes.add(userTodo);
        }

        // 创建待办记录列表
        List<TodoRecord> records = new ArrayList<>();
        if (request.getRecords() != null) {
            for (TodoRecordResponse recordReq : request.getRecords()) {
                TodoRecord record = new TodoRecord();
                record.setUserId(recordReq.getUserId());
                record.setContent(recordReq.getContent());
                record.setImage(recordReq.getImage());
                record.setCreateAt(System.currentTimeMillis() / 1000);
                records.add(record);
            }
        }

        // 创建待办事项
        long currentTime = System.currentTimeMillis() / 1000;
        Todo todo = new Todo();
        todo.setCreatorId(currentUserId);
        todo.setTitle(request.getTitle());
        todo.setDeadlineAt(request.getDeadlineAt());
        todo.setDesc(request.getDesc());
        todo.setRecords(records);
        todo.setExecutes(executes);
        todo.setTodoStatus(TodoStatus.PENDING.getValue());
        todo.setCreateAt(currentTime);
        todo.setUpdateAt(currentTime);

        Todo saved = todoRepository.save(todo);
        log.info("create todo success, id: {}", saved.getId());

        return saved.getId();
    }

    @Override
    public void edit(TodoRequest request) {
        if (request.getId() == null || request.getId().isEmpty()) {
            throw new BusinessException("待办事项ID不能为空");
        }

        // 查询原有待办事项
        Todo todo = todoRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("待办事项不存在"));

        // 更新字段
        todo.setTitle(request.getTitle());
        todo.setDesc(request.getDesc());
        todo.setDeadlineAt(request.getDeadlineAt());
        if (request.getStatus() != null) {
            todo.setTodoStatus(request.getStatus());
        }
        todo.setUpdateAt(System.currentTimeMillis() / 1000);

        // 保存更新
        todoRepository.save(todo);
        log.info("edit todo success, id: {}", request.getId());
    }

    @Override
    public void delete(String id) {
        // 获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 查询待办事项
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("待办事项不存在"));

        // 只有创建人可以删除
        if (!currentUserId.equals(todo.getCreatorId())) {
            throw new BusinessException("您不能删除该待办事项");
        }

        // 删除待办事项
        todoRepository.deleteById(id);
        log.info("delete todo success, id: {}", id);
    }

    @Override
    public void finish(FinishTodoRequest request) {
        // 查询待办事项
        Todo todo = todoRepository.findById(request.getTodoId())
                .orElseThrow(() -> new BusinessException("待办事项不存在"));

        // 标记指定用户的待办状态为完成
        boolean userFound = false;
        if (todo.getExecutes() != null) {
            for (UserTodo exec : todo.getExecutes()) {
                if (exec.getUserId().equals(request.getUserId())) {
                    exec.setTodoStatus(TodoStatus.FINISHED.getValue());
                    userFound = true;
                    break;
                }
            }
        }

        if (!userFound) {
            throw new BusinessException("用户不在待办执行人列表中");
        }

        // 检查是否所有执行人都完成了
        boolean isAllFinished = true;
        if (todo.getExecutes() != null) {
            for (UserTodo exec : todo.getExecutes()) {
                if (!exec.getTodoStatus().equals(TodoStatus.FINISHED.getValue())) {
                    isAllFinished = false;
                    break;
                }
            }
        }

        // 如果所有执行人都完成，更新整体状态为完成
        if (isAllFinished) {
            todo.setTodoStatus(TodoStatus.FINISHED.getValue());
        }

        todo.setUpdateAt(System.currentTimeMillis() / 1000);
        todoRepository.save(todo);
        log.info("finish todo success, todoId: {}, userId: {}, allFinished: {}",
                request.getTodoId(), request.getUserId(), isAllFinished);
    }

    @Override
    public void createRecord(TodoRecordRequest request) {
        // 获取当前用户ID，自动设置为记录创建人
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 查询待办事项
        Todo todo = todoRepository.findById(request.getTodoId())
                .orElseThrow(() -> new BusinessException("待办事项不存在"));

        // 创建新记录
        TodoRecord record = new TodoRecord();
        record.setUserId(currentUserId);
        record.setContent(request.getContent());
        record.setImage(request.getImage());
        record.setCreateAt(System.currentTimeMillis() / 1000);

        // 添加记录到待办事项
        if (todo.getRecords() == null) {
            todo.setRecords(new ArrayList<>());
        }
        todo.getRecords().add(record);
        todo.setUpdateAt(System.currentTimeMillis() / 1000);

        // 保存更新
        todoRepository.save(todo);
        log.info("create todo record success, todoId: {}, userId: {}", request.getTodoId(), currentUserId);
    }

    @Override
    public TodoListResponse list(TodoListRequest request) {
        log.info("todo list request: {}", request);

        // 获取当前用户ID
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("用户未登录");
        }

        // 构建分页参数
        int page = request.getPage() != null ? request.getPage() - 1 : 0;
        int pageSize = request.getCount() != null ? request.getCount() : 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));

        // 查询待办列表
        Page<Todo> todoPage;
        if (request.getStartTime() != null && request.getEndTime() != null) {
            todoPage = todoRepository.findByExecuteUserIdAndTimeRange(
                    currentUserId, request.getStartTime(), request.getEndTime(), pageable);
        } else {
            todoPage = todoRepository.findByExecuteUserId(currentUserId, pageable);
        }

        List<Todo> todos = todoPage.getContent();
        long count = todoPage.getTotalElements();

        // 收集所有用户ID（包括创建人和执行人）
        Set<String> userIds = new HashSet<>();
        for (Todo todo : todos) {
            userIds.add(todo.getCreatorId());
            if (todo.getExecutes() != null) {
                todo.getExecutes().forEach(exec -> userIds.add(exec.getUserId()));
            }
        }

        // 批量查询用户信息
        List<User> users = userRepository.findByIdIn(new ArrayList<>(userIds));
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        // 构建响应列表
        long currentTime = System.currentTimeMillis() / 1000;
        List<TodoResponse> data = new ArrayList<>();
        for (Todo todo : todos) {
            // 检查是否超时
            Integer todoStatus = todo.getTodoStatus();
            if (currentTime > todo.getDeadlineAt()) {
                todoStatus = TodoStatus.TIMEOUT.getValue();
            }

            // 获取创建人名称
            User creator = userMap.get(todo.getCreatorId());
            String creatorName = creator != null ? creator.getName() : "";

            // 获取执行人名称列表
            List<String> executeNames = new ArrayList<>();
            if (todo.getExecutes() != null) {
                for (UserTodo exec : todo.getExecutes()) {
                    User execUser = userMap.get(exec.getUserId());
                    if (execUser != null) {
                        executeNames.add(execUser.getName());
                    }
                }
            }

            TodoResponse todoResponse = TodoResponse.builder()
                    .id(todo.getId())
                    .creatorId(todo.getCreatorId())
                    .creatorName(creatorName)
                    .title(todo.getTitle())
                    .deadlineAt(todo.getDeadlineAt())
                    .desc(todo.getDesc())
                    .status(todoStatus)        // 添加 status 字段，与 Go 版本保持一致
                    .todoStatus(todoStatus)
                    .executeIds(executeNames)
                    .createAt(todo.getCreateAt())
                    .updateAt(todo.getUpdateAt())
                    .build();

            data.add(todoResponse);
        }

        return TodoListResponse.builder()
                .count(count)
                .data(data)
                .build();
    }
}
