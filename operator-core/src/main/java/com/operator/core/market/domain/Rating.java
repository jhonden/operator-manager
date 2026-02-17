package com.operator.core.market.domain;

import com.operator.core.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Rating Entity - represents user ratings for market items
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Entity
@Table(name = "ratings", indexes = {
    @Index(name = "idx_rating_market_item", columnList = "market_item_id"),
    @Index(name = "idx_rating_user", columnList = "user_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Rating extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_item_id", nullable = false)
    private MarketItem marketItem;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5 stars

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;
}
