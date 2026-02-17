package com.operator.common.dto.execution;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Task Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;
    private String taskName;
    private com.operator.common.enums.TaskType taskType;
    private Long operatorId;
    private String operatorName;
    private Long packageId;
    private String packageName;
    private Long operatorVersionId;
    private Long packageVersionId;
    private Long userId;
    private String username;
    private com.operator.common.enums.TaskStatus status;
    private Integer priority;
    private Map<String, Object> inputParameters;
    private Map<String, Object> outputData;
    private Integer progress;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String errorMessage;
    private String containerId;
    private Integer retryCount;
    private Integer maxRetries;
    private Integer timeoutSeconds;
    private LocalDateTime createdAt;
    private Long durationSeconds; // Calculated duration
}
