package com.operator.common.dto.execution;


import com.operator.common.enums.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Task Log Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskLogResponse {

    private Long id;
    private Long taskId;
    private LogLevel logLevel;
    private String message;
    private LocalDateTime timestamp;
    private String source;
    private String exceptionTrace;
}
