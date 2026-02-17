package com.operator.infrastructure.scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.operator.common.enums.TaskStatus;
import com.operator.core.execution.domain.Task;
import com.operator.core.execution.domain.TaskLog;
import com.operator.core.execution.repository.TaskRepository;
import com.operator.infrastructure.scheduler.model.TaskExecutionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Task Scheduler
 *
 * Polls for pending tasks and dispatches them to executors
 * Uses Redis List as a task queue
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class TaskScheduler {

    private static final Logger log = LoggerFactory.getLogger(TaskScheduler.class);

    private final TaskRepository taskRepository;
    private final RedisQueueService redisQueueService;
    private final TaskExecutorService taskExecutorService;

    /**
     * Scheduled task poller - runs every second
     * Fetches pending tasks and queues them
     */
    @Scheduled(fixedDelay = 1000)
    public void pollAndQueueTasks() {
        try {
            // Get pending tasks ordered by priority
            List<Task> pendingTasks = taskRepository.findPendingTasksOrderedByPriority();

            if (!pendingTasks.isEmpty()) {
                log.debug("Found {} pending tasks", pendingTasks.size());

                for (Task task : pendingTasks) {
                    // Update task status to QUEUED
                    task.setStatus(TaskStatus.QUEUED);
                    taskRepository.save(task);

                    // Add to Redis queue
                    redisQueueService.enqueueTask(task.getId(), task.getPriority());

                    log.info("Task queued: {} (priority: {})", task.getId(), task.getPriority());
                }
            }

            // Check for timeout tasks
            checkTimeoutTasks();

        } catch (Exception e) {
            log.error("Error polling tasks", e);
        }
    }

    /**
     * Process tasks from queue
     */
    @Scheduled(fixedDelay = 500)
    public void processQueuedTasks() {
        try {
            // Dequeue task from Redis
            Long taskId = redisQueueService.dequeueTask();

            if (taskId != null) {
                log.info("Dequeued task: {}", taskId);

                // Load task
                Task task = taskRepository.findById(taskId).orElse(null);
                if (task == null) {
                    log.warn("Task not found: {}", taskId);
                    return;
                }

                // Execute task
                executeTask(task);
            }

        } catch (Exception e) {
            log.error("Error processing queued tasks", e);
        }
    }

    /**
     * Check for timeout tasks
     */
    private void checkTimeoutTasks() {
        try {
            List<Task> runningTasks = taskRepository.findRunningTasksForTimeoutCheck();

            for (Task task : runningTasks) {
                // Check if task has timed out
                if (task.getStartedAt() != null && task.getTimeoutSeconds() != null) {
                    long elapsedSeconds = java.time.Duration.between(task.getStartedAt(), LocalDateTime.now()).getSeconds();

                    if (elapsedSeconds > task.getTimeoutSeconds()) {
                        log.warn("Task timeout detected: {}", task.getId());

                        task.setStatus(TaskStatus.TIMEOUT);
                        task.setErrorMessage("Task execution timeout");
                        task.setCompletedAt(LocalDateTime.now());
                        taskRepository.save(task);

                        // Log timeout
                        logTaskMessage(task, com.operator.common.enums.LogLevel.ERROR, "Task execution timeout");

                        // Clean up resources
                        taskExecutorService.cleanupTask(task);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error checking timeout tasks", e);
        }
    }

    /**
     * Execute task
     */
    @Transactional
    public void executeTask(Task task) {
        try {
            log.info("Executing task: {}", task.getId());

            // Update status to RUNNING
            task.setStatus(TaskStatus.RUNNING);
            task.setStartedAt(LocalDateTime.now());
            task.setProgress(0);
            taskRepository.save(task);

            // Log start
            logTaskMessage(task, com.operator.common.enums.LogLevel.INFO, "Task execution started");

            // Execute based on task type
            TaskExecutionResult result = taskExecutorService.executeTask(task);

            // Update task with result
            task.setProgress(100);
            task.setOutputData(result.getOutputData());
            task.setCompletedAt(LocalDateTime.now());

            if (result.isSuccess()) {
                task.setStatus(TaskStatus.SUCCESS);
                logTaskMessage(task, com.operator.common.enums.LogLevel.INFO, "Task executed successfully");
                log.info("Task executed successfully: {}", task.getId());
            } else {
                task.setStatus(TaskStatus.FAILED);
                task.setErrorMessage(result.getErrorMessage());
                logTaskMessage(task, com.operator.common.enums.LogLevel.ERROR, "Task failed: " + result.getErrorMessage());
                log.error("Task execution failed: {} - {}", task.getId(), result.getErrorMessage());

                // Check if should retry
                if (task.getRetryCount() < task.getMaxRetries()) {
                    task.setRetryCount(task.getRetryCount() + 1);
                    task.setStatus(TaskStatus.PENDING);
                    log.info("Task will be retried (attempt {}): {}", task.getRetryCount(), task.getId());
                }
            }

            taskRepository.save(task);

        } catch (Exception e) {
            log.error("Error executing task: {}", task.getId(), e);

            task.setStatus(TaskStatus.FAILED);
            task.setErrorMessage("Execution error: " + e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);

            logTaskMessage(task, com.operator.common.enums.LogLevel.ERROR, "Task execution error: " + e.getMessage());
        }
    }

    /**
     * Log task message
     */
    private void logTaskMessage(Task task, com.operator.common.enums.LogLevel level, String message) {
        try {
            TaskLog taskLog = TaskLog.builder()
                    .task(task)
                    .logLevel(level)
                    .message(message)
                    .timestamp(LocalDateTime.now())
                    .source("TaskScheduler")
                    .build();

            // TODO: Save to database or send via WebSocket
            // taskLogRepository.save(taskLog);

        } catch (Exception e) {
            log.error("Error logging task message", e);
        }
    }
}
