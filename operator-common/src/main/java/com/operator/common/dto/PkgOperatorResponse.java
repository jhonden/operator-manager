package com.operator.common.dto;

import com.operator.common.enums.LanguageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Package Operator Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PkgOperatorResponse {

    private Long id;
    private Long operatorId;
    private String operatorName;
    private LanguageType operatorLanguage;
    private Long versionId;
    private String versionNumber;
    private Integer orderIndex;
    private String parameterMapping;
    private Boolean enabled;
    private String notes;
    private LocalDateTime createdAt;
}
