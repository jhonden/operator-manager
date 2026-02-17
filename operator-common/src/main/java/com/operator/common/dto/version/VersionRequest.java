package com.operator.common.dto.version;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Version Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionRequest {

    @NotBlank(message = "Version number is required")
    @Size(max = 50, message = "Version number must not exceed 50 characters")
    private String versionNumber;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    private String changelog;
}
