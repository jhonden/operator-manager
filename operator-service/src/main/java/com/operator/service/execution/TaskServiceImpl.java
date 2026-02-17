package com.operator.service.execution;

import com.operator.common.dto.execution.*;
import com.operator.common.dto.UserInfo;
import com.operator.common.enums.*;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.execution.domain.Task;
import com.operator.core.execution.repository.TaskRepository;
import com.operator.core.execution.repository.TaskLogRepository;
import com.operator.core.operator.domain.Operator;
import com.operator.core.pkg.domain.OperatorPackage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task Service Implementation (Stub)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskLogRepository taskLogRepository;

    @Override
    @Transactional
    public TaskResponse createTask(TaskRequest request, Long userId) {
        log.info("Creating task: {} for user: {}", request.getTaskName(), userId);

        Task task = new Task();
        task.setTaskName(request.getTaskName());
        task.setTaskType(request.getTaskType());

        Operator operator = new Operator();
        operator.setId(request.getOperatorId());
        task.setOperator(operator);

        OperatorPackage operatorPackage = new OperatorPackage();
        operatorPackage.setId(request.getPackageId());
        task.setOperatorPackage(operatorPackage);

        task.setOperatorVersionId(request.getOperatorVersionId());
        task.setPackageVersionId(request.getPackageVersionId());
        task.setUserId(userId);
        task.setStatus(TaskStatus.PENDING);
        task.setPriority(request.getPriority());
        task.setInputParameters(request.getInputParameters() != null ?
                request.getInputParameters().toString() : null);
        task.setTimeoutSeconds(request.getTimeoutSeconds());

        task = taskRepository.save(task);

        log.info("Task created with ID: {}", task.getId());
        return mapToResponse(task);
    }

    @Override
    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        return mapToResponse(task);
    }

    @Override
    public PageResponse<TaskResponse> getAllTasks(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Task> tasks;
        if (status != null && !status.isEmpty()) {
            tasks = taskRepository.findByStatus(TaskStatus.valueOf(status), pageable);
        } else {
            tasks = taskRepository.findAll(pageable);
        }

        return PageResponse.of(tasks.map(this::mapToResponse));
    }

    @Override
    public PageResponse<TaskResponse> getTasksByUser(Long userId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Task> tasks;
        if (status != null && !status.isEmpty()) {
            tasks = taskRepository.findByStatusAndUserId(TaskStatus.valueOf(status), userId, pageable);
        } else {
            tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        return PageResponse.of(tasks.map(this::mapToResponse));
    }

    @Override
    public List<TaskLogResponse> getTaskLogs(Long id) {
        return taskLogRepository.findByTaskIdOrderByTimestampAsc(id).stream()
                .map(this::mapLogToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelTask(Long id, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Task does not belong to user");
        }

        if (task.getStatus() != TaskStatus.PENDING && task.getStatus() != TaskStatus.QUEUED) {
            throw new IllegalStateException("Cannot cancel task in status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.CANCELLED);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskResponse retryTask(Long id, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Task does not belong to user");
        }

        if (task.getStatus() != TaskStatus.FAILED && task.getStatus() != TaskStatus.TIMEOUT) {
            throw new IllegalStateException("Can only retry failed or timeout tasks");
        }

        // Create retry task
        Task retryTask = new Task();
        retryTask.setTaskName(task.getTaskName() + " (Retry)");
        retryTask.setTaskType(task.getTaskType());
        retryTask.setOperator(task.getOperator());
        retryTask.setOperatorPackage(task.getOperatorPackage());
        retryTask.setOperatorVersionId(task.getOperatorVersionId());
        retryTask.setPackageVersionId(task.getPackageVersionId());
        retryTask.setUserId(task.getUserId());
        retryTask.setStatus(TaskStatus.PENDING);
        retryTask.setPriority(task.getPriority());
        retryTask.setInputParameters(task.getInputParameters());
        retryTask.setTimeoutSeconds(task.getTimeoutSeconds());
        retryTask.setRetryCount(0);

        retryTask = taskRepository.save(retryTask);

        return mapToResponse(retryTask);
    }

    @Override
    @Transactional
    public void deleteTask(Long id, Long userId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (!task.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Task does not belong to user");
        }

        taskRepository.delete(task);
    }

    @Override
    public TaskStatistics getTaskStatistics(Long userId) {
        long totalTasks = taskRepository.count();
        long pendingTasks = taskRepository.countByStatus(TaskStatus.PENDING);
        long runningTasks = taskRepository.countByStatus(TaskStatus.RUNNING);
        long successTasks = taskRepository.countByStatus(TaskStatus.SUCCESS);
        long failedTasks = taskRepository.countByStatus(TaskStatus.FAILED);

        return new TaskStatistics(totalTasks, pendingTasks, runningTasks, successTasks, failedTasks);
    }

    private TaskResponse mapToResponse(Task task) {
        Long durationSeconds = null;
        if (task.getStartedAt() != null && task.getCompletedAt() != null) {
            durationSeconds = Duration.between(task.getStartedAt(), task.getCompletedAt()).getSeconds();
        } else if (task.getStartedAt() != null) {
            durationSeconds = Duration.between(task.getStartedAt(), LocalDateTime.now()).getSeconds();
        }

        return TaskResponse.builder()
                .id(task.getId())
                .taskName(task.getTaskName())
                .taskType(task.getTaskType())
                .operatorId(task.getOperator() != null ? task.getOperator().getId() : null)
                .operatorName(task.getOperator() != null ? task.getOperator().getName() : null)
                .packageId(task.getOperatorPackage() != null ? task.getOperatorPackage().getId() : null)
                .packageName(task.getOperatorPackage() != null ? task.getOperatorPackage().getName() : null)
                .operatorVersionId(task.getOperatorVersionId())
                .packageVersionId(task.getPackageVersionId())
                .userId(task.getUserId())
                .status(task.getStatus())
                .priority(task.getPriority())
                .progress(task.getProgress())
                .startedAt(task.getStartedAt())
                .completedAt(task.getCompletedAt())
                .errorMessage(task.getErrorMessage())
                .containerId(task.getContainerId())
                .retryCount(task.getRetryCount())
                .maxRetries(task.getMaxRetries())
                .timeoutSeconds(task.getTimeoutSeconds())
                .createdAt(task.getCreatedAt())
                .durationSeconds(durationSeconds)
                .build();
    }

    private TaskLogResponse mapLogToResponse(com.operator.core.execution.domain.TaskLog log) {
        return TaskLogResponse.builder()
                .id(log.getId())
                .taskId(log.getTask().getId())
                .logLevel(log.getLogLevel())
                .message(log.getMessage())
                .timestamp(log.getTimestamp())
                .source(log.getSource())
                .exceptionTrace(log.getExceptionTrace())
                .build();
    }
}
