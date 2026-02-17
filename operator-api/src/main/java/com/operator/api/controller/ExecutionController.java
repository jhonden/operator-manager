package com.operator.api.controller;

import com.operator.infrastructure.security.UserPrincipal;
import com.operator.common.utils.ApiResponse;
import com.operator.common.utils.PageResponse;
import com.operator.common.dto.execution.TaskRequest;
import com.operator.common.dto.execution.TaskResponse;
import com.operator.common.dto.execution.TaskLogResponse;
import com.operator.service.execution.TaskService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Execution Controller
 *
 * Handles task execution and monitoring
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/execution")
@RequiredArgsConstructor
@Tag(name = "Execution", description = "Task execution APIs")
public class ExecutionController {

    private final TaskService taskService;

    /**
     * Create a new execution task
     */
    @PostMapping("/tasks")
    @Operation(summary = "Create task", description = "Create a new execution task")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody TaskRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Creating task: {} for user: {}", request.getTaskName(), userPrincipal.getUsername());

        TaskResponse response = taskService.createTask(request, userPrincipal.getId());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success("Task created successfully", response));
    }

    /**
     * Get task by ID
     */
    @GetMapping("/tasks/{id}")
    @Operation(summary = "Get task", description = "Get task details by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskResponse>> getTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting task: {} for user: {}", id, userPrincipal.getUsername());

        TaskResponse response = taskService.getTaskById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all tasks
     */
    @GetMapping("/tasks")
    @Operation(summary = "List tasks", description = "Get all tasks with pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getAllTasks(
            @Parameter(description = "Status filter") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Page number (default: 0)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(value = "size", defaultValue = "20") int size) {
        log.debug("Getting tasks - status: {}, page: {}, size: {}", status, page, size);

        PageResponse<TaskResponse> response = taskService.getAllTasks(status, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get current user's tasks
     */
    @GetMapping("/my-tasks")
    @Operation(summary = "Get my tasks", description = "Get all tasks for current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<TaskResponse>>> getMyTasks(
            @Parameter(description = "Status filter") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Page number (default: 0)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting tasks for user: {} - status: {}, page: {}", userPrincipal.getUsername(), status, page);

        PageResponse<TaskResponse> response = taskService.getTasksByUser(userPrincipal.getId(), status, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get task logs
     */
    @GetMapping("/tasks/{id}/logs")
    @Operation(summary = "Get task logs", description = "Get execution logs for a task")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TaskLogResponse>>> getTaskLogs(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting logs for task: {}", id);

        List<TaskLogResponse> logs = taskService.getTaskLogs(id);

        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    /**
     * Cancel task
     */
    @PostMapping("/tasks/{id}/cancel")
    @Operation(summary = "Cancel task", description = "Cancel a running task")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> cancelTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Cancelling task: {} by user: {}", id, userPrincipal.getUsername());

        taskService.cancelTask(id, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Task cancelled successfully"));
    }

    /**
     * Retry task
     */
    @PostMapping("/tasks/{id}/retry")
    @Operation(summary = "Retry task", description = "Retry a failed task")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskResponse>> retryTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Retrying task: {} by user: {}", id, userPrincipal.getUsername());

        TaskResponse response = taskService.retryTask(id, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Task retry initiated", response));
    }

    /**
     * Delete task
     */
    @DeleteMapping("/tasks/{id}")
    @Operation(summary = "Delete task", description = "Delete a task")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Task ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Deleting task: {} by user: {}", id, userPrincipal.getUsername());

        taskService.deleteTask(id, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Task deleted successfully"));
    }

    /**
     * Get task statistics
     */
    @GetMapping("/statistics")
    @Operation(summary = "Get statistics", description = "Get execution statistics for current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<TaskService.TaskStatistics>> getStatistics(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting statistics for user: {}", userPrincipal.getUsername());

        TaskService.TaskStatistics statistics = taskService.getTaskStatistics(userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
}
