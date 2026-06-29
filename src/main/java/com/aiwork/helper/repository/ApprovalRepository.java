package com.aiwork.helper.repository;

import com.aiwork.helper.entity.Approval;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * MongoDB repository for approval workflows.
 */
@Repository
public interface ApprovalRepository extends MongoRepository<Approval, String> {

    Page<Approval> findByUserId(String userId, Pageable pageable);

    Page<Approval> findByApprovalIdAndStatus(String approvalId, Integer status, Pageable pageable);

    Approval findByNo(String no);

    List<Approval> findByUserIdAndType(String userId, Integer type);

    List<Approval> findByStatus(Integer status);

    @Query("{'participation': ?0}")
    List<Approval> findByParticipation(String userId);

    @Query("{'participation': ?0}")
    Page<Approval> findByParticipationContaining(String userId, Pageable pageable);

    @Query("{'participation': ?0, 'type': ?1}")
    Page<Approval> findByParticipationContainingAndType(String userId, Integer type, Pageable pageable);

    Long countByUserId(String userId);

    Long countByApprovalIdAndStatus(String approvalId, Integer status);
}
