package com.operator.core.version.domain;

import com.operator.core.domain.BaseEntity;
import com.operator.common.enums.*;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Version Entity - represents operator versions
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "versions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Version extends BaseEntity {

    @Column(name = "version_number", nullable = false, length = 50)
    private String versionNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private VersionStatus status = VersionStatus.DRAFT;

    @Column(name = "code_file_path", length = 500)
    private String codeFilePath;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "git_commit_hash", length = 100)
    private String gitCommitHash;

    @Column(name = "git_tag", length = 100)
    private String gitTag;

    @Column(name = "is_released", nullable = false)
    @Builder.Default
    private Boolean isReleased = false;

    @Column(name = "release_date")
    private java.time.LocalDateTime releaseDate;
}
