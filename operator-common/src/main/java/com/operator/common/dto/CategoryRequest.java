package com.operator.common.dto;
import lombok.Data;
@Data
public class CategoryRequest {
    private String name;
    private Long parentId;
    private String description;
}
