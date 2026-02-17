package com.operator.api.controller;

import com.operator.infrastructure.security.JwtTokenProvider;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.common.utils.ApiResponse;
import com.operator.common.dto.AuthResponse;
import com.operator.common.dto.LoginRequest;
import com.operator.common.dto.RegisterRequest;
import com.operator.common.dto.RefreshTokenRequest;
import com.operator.common.dto.UserInfo;
import com.operator.common.dto.ChangePasswordRequest;
import com.operator.service.security.AuthService;
import java.time.LocalDateTime;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller
 *
 * Handles user registration, login, token refresh, etc.
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;

    /**
     * User login endpoint
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = tokenProvider.generateToken(userPrincipal);
        String refreshToken = tokenProvider.generateRefreshToken(userPrincipal);

        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .fullName(userPrincipal.getFullName())
                .role(userPrincipal.getRole())
                .status(userPrincipal.getStatus())
                .lastLoginAt(LocalDateTime.now())
                .build();

        AuthResponse response = AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(86400000L) // 24 hours
                .user(userInfo)
                .build();

        // Update last login
        authService.updateLastLogin(userPrincipal.getId());

        log.info("User logged in successfully: {}", request.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    /**
     * User registration endpoint
     */
    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for user: {}", request.getUsername());

        AuthResponse response = authService.registerUser(request);

        log.info("User registered successfully: {}", request.getUsername());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    /**
     * Token refresh endpoint
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Token refresh request");

        AuthResponse response = authService.refreshToken(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    /**
     * Get current user info
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user information")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        UserInfo userInfo = authService.getUserInfo(userPrincipal.getId());
        return ResponseEntity.ok(ApiResponse.success(userInfo));
    }

    /**
     * Change password
     */
    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Change user password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChangePasswordRequest request) {
        log.info("Password change request for user: {}", userPrincipal.getUsername());

        authService.changePassword(userPrincipal.getId(), request.getOldPassword(), request.getNewPassword());

        log.info("Password changed successfully for user: {}", userPrincipal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    /**
     * Logout endpoint (client-side token removal)
     */
    @PostMapping("/logout")
    @Operation(summary = "Logout user", description = "Logout user (client should remove token)")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String username = null;
        Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
            username = ((UserPrincipal) auth.getPrincipal()).getUsername();
        }

        log.info("User logged out: {}", username);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();

        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
}
