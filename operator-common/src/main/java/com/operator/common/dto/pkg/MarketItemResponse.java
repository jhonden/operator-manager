package com.operator.common.dto.pkg;

import lombok.Data;

/**
 * Market Item Response DTO
 */
@Data
public class MarketItemResponse {
    private Long id;
    private String itemType;
    private Long itemId;
    private String name;
    private String description;
    private String businessScenario;
    private Double averageRating;
    private Integer ratingsCount;
    private Integer downloadsCount;
    private String status;
    private String createdBy;
    private String createdAt;
}
