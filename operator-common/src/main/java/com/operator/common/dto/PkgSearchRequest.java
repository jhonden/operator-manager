package com.operator.common.dto;
import lombok.Data;
@Data
public class PkgSearchRequest {
    private String keyword;
    private String status;
    private Integer page;
    private Integer size;
}
