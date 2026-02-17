package com.operator.common.dto.pkg;
import lombok.Data;
@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private Long parentId;
    private String description;
}
