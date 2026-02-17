package com.operator.service.version;

import com.operator.common.dto.version.*;

import java.util.List;

/**
 * Version Service Interface
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface VersionService {

    /**
     * Create a new version
     */
    VersionResponse createVersion(VersionRequest request, String username);

    /**
     * Get version by ID
     */
    VersionResponse getVersionById(Long id);

    /**
     * Update version status
     */
    VersionResponse updateVersionStatus(Long id, String status, String username);

    /**
     * Release version
     */
    VersionResponse releaseVersion(Long id, String username);

    /**
     * Delete version
     */
    void deleteVersion(Long id, String username);

    /**
     * Compare two versions
     */
    VersionComparison compareVersions(Long versionId1, Long versionId2);

    /**
     * Version comparison result
     */
    record VersionComparison(
        VersionResponse version1,
        VersionResponse version2,
        List<String> differences
    ) {}
}
