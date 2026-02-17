package com.operator.infrastructure.security;

import com.operator.common.enums.UserRole;
import com.operator.common.enums.UserStatus;
import com.operator.core.security.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User Principal - implements UserDetails for Spring Security
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Long id;
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String avatarUrl;
    private UserRole role;
    private UserStatus status;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Create UserPrincipal from User entity
     */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .role(user.getRole())
                .status(user.getStatus())
                .authorities(authorities)
                .build();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.SUSPENDED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE && true; // Also check user.enabled field
    }

    /**
     * Get role as enum
     */
    public UserRole getRole() {
        return role;
    }

    /**
     * Get role as string for Spring Security
     */
    public String getRoleAsString() {
        return role != null ? role.name() : "USER";
    }

    /**
     * Get status as enum
     */
    public UserStatus getStatus() {
        return status;
    }
}
