package com.operator.core.execution.repository;

import com.operator.common.enums.TaskStatus;
import com.operator.core.execution.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Task entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find tasks by status
     */
    List<Task> findByStatusOrderByCreatedAtDesc(TaskStatus status);

    /**
     * Find tasks by user
     */
    Page<Task> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * Find tasks by status and user
     */
    List<Task> findByStatusAndUserIdOrderByCreatedAtDesc(TaskStatus status, Long userId);

    /**
     * Find pending/queued tasks ordered by priority
     */
    @Query("SELECT t FROM Task t WHERE t.status IN ('PENDING', 'QUEUED') ORDER BY t.priority DESC, t.createdAt ASC")
    List<Task> findPendingTasksOrderedByPriority();

    /**
     * Find running tasks
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Find tasks by status with pagination
     */
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    /**
     * Find tasks by status and user with pagination
     */
    Page<Task> findByStatusAndUserId(TaskStatus status, Long userId, Pageable pageable);

    /**
     * Count tasks by status
     */
    long countByStatus(TaskStatus status);

    /**
     * Find tasks by operator
     */
    List<Task> findByOperatorIdOrderByCreatedAtDesc(Long operatorId);

    /**
     * Find tasks by package
     */
    List<Task> findByOperatorPackageIdOrderByCreatedAtDesc(Long packageId);

    /**
     * Find timeout tasks
     * Note: Simplified query for H2 compatibility
     * Returns all RUNNING tasks that should be checked for timeout in application code
     */
    @Query("SELECT t FROM Task t WHERE t.status = 'RUNNING' AND t.startedAt IS NOT NULL ORDER BY t.startedAt ASC")
    List<Task> findRunningTasksForTimeoutCheck();
}
