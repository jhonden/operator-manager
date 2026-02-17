package com.operator.common.dto;

import lombok.Data;

/**
 * Market Search Request DTO
 */
@Data
public class MarketSearchRequest {
    private String keyword;
    private String itemType;
    private Long categoryId;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}
