package com.operator.core.version.repository;

import com.operator.core.version.domain.Version;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Version entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface VersionRepository extends JpaRepository<Version, Long> {

    /**
     * Find versions by status
     */
    List<Version> findByStatus(com.operator.common.enums.VersionStatus status);

    /**
     * Find version by version number
     */
    Optional<Version> findByVersionNumber(String versionNumber);
}
