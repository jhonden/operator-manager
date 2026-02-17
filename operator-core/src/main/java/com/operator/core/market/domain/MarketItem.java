package com.operator.core.market.domain;

import com.operator.common.enums.ItemType;
import com.operator.common.enums.MarketStatus;
import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Market Item Entity - represents items in the marketplace (operators or packages)
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "market_items", indexes = {
    @Index(name = "idx_market_item_type", columnList = "item_type"),
    @Index(name = "idx_market_item_featured", columnList = "featured"),
    @Index(name = "idx_market_item_operator", columnList = "operator_id"),
    @Index(name = "idx_market_item_package", columnList = "package_id"),
    @Index(name = "idx_market_item_rating", columnList = "average_rating")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MarketItem extends BaseEntity {

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "item_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ItemType itemType;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "package_id")
    private Long packageId;

    @Column(name = "featured", nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "ratings_count")
    @Builder.Default
    private Integer ratingsCount = 0;

    @Column(name = "reviews_count")
    @Builder.Default
    private Integer reviewsCount = 0;

    @Column(name = "downloads_count")
    @Builder.Default
    private Integer downloadsCount = 0;

    @Column(name = "views_count")
    @Builder.Default
    private Integer viewsCount = 0;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private MarketStatus status = MarketStatus.PUBLISHED;

    @Column(name = "published_date")
    private java.time.LocalDateTime publishedDate;

    @Column(name = "tags", columnDefinition = "TEXT")
    private String tags; // JSON array

    @Column(name = "business_scenario", length = 255)
    private String businessScenario; // For packages

    @OneToMany(mappedBy = "marketItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "marketItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    /**
     * Item type enum
     */

    /**
     * Market status enum
     */
}
