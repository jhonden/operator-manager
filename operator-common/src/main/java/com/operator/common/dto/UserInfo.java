package com.operator.common.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * User Info DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatarUrl;
    private String phone;
    private String department;
    private com.operator.common.enums.UserRole role;
    private com.operator.common.enums.UserStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
