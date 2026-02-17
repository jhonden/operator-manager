package com.operator.common.dto.pkg;
import lombok.Data;
@Data
public class OperatorSearchRequest {
    private String keyword;
    private String language;
    private String status;
    private Long categoryId;
    private String sortBy;
    private String sortOrder;
    private Integer page;
    private Integer size;
}
