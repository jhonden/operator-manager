package com.operator.core.operator.repository;

import com.operator.core.operator.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find root categories (parent is null)
     */
    List<Category> findByParentIsNullOrderByOrderIndexAsc();

    /**
     * Find child categories by parent
     */
    List<Category> findByParentIdOrderByOrderIndexAsc(Long parentId);

    /**
     * Find category by name
     */
    Optional<Category> findByName(String name);

    /**
     * Check if category name exists
     */
    boolean existsByName(String name);

    /**
     * Find all categories as tree structure
     */
    @Query("SELECT c FROM Category c LEFT JOIN FETCH c.children WHERE c.parent IS NULL ORDER BY c.orderIndex")
    List<Category> findCategoryTree();
}
