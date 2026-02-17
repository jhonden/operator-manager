package com.operator.core.market.repository;

import com.operator.core.market.domain.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Review entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find reviews by market item
     */
    Page<Review> findByMarketItemIdOrderByCreatedAtDesc(Long marketItemId, Pageable pageable);

    /**
     * Find top-level reviews (no parent)
     */
    List<Review> findByMarketItemIdAndParentIdIsNullOrderByCreatedAtDesc(Long marketItemId);

    /**
     * Find replies by parent review
     */
    List<Review> findByParentIdOrderByCreatedAtAsc(Long parentId);

    /**
     * Find reviews by user
     */
    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Count reviews for market item
     */
    long countByMarketItemId(Long marketItemId);
}
