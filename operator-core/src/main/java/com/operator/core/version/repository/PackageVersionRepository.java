package com.operator.core.version.repository;

import com.operator.common.enums.VersionStatus;
import com.operator.core.pkg.domain.OperatorPackage;
import com.operator.core.version.domain.PackageVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PackageVersion entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface PackageVersionRepository extends JpaRepository<PackageVersion, Long> {

    /**
     * Find versions by package
     */
    List<PackageVersion> findByOperatorPackageIdOrderByCreatedAtDesc(Long packageId);

    /**
     * Find version by package and version number
     */
    Optional<PackageVersion> findByOperatorPackageIdAndVersionNumber(Long packageId, String versionNumber);

    /**
     * Find published versions by package
     */
    List<PackageVersion> findByOperatorPackageIdAndStatusOrderByCreatedAtDesc(Long packageId, VersionStatus status);

    /**
     * Find latest version by package
     */
    @Query("SELECT v FROM PackageVersion v WHERE v.operatorPackage.id = :packageId ORDER BY v.createdAt DESC")
    Optional<PackageVersion> findLatestByPackageId(@Param("packageId") Long packageId);
}
