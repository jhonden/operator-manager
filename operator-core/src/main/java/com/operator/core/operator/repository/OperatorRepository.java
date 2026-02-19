package com.operator.core.operator.repository;

import com.operator.common.enums.LanguageType;
import com.operator.common.enums.OperatorStatus;
import com.operator.core.operator.domain.Operator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Operator entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface OperatorRepository extends JpaRepository<Operator, Long> {

    /**
     * Find operators by status
     */
    List<Operator> findByStatus(OperatorStatus status);

    /**
     * Find operators by language
     */
    List<Operator> findByLanguage(LanguageType language);

    /**
     * Find public operators
     */
    List<Operator> findByIsPublicTrue();

    /**
     * Find featured operators
     */
    List<Operator> findByFeaturedTrue();

    /**
     * Search operators by name or description
     */
    @Query("SELECT o FROM Operator o WHERE " +
           "LOWER(o.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(o.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Operator> searchOperators(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find operators by creator
     */
    List<Operator> findByCreatedBy(String username);

    /**
     * Count operators by status
     */
    long countByStatus(OperatorStatus status);

    /**
     * Find operator by name
     */
    Optional<Operator> findByName(String name);

    /**
     * Check if operator name exists
     */
    boolean existsByName(String name);

    /**
     * Find all operators with parameters eagerly loaded
     */
    @Query("SELECT DISTINCT o FROM Operator o " +
           "LEFT JOIN FETCH o.parameters " +
           "ORDER BY o.createdAt DESC")
    List<Operator> findAllWithAssociations();

    /**
     * Find operator by ID with parameters eagerly loaded
     */
    @Query("SELECT o FROM Operator o " +
           "LEFT JOIN FETCH o.parameters " +
           "WHERE o.id = :id")
    Optional<Operator> findByIdWithAssociations(@Param("id") Long id);
}
