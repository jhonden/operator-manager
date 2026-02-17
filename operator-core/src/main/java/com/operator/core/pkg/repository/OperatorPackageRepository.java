package com.operator.core.pkg.repository;

import com.operator.core.pkg.domain.OperatorPackage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for OperatorPackage entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface OperatorPackageRepository extends JpaRepository<OperatorPackage, Long> {

    /**
     * Find packages by status
     */
    List<OperatorPackage> findByStatus(com.operator.common.enums.PackageStatus status);

    /**
     * Find public packages
     */
    List<OperatorPackage> findByIsPublicTrue();

    /**
     * Find featured packages
     */
    List<OperatorPackage> findByFeaturedTrue();

    /**
     * Search packages by name or description or business scenario
     */
    @Query("SELECT p FROM OperatorPackage p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.businessScenario) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<OperatorPackage> searchPackages(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find packages by business scenario
     */
    List<OperatorPackage> findByBusinessScenarioContaining(String businessScenario);

    /**
     * Find packages by creator
     */
    List<OperatorPackage> findByCreatedBy(String username);

    /**
     * Count packages by status
     */
    long countByStatus(com.operator.common.enums.PackageStatus status);
}
