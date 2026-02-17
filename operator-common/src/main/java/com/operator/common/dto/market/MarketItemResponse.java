package com.operator.common.dto.market;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Market Item Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketItemResponse {

    private Long id;
    private String name;
    private String description;
    private com.operator.common.enums.ItemType itemType;
    private Long operatorId;
    private Long packageId;
    private Boolean featured;
    private Double averageRating;
    private Integer ratingsCount;
    private Integer reviewsCount;
    private Integer downloadsCount;
    private Integer viewsCount;
    private com.operator.common.enums.MarketStatus status;
    private LocalDateTime publishedDate;
    private List<String> tags;
    private String businessScenario;
    private LocalDateTime createdAt;

    // Additional fields for detail view
    private String operatorLanguage;
    private String operatorDescription;
    private String packageBusinessScenario;
}
