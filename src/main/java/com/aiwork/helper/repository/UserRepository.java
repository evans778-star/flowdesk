package com.aiwork.helper.repository;

import com.aiwork.helper.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByName(String name);

    /**
     * 查找管理员用户
     */
    Optional<User> findByIsAdminTrue();

    /**
     * 根据用户名列表查找用户
     */
    List<User> findByNameIn(List<String> names);

    /**
     * 根据ID列表查找用户
     */
    List<User> findByIdIn(List<String> ids);

    /**
     * 根据状态查找用户
     */
    List<User> findByStatus(Integer status);
}