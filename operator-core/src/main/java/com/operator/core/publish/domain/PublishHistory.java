package com.operator.core.publish.domain;

import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Publish History Entity - tracks publish operations
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "publish_history", indexes = {
    @Index(name = "idx_publish_dest", columnList = "publish_destination_id"),
    @Index(name = "idx_publish_status", columnList = "status"),
    @Index(name = "idx_publish_item_type", columnList = "item_type")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PublishHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publish_destination_id", nullable = false)
    private PublishDestination publishDestination;

    @Column(name = "package_version_id")
    private Long packageVersionId;

    @Column(name = "item_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(name = "version", length = 50)
    private String version;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PublishStatus status = PublishStatus.PENDING;

    @Column(name = "started_at")
    private java.time.LocalDateTime startedAt;

    @Column(name = "completed_at")
    private java.time.LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "published_path", length = 500)
    private String publishedPath;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata; // JSON with additional info

    /**
     * Item type enum
     */
    public enum ItemType {
        OPERATOR,
        PACKAGE
    }
}
