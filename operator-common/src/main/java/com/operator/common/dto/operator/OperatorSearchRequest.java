package com.operator.common.dto.operator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Operator Search/Filter Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorSearchRequest {

    private String keyword;
    private Long categoryId;
    private String language;
    private String status;
    private String tags;
    private Boolean isPublic;
    private Boolean featured;
    private String sortBy = "createdAt";
    private String sortOrder = "desc";
    private Integer page = 0;
    private Integer size = 20;
}
