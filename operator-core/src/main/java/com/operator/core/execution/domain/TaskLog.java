package com.operator.core.execution.domain;

import com.operator.common.enums.LogLevel;
import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Task Log Entity - represents task execution logs
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "task_logs", indexes = {
    @Index(name = "idx_task_log_task", columnList = "task_id"),
    @Index(name = "idx_task_log_level", columnList = "log_level"),
    @Index(name = "idx_task_log_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "log_level", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private LogLevel logLevel;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "timestamp", nullable = false)
    private java.time.LocalDateTime timestamp;

    @Column(name = "source", length = 100)
    private String source; // Source of the log (e.g., "executor", "container")

    @Column(name = "exception_trace", columnDefinition = "TEXT")
    private String exceptionTrace;

    /**
     * Log level enum
     */
}
