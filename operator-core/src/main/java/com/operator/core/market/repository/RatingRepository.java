package com.operator.core.market.repository;

import com.operator.core.market.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Rating entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {

    /**
     * Find ratings by market item
     */
    List<Rating> findByMarketItemId(Long marketItemId);

    /**
     * Find rating by user and market item
     */
    Optional<Rating> findByUserIdAndMarketItemId(Long userId, Long marketItemId);

    /**
     * Calculate average rating for market item
     */
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.marketItem.id = :marketItemId")
    Double calculateAverageRating(@Param("marketItemId") Long marketItemId);

    /**
     * Count ratings for market item
     */
    long countByMarketItemId(Long marketItemId);

    /**
     * Delete ratings by market item
     */
    void deleteByMarketItemId(Long marketItemId);
}
