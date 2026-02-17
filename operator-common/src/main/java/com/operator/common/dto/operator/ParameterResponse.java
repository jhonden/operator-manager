package com.operator.common.dto.operator;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Parameter Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterResponse {

    private Long id;
    private String name;
    private String description;
    private com.operator.common.enums.ParameterType parameterType;
    private com.operator.common.enums.IOType ioType;
    private Boolean isRequired;
    private String defaultValue;
    private String validationRules;
    private Integer orderIndex;
    private LocalDateTime createdAt;
}
