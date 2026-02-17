package com.operator.common.dto;
import lombok.Data;
@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String description;
}
