package com.operator.core.execution.domain;

import com.operator.common.enums.TaskStatus;
import com.operator.common.enums.TaskType;
import com.operator.core.domain.BaseEntity;
import com.operator.core.operator.domain.Operator;
import com.operator.core.pkg.domain.OperatorPackage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Task Entity - represents execution tasks
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_type", columnList = "task_type"),
    @Index(name = "idx_task_operator", columnList = "operator_id"),
    @Index(name = "idx_task_package", columnList = "package_id"),
    @Index(name = "idx_task_user", columnList = "user_id"),
    @Index(name = "idx_task_created", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseEntity {

    @Column(name = "task_name", nullable = false, length = 255)
    private String taskName;

    @Column(name = "task_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskType taskType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id")
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private OperatorPackage operatorPackage;

    @Column(name = "operator_version_id")
    private Long operatorVersionId;

    @Column(name = "package_version_id")
    private Long packageVersionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TaskStatus status = TaskStatus.PENDING;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 0; // Higher value = higher priority

    @Column(name = "input_parameters", columnDefinition = "TEXT")
    private String inputParameters; // JSON format

    @Column(name = "output_data", columnDefinition = "TEXT")
    private String outputData; // JSON format

    @Column(name = "progress", nullable = false)
    @Builder.Default
    private Integer progress = 0; // 0-100

    @Column(name = "started_at")
    private java.time.LocalDateTime startedAt;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "container_id", length = 100)
    private String containerId; // Docker container ID

    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries")
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "timeout_seconds")
    @Builder.Default
    private Integer timeoutSeconds = 300; // 5 minutes default

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<TaskLog> logs = new ArrayList<>();

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TaskArtifact> artifacts = new ArrayList<>();

    /**
     * Task type enum
     */

    /**
     * Task status enum
     */
}
