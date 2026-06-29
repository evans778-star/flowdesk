package com.aiwork.helper.service.impl;

import com.aiwork.helper.dto.request.FinishTodoRequest;
import com.aiwork.helper.dto.request.TodoListRequest;
import com.aiwork.helper.dto.request.TodoRecordRequest;
import com.aiwork.helper.dto.request.TodoRequest;
import com.aiwork.helper.dto.response.TodoInfoResponse;
import com.aiwork.helper.dto.response.TodoListResponse;
import com.aiwork.helper.dto.response.TodoRecordResponse;
import com.aiwork.helper.dto.response.TodoResponse;
import com.aiwork.helper.dto.response.UserTodoResponse;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implements todo creation, assignment, completion, records, and list views.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Override
    public TodoInfoResponse info(String id) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Todo does not exist"));

        Set<String> userIds = new HashSet<>();
        userIds.add(todo.getCreatorId());
        if (todo.getExecutes() != null) {
            todo.getExecutes().forEach(exec -> userIds.add(exec.getUserId()));
        }

        List<User> users = userRepository.findByIdIn(new ArrayList<>(userIds));
        Map<String, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        long currentTime = System.currentTimeMillis() / 1000;
        Integer todoStatus = todo.getTodoStatus();
        if (currentTime > todo.getDeadlineAt()) {
            todoStatus = TodoStatus.TIMEOUT.getValue();
        }

        User creator = userMap.get(todo.getCreatorId());
        if (creator == null) {
            throw new BusinessException("User information lookup failed");
        }

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

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("User is not logged in");
        }

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
            UserTodo userTodo = new UserTodo();
            userTodo.setId(UUID.randomUUID().toString());
            userTodo.setUserId(currentUserId);
            userTodo.setTodoStatus(TodoStatus.PENDING.getValue());
            executes.add(userTodo);
        }

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
            throw new BusinessException("Todo id is required");
        }

        Todo todo = todoRepository.findById(request.getId())
                .orElseThrow(() -> new BusinessException("Todo does not exist"));

        todo.setTitle(request.getTitle());
        todo.setDesc(request.getDesc());
        todo.setDeadlineAt(request.getDeadlineAt());
        if (request.getStatus() != null) {
            todo.setTodoStatus(request.getStatus());
        }
        todo.setUpdateAt(System.currentTimeMillis() / 1000);

        todoRepository.save(todo);
        log.info("edit todo success, id: {}", request.getId());
    }

    @Override
    public void delete(String id) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("User is not logged in");
        }

        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Todo does not exist"));

        if (!currentUserId.equals(todo.getCreatorId())) {
            throw new BusinessException("Only the creator can delete this todo");
        }

        todoRepository.deleteById(id);
        log.info("delete todo success, id: {}", id);
    }

    @Override
    public void finish(FinishTodoRequest request) {
        Todo todo = todoRepository.findById(request.getTodoId())
                .orElseThrow(() -> new BusinessException("Todo does not exist"));

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
            throw new BusinessException("User is not assigned to this todo");
        }

        boolean isAllFinished = true;
        if (todo.getExecutes() != null) {
            for (UserTodo exec : todo.getExecutes()) {
                if (!exec.getTodoStatus().equals(TodoStatus.FINISHED.getValue())) {
                    isAllFinished = false;
                    break;
                }
            }
        }

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
        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("User is not logged in");
        }

        Todo todo = todoRepository.findById(request.getTodoId())
                .orElseThrow(() -> new BusinessException("Todo does not exist"));

        TodoRecord record = new TodoRecord();
        record.setUserId(currentUserId);
        record.setContent(request.getContent());
        record.setImage(request.getImage());
        record.setCreateAt(System.currentTimeMillis() / 1000);

        if (todo.getRecords() == null) {
            todo.setRecords(new ArrayList<>());
        }
        todo.getRecords().add(record);
        todo.setUpdateAt(System.currentTimeMillis() / 1000);

        todoRepository.save(todo);
        log.info("create todo record success, todoId: {}, userId: {}", request.getTodoId(), currentUserId);
    }

    @Override
    public TodoListResponse list(TodoListRequest request) {
        log.info("todo list request: {}", request);

        String currentUserId = SecurityUtils.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("User is not logged in");
        }

        int page = request.getPage() != null ? request.getPage() - 1 : 0;
        int pageSize = request.getCount() != null ? request.getCount() : 10;
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createAt"));

        Page<Todo> todoPage;
        if (request.getStartTime() != null && request.getEndTime() != null) {
            todoPage = todoRepository.findByExecuteUserIdAndTimeRange(
                    currentUserId, request.getStartTime(), request.getEndTime(), pageable);
        } else {
            todoPage = todoRepository.findByExecuteUserId(currentUserId, pageable);
        }

        List<Todo> todos = todoPage.getContent();
        long count = todoPage.getTotalElements();

        Set<String> userIds = new HashSet<>();
        for (Todo todo : todos) {
            userIds.add(todo.getCreatorId());
            if (todo.getExecutes() != null) {
                todo.getExecutes().forEach(exec -> userIds.add(exec.getUserId()));
            }
        }

        Map<String, User> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            List<User> users = userRepository.findByIdIn(new ArrayList<>(userIds));
            userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        }

        long currentTime = System.currentTimeMillis() / 1000;
        List<TodoResponse> data = new ArrayList<>();
        for (Todo todo : todos) {
            Integer todoStatus = todo.getTodoStatus();
            if (currentTime > todo.getDeadlineAt()) {
                todoStatus = TodoStatus.TIMEOUT.getValue();
            }

            User creator = userMap.get(todo.getCreatorId());
            String creatorName = creator != null ? creator.getName() : "";

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
                    .status(todoStatus)
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
