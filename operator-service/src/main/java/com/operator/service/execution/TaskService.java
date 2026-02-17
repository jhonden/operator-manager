package com.operator.service.execution;

import com.operator.common.dto.execution.*;
import com.operator.common.utils.PageResponse;

import java.util.List;

/**
 * Task Service Interface
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface TaskService {

    /**
     * Create a new execution task
     */
    TaskResponse createTask(TaskRequest request, Long userId);

    /**
     * Get task by ID
     */
    TaskResponse getTaskById(Long id);

    /**
     * Get all tasks (paginated)
     */
    PageResponse<TaskResponse> getAllTasks(String status, int page, int size);

    /**
     * Get tasks by user
     */
    PageResponse<TaskResponse> getTasksByUser(Long userId, String status, int page, int size);

    /**
     * Get task logs
     */
    List<TaskLogResponse> getTaskLogs(Long id);

    /**
     * Cancel task
     */
    void cancelTask(Long id, Long userId);

    /**
     * Retry task
     */
    TaskResponse retryTask(Long id, Long userId);

    /**
     * Delete task
     */
    void deleteTask(Long id, Long userId);

    /**
     * Get task statistics
     */
    TaskStatistics getTaskStatistics(Long userId);

    /**
     * Task statistics DTO
     */
    record TaskStatistics(
        long totalTasks,
        long pendingTasks,
        long runningTasks,
        long successTasks,
        long failedTasks
    ) {}
}
