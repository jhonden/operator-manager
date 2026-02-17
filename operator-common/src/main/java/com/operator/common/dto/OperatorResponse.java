package com.operator.common.dto;
import lombok.Data;
@Data
public class OperatorResponse {
    private Long id;
    private String name;
    private String description;
    private String language;
    private String status;
    private Long categoryId;
    private String version;
}
