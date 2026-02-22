package com.operator.core.pkg.repository;

import com.operator.core.pkg.domain.PackageOperator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PackageOperator entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface PackageOperatorRepository extends JpaRepository<PackageOperator, Long> {

    /**
     * Find package operators by package with operator JOIN FETCH
     */
    @Query("SELECT po FROM PackageOperator po " +
            "LEFT JOIN FETCH po.operator " +
            "WHERE po.operatorPackage.id = :packageId " +
            "ORDER BY po.orderIndex ASC")
    List<PackageOperator> findByOperatorPackageIdOrderByOrderIndexAscWithFetch(@org.springframework.data.repository.query.Param("packageId") Long packageId);

    /**
     * Find package operators by package
     */
    List<PackageOperator> findByOperatorPackageIdOrderByOrderIndexAsc(Long packageId);

    /**
     * Find package operators by operator ID
     */
    List<PackageOperator> findByOperatorId(Long operatorId);

    /**
     * Find package operator by package and operator
     */
    Optional<PackageOperator> findByOperatorPackageIdAndOperatorId(Long packageId, Long operatorId);

    /**
     * Check if operator exists in package
     */
    boolean existsByOperatorPackageIdAndOperatorId(Long packageId, Long operatorId);

    /**
     * Delete package operators by package
     */
    void deleteByOperatorPackageId(Long packageId);

    /**
     * Count operators in package
     */
    long countByOperatorPackageId(Long packageId);
}
