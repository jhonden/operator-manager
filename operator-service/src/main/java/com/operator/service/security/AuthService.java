package com.operator.service.security;

import com.operator.common.dto.*;
import com.operator.common.enums.*;
import com.operator.common.exception.BadRequestException;
import com.operator.infrastructure.security.JwtTokenProvider;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.core.security.domain.User;
import com.operator.core.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Authentication Service
 *
 * Handles authentication business logic
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register new user
     */
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .department(request.getDepartment())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();

        user = userRepository.save(user);

        // Generate tokens
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateToken(userPrincipal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(AuthResponse.UserInfo.builder()
                        .id(userPrincipal.getId())
                        .username(userPrincipal.getUsername())
                        .email(userPrincipal.getEmail())
                        .fullName(userPrincipal.getFullName())
                        .avatarUrl(userPrincipal.getAvatarUrl())
                        .role(userPrincipal.getRole())
                        .status(userPrincipal.getStatus())
                        .lastLoginAt(LocalDateTime.now())
                        .build())
                .build();
    }

    /**
     * Refresh access token
     */
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadRequestException("Invalid refresh token");
        }

        // Get user ID from token
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Load user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Check if user is active
        if (user.getStatus() != UserStatus.ACTIVE || !user.getEnabled()) {
            throw new BadRequestException("User account is not active");
        }

        // Generate new tokens
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        String accessToken = jwtTokenProvider.generateToken(userPrincipal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .user(AuthResponse.UserInfo.builder()
                        .id(userPrincipal.getId())
                        .username(userPrincipal.getUsername())
                        .email(userPrincipal.getEmail())
                        .fullName(userPrincipal.getFullName())
                        .avatarUrl(userPrincipal.getAvatarUrl())
                        .role(userPrincipal.getRole())
                        .status(userPrincipal.getStatus())
                        .lastLoginAt(user.getLastLoginAt())
                        .build())
                .build();
    }

    /**
     * Get user info
     */
    @Transactional(readOnly = true)
    public UserInfo getUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        return UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .phone(user.getPhone())
                .department(user.getDepartment())
                .role(user.getRole())
                .status(user.getStatus())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * Change password
     */
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password changed for user: {}", user.getUsername());
    }

    /**
     * Update last login time
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
        });
    }
}
