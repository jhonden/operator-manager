package com.operator.service.market;

import com.operator.common.dto.market.*;
import com.operator.common.utils.PageResponse;

import java.util.List;

/**
 * Market Service Interface
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface MarketService {

    /**
     * Search market items
     */
    PageResponse<MarketItemResponse> searchItems(MarketSearchRequest request);

    /**
     * Get market item by ID
     */
    MarketItemResponse getItemById(Long id);

    /**
     * Get featured items
     */
    List<MarketItemResponse> getFeaturedItems();

    /**
     * Get top rated items
     */
    List<MarketItemResponse> getTopRatedItems();

    /**
     * Get most downloaded items
     */
    List<MarketItemResponse> getMostDownloadedItems();

    /**
     * Get latest items
     */
    List<MarketItemResponse> getLatestItems();

    /**
     * Publish operator to market
     */
    MarketItemResponse publishOperator(Long operatorId, String username);

    /**
     * Publish package to market
     */
    MarketItemResponse publishPackage(Long packageId, String username);

    /**
     * Unpublish from market
     */
    void unpublishFromMarket(Long marketItemId, String username);

    /**
     * Submit rating
     */
    void submitRating(Long marketItemId, RatingRequest request, Long userId);

    /**
     * Submit review
     */
    ReviewResponse submitReview(Long marketItemId, ReviewRequest request, Long userId);

    /**
     * Get reviews for item
     */
    List<ReviewResponse> getReviews(Long marketItemId, int page, int size);

    /**
     * Like review
     */
    void likeReview(Long reviewId, Long userId);

    /**
     * Download item (clone to workspace)
     */
    void downloadItem(Long marketItemId, Long userId);

    /**
     * Increment view count
     */
    void incrementViewCount(Long marketItemId);
}
