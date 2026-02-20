package com.operator.common.dto.operator;


import com.operator.common.validation.OperatorCode;
import com.operator.common.validation.ValidationGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create/Update Operator Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorRequest {

    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private com.operator.common.enums.LanguageType language;

    private com.operator.common.enums.OperatorStatus status;

    private Long categoryId;

    private List<String> tags;

    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    @Builder.Default
    private Boolean isPublic = false;

    private String code;  // Operator implementation code

    private List<ParameterRequest> parameters;

    @NotBlank(message = "Operator code is required")
    @OperatorCode
    private String operatorCode;

    @NotBlank(message = "Object code is required")
    @OperatorCode
    private String objectCode;

    private String dataFormat;

    private String generator;

    private String businessLogic;
}
