package com.operator.common.dto.market;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
    import lombok.NoArgsConstructor;

/**
 * Market Search/Filter Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketSearchRequest {

    private String keyword;
    private String itemType; // OPERATOR or PACKAGE
    private String tags;
    private String businessScenario;
    private Boolean featured;
    private String sortBy = "averageRating"; // averageRating, downloadsCount, latest
    private String sortOrder = "desc";
    private Integer page = 0;
    private Integer size = 20;
}
