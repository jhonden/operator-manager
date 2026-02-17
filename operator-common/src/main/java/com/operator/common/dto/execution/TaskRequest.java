package com.operator.common.dto.execution;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Create Task Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Task name is required")
    private String taskName;

    @NotNull(message = "Task type is required")
    private com.operator.common.enums.TaskType taskType;

    private Long operatorId;

    private Long packageId;

    private Long operatorVersionId;

    private Long packageVersionId;

    private Map<String, Object> inputParameters; // JSON format

    @Builder.Default
    private Integer priority = 0;

    @Builder.Default
    private Integer timeoutSeconds = 300;
}
