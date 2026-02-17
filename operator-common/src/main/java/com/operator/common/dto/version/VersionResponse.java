package com.operator.common.dto.version;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Version Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionResponse {

    private Long id;
    private String versionNumber;
    private String description;
    private com.operator.common.enums.VersionStatus status;
    private String codeFilePath;
    private String fileName;
    private Long fileSize;
    private String gitCommitHash;
    private String gitTag;
    private Boolean isReleased;
    private LocalDateTime releaseDate;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
