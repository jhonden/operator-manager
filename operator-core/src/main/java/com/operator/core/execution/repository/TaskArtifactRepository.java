package com.operator.core.execution.repository;

import com.operator.core.execution.domain.TaskArtifact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for TaskArtifact entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface TaskArtifactRepository extends JpaRepository<TaskArtifact, Long> {

    /**
     * Find artifacts by task
     */
    List<TaskArtifact> findByTaskIdOrderByCreatedAtAsc(Long taskId);

    /**
     * Find artifacts by task and type
     */
    List<TaskArtifact> findByTaskIdAndArtifactType(Long taskId, TaskArtifact.ArtifactType artifactType);

    /**
     * Delete artifacts by task
     */
    void deleteByTaskId(Long taskId);
}
