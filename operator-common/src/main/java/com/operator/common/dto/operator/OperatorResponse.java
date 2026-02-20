package com.operator.common.dto.operator;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Operator Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorResponse {

    private Long id;
    private String name;
    private String description;
    private com.operator.common.enums.LanguageType language;
    private com.operator.common.enums.OperatorStatus status;
    private String version;
    private String codeFilePath;
    private String code;  // Operator implementation code (stored in version field for now)
    private String fileName;
    private Long fileSize;
    private List<String> tags;
    private Boolean isPublic;
    private Integer downloadsCount;
    private Boolean featured;
    private List<ParameterResponse> parameters;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String operatorCode;
    private String objectCode;
    private String dataFormat;
    private String generator;
}
