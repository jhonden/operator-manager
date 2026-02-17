package com.operator.core.execution.repository;

import com.operator.core.execution.domain.TaskLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TaskLog entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface TaskLogRepository extends JpaRepository<TaskLog, Long> {

    /**
     * Find logs by task
     */
    List<TaskLog> findByTaskIdOrderByTimestampAsc(Long taskId);

    /**
     * Find logs by task and level
     */
    List<TaskLog> findByTaskIdAndLogLevelOrderByTimestampAsc(Long taskId, com.operator.common.enums.LogLevel logLevel);

    /**
     * Delete logs by task
     */
    void deleteByTaskId(Long taskId);

    /**
     * Count logs by task
     */
    long countByTaskId(Long taskId);
}
