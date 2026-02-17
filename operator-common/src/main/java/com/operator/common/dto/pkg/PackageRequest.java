package com.operator.common.dto.pkg;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Create/Update Operator Package Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 255, message = "Name must be between 3 and 255 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotBlank(message = "Business scenario is required")
    @Size(max = 255, message = "Business scenario must not exceed 255 characters")
    private String businessScenario;

    private com.operator.common.enums.PackageStatus status;

    private String icon;

    private List<String> tags;

    @Size(max = 50, message = "Version must not exceed 50 characters")
    private String version;

    @Builder.Default
    private Boolean isPublic = false;
}
