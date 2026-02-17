package com.operator.common.dto.pkg;
import lombok.Data;
@Data
public class MarketItemDetailResponse {
    private Long id;
    private String itemType;
    private Long itemId;
    private String name;
    private String description;
    private String code;
    private Double averageRating;
    private Integer ratingsCount;
    private Integer downloadsCount;
}
