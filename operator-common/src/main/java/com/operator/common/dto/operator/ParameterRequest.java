package com.operator.common.dto.operator;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Parameter Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterRequest {

    @NotBlank(message = "Parameter name is required")
    private String name;

    private String description;

    @NotNull(message = "Parameter type is required")
    private com.operator.common.enums.ParameterType parameterType;

    @NotNull(message = "IO type is required")
    private com.operator.common.enums.IOType ioType;

    @Builder.Default
    private Boolean isRequired = false;

    private String defaultValue;

    private String validationRules; // JSON format

    @Builder.Default
    private Integer orderIndex = 0;
}
