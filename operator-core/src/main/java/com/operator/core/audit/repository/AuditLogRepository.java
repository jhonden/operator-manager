package com.operator.core.audit.repository;

import com.operator.core.audit.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for AuditLog entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user
     */
    Page<AuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find audit logs by action
     */
    List<AuditLog> findByActionOrderByCreatedAtDesc(String action);

    /**
     * Find audit logs by entity type and entity id
     */
    List<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Long entityId);

    /**
     * Find audit logs by date range
     */
    @Query("SELECT a FROM AuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<AuditLog> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    /**
     * Find audit logs by operation type
     */
    List<AuditLog> findByOperationTypeOrderByCreatedAtDesc(AuditLog.OperationType operationType);

    /**
     * Find failed operations
     */
    List<AuditLog> findBySuccessFalseOrderByCreatedAtDesc();

    /**
     * Count audit logs by user
     */
    long countByUserId(Long userId);

    /**
     * Delete old audit logs (cleanup)
     */
    void deleteByCreatedAtBefore(LocalDateTime date);
}
