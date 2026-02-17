package com.operator.core.market.repository;

import com.operator.core.market.domain.MarketItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for MarketItem entity
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface MarketItemRepository extends JpaRepository<MarketItem, Long> {

    /**
     * Find items by type
     */
    List<MarketItem> findByItemType(com.operator.common.enums.ItemType itemType);

    /**
     * Find featured items
     */
    List<MarketItem> findByFeaturedTrueOrderByAverageRatingDesc();

    /**
     * Find published items
     */
    List<MarketItem> findByStatusOrderByAverageRatingDesc(com.operator.common.enums.MarketStatus status);

    /**
     * Search items with full-text search
     */
    @Query("SELECT m FROM MarketItem m WHERE m.status = 'PUBLISHED' AND " +
           "(LOWER(m.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.tags) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.businessScenario) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<MarketItem> searchItems(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find top rated items
     */
    List<MarketItem> findTop10ByStatusOrderByAverageRatingDescDownloadsCountDesc(com.operator.common.enums.MarketStatus status);

    /**
     * Find most downloaded items
     */
    List<MarketItem> findTop10ByStatusOrderByDownloadsCountDesc(com.operator.common.enums.MarketStatus status);

    /**
     * Find latest items
     */
    List<MarketItem> findTop10ByStatusOrderByPublishedDateDesc(com.operator.common.enums.MarketStatus status);

    /**
     * Find market item by operator ID
     */
    java.util.Optional<MarketItem> findByOperatorId(Long operatorId);

    /**
     * Find market item by package ID
     */
    java.util.Optional<MarketItem> findByPackageId(Long packageId);
}
