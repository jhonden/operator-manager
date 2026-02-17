package com.operator.core.version.domain;

import com.operator.core.domain.BaseEntity;
import com.operator.core.pkg.domain.OperatorPackage;
import com.operator.core.publish.domain.PublishHistory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Package Version Entity - represents package versions
 * Includes all operator versions in the package
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "package_versions", indexes = {
    @Index(name = "idx_pkg_version_package", columnList = "package_id"),
    @Index(name = "idx_pkg_version_number", columnList = "version_number"),
    @Index(name = "idx_pkg_version_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PackageVersion extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private OperatorPackage operatorPackage;

    @Column(name = "version_number", nullable = false, length = 50)
    private String versionNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "changelog", columnDefinition = "TEXT")
    private String changelog;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VersionStatus status = VersionStatus.DRAFT;

    @Column(name = "operator_versions", columnDefinition = "TEXT")
    private String operatorVersions; // JSON array of operator_id -> version_id mappings

    @Column(name = "git_commit_hash", length = 100)
    private String gitCommitHash;

    @Column(name = "git_tag", length = 100)
    private String gitTag;

    @Column(name = "is_released", nullable = false)
    @Builder.Default
    private Boolean isReleased = false;

    @Column(name = "release_date")
    private java.time.LocalDateTime releaseDate;

    @OneToMany(mappedBy = "packageVersion")
    private List<PublishHistory> publishHistories = new ArrayList<>();

    /**
     * Version status enum
     */
    public enum VersionStatus {
        DRAFT,
        BUILD,
        TEST,
        REVIEW,
        PUBLISHED,
        ARCHIVED
    }
}
