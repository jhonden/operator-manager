package com.operator.common.dto.operator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Category Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String icon;
    private String color;
    private Long parentId;
    private Integer orderIndex;
    private Integer operatorCount;
    private List<CategoryResponse> children;
    private LocalDateTime createdAt;
}
