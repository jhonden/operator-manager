package com.operator.common.dto.pkg;
import lombok.Data;
@Data
public class PackageSearchRequest {
    private String keyword;
    private String status;
    private Integer page;
    private Integer size;
}
