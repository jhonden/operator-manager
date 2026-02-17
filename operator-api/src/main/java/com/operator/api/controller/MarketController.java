package com.operator.api.controller;

import com.operator.infrastructure.security.UserPrincipal;
import com.operator.common.utils.ApiResponse;
import com.operator.common.utils.PageResponse;
import com.operator.common.dto.market.MarketItemResponse;
import com.operator.common.dto.market.MarketSearchRequest;
import com.operator.common.dto.market.RatingRequest;
import com.operator.common.dto.market.ReviewRequest;
import com.operator.common.dto.market.ReviewResponse;
import com.operator.service.market.MarketService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Market Controller
 *
 * Handles marketplace operations
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/market")
@RequiredArgsConstructor
@Tag(name = "Marketplace", description = "Marketplace APIs")
public class MarketController {

    private final MarketService marketService;

    /**
     * Search market items
     */
    @PostMapping("/search")
    @Operation(summary = "Search marketplace", description = "Search for items in the marketplace")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<MarketItemResponse>>> searchItems(
            @Valid @RequestBody MarketSearchRequest request) {
        log.info("Searching marketplace with request: {}", request);

        PageResponse<MarketItemResponse> response = marketService.searchItems(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get market item by ID
     */
    @GetMapping("/items/{id}")
    @Operation(summary = "Get market item", description = "Get market item details by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MarketItemResponse>> getItemById(
            @Parameter(description = "Market item ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting market item: {}", id);

        // Increment view count
        marketService.incrementViewCount(id);

        MarketItemResponse response = marketService.getItemById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get featured items
     */
    @GetMapping("/featured")
    @Operation(summary = "Get featured items", description = "Get all featured marketplace items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MarketItemResponse>>> getFeaturedItems() {
        log.debug("Getting featured items");

        List<MarketItemResponse> items = marketService.getFeaturedItems();

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * Get top rated items
     */
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated", description = "Get top rated marketplace items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MarketItemResponse>>> getTopRatedItems() {
        log.debug("Getting top rated items");

        List<MarketItemResponse> items = marketService.getTopRatedItems();

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * Get most downloaded items
     */
    @GetMapping("/most-downloaded")
    @Operation(summary = "Get most downloaded", description = "Get most downloaded marketplace items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MarketItemResponse>>> getMostDownloadedItems() {
        log.debug("Getting most downloaded items");

        List<MarketItemResponse> items = marketService.getMostDownloadedItems();

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * Get latest items
     */
    @GetMapping("/latest")
    @Operation(summary = "Get latest", description = "Get latest marketplace items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MarketItemResponse>>> getLatestItems() {
        log.debug("Getting latest items");

        List<MarketItemResponse> items = marketService.getLatestItems();

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * Publish operator to market
     */
    @PostMapping("/publish/operator/{operatorId}")
    @Operation(summary = "Publish operator", description = "Publish operator to marketplace")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MarketItemResponse>> publishOperator(
            @Parameter(description = "Operator ID") @PathVariable Long operatorId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Publishing operator: {} to market by user: {}", operatorId, userPrincipal.getUsername());

        MarketItemResponse response = marketService.publishOperator(operatorId, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Operator published to marketplace successfully", response));
    }

    /**
     * Publish package to market
     */
    @PostMapping("/publish/package/{packageId}")
    @Operation(summary = "Publish package", description = "Publish package to marketplace")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<MarketItemResponse>> publishPackage(
            @Parameter(description = "Package ID") @PathVariable Long packageId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Publishing package: {} to market by user: {}", packageId, userPrincipal.getUsername());

        MarketItemResponse response = marketService.publishPackage(packageId, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Package published to marketplace successfully", response));
    }

    /**
     * Unpublish from market
     */
    @DeleteMapping("/items/{id}")
    @Operation(summary = "Unpublish item", description = "Unpublish item from marketplace")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> unpublishFromMarket(
            @Parameter(description = "Market item ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Unpublishing item: {} from market by user: {}", id, userPrincipal.getUsername());

        marketService.unpublishFromMarket(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Item unpublished from marketplace successfully"));
    }

    /**
     * Submit rating
     */
    @PostMapping("/items/{id}/rating")
    @Operation(summary = "Submit rating", description = "Submit rating for marketplace item")
    public ResponseEntity<ApiResponse<Void>> submitRating(
            @Parameter(description = "Market item ID") @PathVariable Long id,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Submitting rating for item: {} by user: {}", id, userPrincipal.getUsername());

        marketService.submitRating(id, request, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Rating submitted successfully"));
    }

    /**
     * Submit review
     */
    @PostMapping("/items/{id}/reviews")
    @Operation(summary = "Submit review", description = "Submit review for marketplace item")
    public ResponseEntity<ApiResponse<ReviewResponse>> submitReview(
            @Parameter(description = "Market item ID") @PathVariable Long id,
            @Valid @RequestBody ReviewRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Submitting review for item: {} by user: {}", id, userPrincipal.getUsername());

        ReviewResponse response = marketService.submitReview(id, request, userPrincipal.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Review submitted successfully", response));
    }

    /**
     * Get reviews for item
     */
    @GetMapping("/items/{id}/reviews")
    @Operation(summary = "Get reviews", description = "Get reviews for marketplace item")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @Parameter(description = "Market item ID") @PathVariable Long id,
            @Parameter(description = "Page number (default: 0)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(value = "size", defaultValue = "20") int size) {
        log.debug("Getting reviews for item: {}", id);

        List<ReviewResponse> reviews = marketService.getReviews(id, page, size);

        return ResponseEntity.ok(ApiResponse.success(reviews));
    }

    /**
     * Like review
     */
    @PostMapping("/reviews/{reviewId}/like")
    @Operation(summary = "Like review", description = "Like a review")
    public ResponseEntity<ApiResponse<Void>> likeReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Liking review: {} by user: {}", reviewId, userPrincipal.getUsername());

        marketService.likeReview(reviewId, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Review liked successfully"));
    }

    /**
     * Download item (clone to workspace)
     */
    @PostMapping("/items/{id}/download")
    @Operation(summary = "Download item", description = "Download marketplace item to workspace")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<Void>> downloadItem(
            @Parameter(description = "Market item ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Downloading item: {} by user: {}", id, userPrincipal.getUsername());

        marketService.downloadItem(id, userPrincipal.getId());

        return ResponseEntity.ok(ApiResponse.success("Item downloaded successfully"));
    }
}
