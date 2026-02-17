package com.operator.common.dto;
import lombok.Data;
@Data
public class PkgResponse {
    private Long id;
    private String name;
    private String description;
    private String businessScenario;
    private String status;
    private String version;
}
