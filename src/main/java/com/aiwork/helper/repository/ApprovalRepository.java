package com.aiwork.helper.repository;

import com.aiwork.helper.entity.Approval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 审批数据访问接口
 */
@Repository
public interface ApprovalRepository extends MongoRepository<Approval, String> {

    /**
     * 根据申请人ID查找审批列表 (我提交的)
     */
    Page<Approval> findByUserId(String userId, Pageable pageable);

    /**
     * 根据当前审批人ID和状态查找审批列表 (待我审批的)
     */
    Page<Approval> findByApprovalIdAndStatus(String approvalId, Integer status, Pageable pageable);

    /**
     * 根据审批编号查找
     */
    Approval findByNo(String no);

    /**
     * 根据申请人和审批类型查找
     */
    List<Approval> findByUserIdAndType(String userId, Integer type);

    /**
     * 根据审批状态查找
     */
    List<Approval> findByStatus(Integer status);

    /**
     * 根据参与人ID查找审批 (参与人包括申请人、审批人、抄送人)
     */
    @Query("{'participation': ?0}")
    List<Approval> findByParticipation(String userId);

    /**
     * 根据参与人ID分页查找审批 (与我相关的)
     */
    @Query("{'participation': ?0}")
    Page<Approval> findByParticipationContaining(String userId, Pageable pageable);

    /**
     * 根据参与人ID和审批类型分页查找审批
     */
    @Query("{'participation': ?0, 'type': ?1}")
    Page<Approval> findByParticipationContainingAndType(String userId, Integer type, Pageable pageable);

    /**
     * 统计用户的审批数量
     */
    Long countByUserId(String userId);

    /**
     * ��计待审批数量
     */
    Long countByApprovalIdAndStatus(String approvalId, Integer status);
}