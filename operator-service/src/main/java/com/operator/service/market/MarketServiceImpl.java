package com.operator.service.market;

import com.operator.common.enums.*;
import com.operator.common.dto.market.*;
import com.operator.common.dto.UserInfo;
import com.operator.common.exception.ResourceNotFoundException;
import com.operator.common.utils.PageResponse;
import com.operator.core.market.domain.*;
import com.operator.core.market.repository.*;
import com.operator.core.operator.domain.Operator;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.core.pkg.domain.OperatorPackage;
import com.operator.core.pkg.repository.OperatorPackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Market Service Implementation (Stub)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarketServiceImpl implements MarketService {

    private final MarketItemRepository marketItemRepository;
    private final RatingRepository ratingRepository;
    private final ReviewRepository reviewRepository;
    private final OperatorRepository operatorRepository;
    private final OperatorPackageRepository packageRepository;

    @Override
    public PageResponse<MarketItemResponse> searchItems(MarketSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),
                Sort.by(Sort.Direction.DESC, getSortField(request.getSortBy())));

        Page<MarketItem> items = marketItemRepository.searchItems(
                request.getKeyword() != null ? request.getKeyword() : "", pageable);

        return PageResponse.of(items.map(this::mapMarketItemToResponse));
    }

    @Override
    public MarketItemResponse getItemById(Long id) {
        MarketItem item = marketItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MarketItem", id));
        return mapMarketItemToResponse(item);
    }

    @Override
    public List<MarketItemResponse> getFeaturedItems() {
        return marketItemRepository.findByFeaturedTrueOrderByAverageRatingDesc().stream()
                .map(this::mapMarketItemToResponse)
                .toList();
    }

    @Override
    public List<MarketItemResponse> getTopRatedItems() {
        return marketItemRepository.findTop10ByStatusOrderByAverageRatingDescDownloadsCountDesc(
                MarketStatus.PUBLISHED).stream()
                .map(this::mapMarketItemToResponse)
                .toList();
    }

    @Override
    public List<MarketItemResponse> getMostDownloadedItems() {
        return marketItemRepository.findTop10ByStatusOrderByDownloadsCountDesc(
                MarketStatus.PUBLISHED).stream()
                .map(this::mapMarketItemToResponse)
                .toList();
    }

    @Override
    public List<MarketItemResponse> getLatestItems() {
        return marketItemRepository.findTop10ByStatusOrderByPublishedDateDesc(
                MarketStatus.PUBLISHED).stream()
                .map(this::mapMarketItemToResponse)
                .toList();
    }

    @Override
    @Transactional
    public MarketItemResponse publishOperator(Long operatorId, String username) {
        Operator operator = operatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator", operatorId));

        // Check if already published
        if (marketItemRepository.findByOperatorId(operatorId).isPresent()) {
            throw new IllegalArgumentException("Operator already published to marketplace");
        }

        MarketItem marketItem = new MarketItem();
        marketItem.setName(operator.getName());
        marketItem.setDescription(operator.getDescription());
        marketItem.setItemType(ItemType.OPERATOR);
        marketItem.setOperatorId(operatorId);
        marketItem.setFeatured(false);
        marketItem.setAverageRating(0.0);
        marketItem.setRatingsCount(0);
        marketItem.setReviewsCount(0);
        marketItem.setDownloadsCount(0);
        marketItem.setViewsCount(0);
        marketItem.setStatus(MarketStatus.PUBLISHED);
        marketItem.setPublishedDate(LocalDateTime.now());
        marketItem.setTags(operator.getTags());
        marketItem.setCreatedBy(username);

        marketItem = marketItemRepository.save(marketItem);

        return mapMarketItemToResponse(marketItem);
    }

    @Override
    @Transactional
    public MarketItemResponse publishPackage(Long packageId, String username) {
        OperatorPackage pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new ResourceNotFoundException("Package", packageId));

        // Check if already published
        if (marketItemRepository.findByPackageId(packageId).isPresent()) {
            throw new IllegalArgumentException("Package already published to marketplace");
        }

        MarketItem marketItem = new MarketItem();
        marketItem.setName(pkg.getName());
        marketItem.setDescription(pkg.getDescription());
        marketItem.setItemType(ItemType.PACKAGE);
        marketItem.setPackageId(packageId);
        marketItem.setFeatured(false);
        marketItem.setAverageRating(0.0);
        marketItem.setRatingsCount(0);
        marketItem.setReviewsCount(0);
        marketItem.setDownloadsCount(0);
        marketItem.setViewsCount(0);
        marketItem.setStatus(MarketStatus.PUBLISHED);
        marketItem.setPublishedDate(LocalDateTime.now());
        marketItem.setTags(pkg.getTags());
        marketItem.setBusinessScenario(pkg.getBusinessScenario());
        marketItem.setCreatedBy(username);

        marketItem = marketItemRepository.save(marketItem);

        return mapMarketItemToResponse(marketItem);
    }

    @Override
    @Transactional
    public void unpublishFromMarket(Long marketItemId, String username) {
        MarketItem item = marketItemRepository.findById(marketItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MarketItem", marketItemId));

        marketItemRepository.delete(item);
    }

    @Override
    @Transactional
    public void submitRating(Long marketItemId, RatingRequest request, Long userId) {
        MarketItem item = marketItemRepository.findById(marketItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MarketItem", marketItemId));

        // Check if user already rated
        if (ratingRepository.findByUserIdAndMarketItemId(userId, marketItemId).isPresent()) {
            // Update existing rating
            Rating rating = ratingRepository.findByUserIdAndMarketItemId(userId, marketItemId).get();
            rating.setRating(request.getRating());
            rating.setReview(request.getReview());
            ratingRepository.save(rating);
        } else {
            // Create new rating
            Rating rating = new Rating();
            rating.setMarketItem(item);
            rating.setUserId(userId);
            rating.setRating(request.getRating());
            rating.setReview(request.getReview());

            ratingRepository.save(rating);
            item.setRatingsCount(item.getRatingsCount() + 1);
        }

        // Update average rating
        Double newAverage = ratingRepository.calculateAverageRating(marketItemId);
        item.setAverageRating(newAverage != null ? newAverage : 0.0);
        marketItemRepository.save(item);
    }

    @Override
    @Transactional
    public ReviewResponse submitReview(Long marketItemId, ReviewRequest request, Long userId) {
        MarketItem item = marketItemRepository.findById(marketItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MarketItem", marketItemId));

        Review review = new Review();
        review.setMarketItem(item);
        review.setUserId(userId);
        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setLikesCount(0);

        review = reviewRepository.save(review);

        item.setReviewsCount(item.getReviewsCount() + 1);
        marketItemRepository.save(item);

        return mapReviewToResponse(review);
    }

    @Override
    public List<ReviewResponse> getReviews(Long marketItemId, int page, int size) {
        return reviewRepository.findByMarketItemIdOrderByCreatedAtDesc(
                marketItemId, PageRequest.of(page, size)).stream()
                .map(this::mapReviewToResponse)
                .toList();
    }

    @Override
    @Transactional
    public void likeReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", reviewId));

        review.setLikesCount(review.getLikesCount() + 1);
        reviewRepository.save(review);
    }

    @Override
    @Transactional
    public void downloadItem(Long marketItemId, Long userId) {
        MarketItem item = marketItemRepository.findById(marketItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MarketItem", marketItemId));

        item.setDownloadsCount(item.getDownloadsCount() + 1);

        // TODO: Clone operator or package to user's workspace

        marketItemRepository.save(item);
    }

    @Override
    @Transactional
    public void incrementViewCount(Long marketItemId) {
        MarketItem item = marketItemRepository.findById(marketItemId)
                .orElseThrow(() -> new ResourceNotFoundException("MarketItem", marketItemId));

        item.setViewsCount(item.getViewsCount() + 1);
        marketItemRepository.save(item);
    }

    private MarketItemResponse mapMarketItemToResponse(MarketItem item) {
        return MarketItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .itemType(item.getItemType())
                .operatorId(item.getOperatorId())
                .packageId(item.getPackageId())
                .featured(item.getFeatured())
                .averageRating(item.getAverageRating())
                .ratingsCount(item.getRatingsCount())
                .reviewsCount(item.getReviewsCount())
                .downloadsCount(item.getDownloadsCount())
                .viewsCount(item.getViewsCount())
                .status(item.getStatus())
                .publishedDate(item.getPublishedDate())
                .tags(item.getTags() != null ? List.of(item.getTags().split(",")) : null)
                .businessScenario(item.getBusinessScenario())
                .createdAt(item.getCreatedAt())
                .build();
    }

    private ReviewResponse mapReviewToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .marketItemId(review.getMarketItem() != null ? review.getMarketItem().getId() : null)
                .userId(review.getUserId())
                .userName(review.getUserName())
                .content(review.getContent())
                .rating(review.getRating())
                .likesCount(review.getLikesCount())
                .parentId(review.getParentId())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private String getSortField(String sortBy) {
        return switch (sortBy) {
            case "averageRating" -> "averageRating";
            case "downloadsCount" -> "downloadsCount";
            case "latest" -> "publishedDate";
            default -> "createdAt";
        };
    }
}
