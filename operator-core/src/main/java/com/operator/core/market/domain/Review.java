package com.operator.core.market.domain;

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
 * Review Entity - represents user reviews for market items
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "reviews", indexes = {
    @Index(name = "idx_review_market_item", columnList = "market_item_id"),
    @Index(name = "idx_review_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_item_id", nullable = false)
    private MarketItem marketItem;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_name", length = 100)
    private String userName;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "rating")
    private Integer rating; // 1-5 stars (optional, can rate without detailed review)

    @Column(name = "likes_count")
    @Builder.Default
    private Integer likesCount = 0;

    @Column(name = "parent_id")
    private Long parentId; // For reply reviews

    @OneToMany(mappedBy = "parentReview", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<Review> replies = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Review parentReview;
}
