package com.operator.core.execution.domain;

import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Task Artifact Entity - represents output artifacts from task execution
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "task_artifacts", indexes = {
    @Index(name = "idx_task_artifact_task", columnList = "task_id"),
    @Index(name = "idx_task_artifact_type", columnList = "artifact_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskArtifact extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "artifact_name", nullable = false, length = 255)
    private String artifactName;

    @Column(name = "artifact_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private ArtifactType artifactType;

    @Column(name = "file_path", length = 500)
    private String filePath; // MinIO path

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Artifact type enum
     */
    public enum ArtifactType {
        OUTPUT_FILE,
        LOG_FILE,
        REPORT,
        METADATA,
        SCREENSHOT,
        OTHER
    }
}
