package com.operator.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Add/Update Package Operator Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PkgOperatorRequest {

    @NotNull(message = "Operator ID is required")
    private Long operatorId;

    @NotNull(message = "Version ID is required")
    private Long versionId;

    @Builder.Default
    private Integer orderIndex = 0;

    private String parameterMapping; // JSON format

    @Builder.Default
    private Boolean enabled = true;

    private String notes;
}
