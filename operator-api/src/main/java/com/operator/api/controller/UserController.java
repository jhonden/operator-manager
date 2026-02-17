package com.operator.api.controller;

import com.operator.infrastructure.security.UserPrincipal;
import com.operator.common.utils.ApiResponse;
import com.operator.common.dto.UserInfo;
import com.operator.common.dto.user.UpdateUserRequest;
import com.operator.service.user.UserService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User Controller
 *
 * Handles user profile and management operations
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    private final UserService userService;

    /**
     * Get current user profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get current authenticated user profile")
    public ResponseEntity<ApiResponse<UserInfo>> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting current user: {}", userPrincipal.getUsername());

        UserInfo user = userService.getUserProfile(userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Update current user profile
     */
    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update current user profile")
    public ResponseEntity<ApiResponse<UserInfo>> updateProfile(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating profile for user: {}", userPrincipal.getUsername());

        UserInfo user = userService.updateUserProfile(userPrincipal.getUsername(), request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", user));
    }

    /**
     * Get user by ID (admin only)
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user details by ID (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserInfo>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        log.debug("Getting user: {}", id);

        UserInfo user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * Get all users (admin only)
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserInfo>>> getAllUsers() {
        log.debug("Getting all users");

        List<UserInfo> users = userService.getAllUsers();

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Search users (admin only)
     */
    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by keyword (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserInfo>>> searchUsers(
            @Parameter(description = "Search keyword") @RequestParam String keyword) {
        log.debug("Searching users with keyword: {}", keyword);

        List<UserInfo> users = userService.searchUsers(keyword);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * Update user role (admin only)
     */
    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Update user role (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "New role") @RequestParam String role,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating role for user: {} to {} by admin: {}", id, role, userPrincipal.getUsername());

        userService.updateUserRole(id, role);

        return ResponseEntity.ok(ApiResponse.success("User role updated successfully"));
    }

    /**
     * Update user status (admin only)
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "Update user status", description = "Update user status (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating status for user: {} to {} by admin: {}", id, status, userPrincipal.getUsername());

        userService.updateUserStatus(id, status);

        return ResponseEntity.ok(ApiResponse.success("User status updated successfully"));
    }

    /**
     * Delete user (admin only)
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user (admin only)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Deleting user: {} by admin: {}", id, userPrincipal.getUsername());

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success("User deleted successfully"));
    }
}
