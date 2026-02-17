package com.operator.common.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update User Profile Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 255, message = "Full name must not exceed 255 characters")
    private String fullName;

    @Email(message = "Email must be valid")
    private String email;

    private String avatarUrl;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    @Size(max = 100, message = "Department must not exceed 100 characters")
    private String department;
}
