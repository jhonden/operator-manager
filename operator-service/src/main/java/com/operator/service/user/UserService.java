package com.operator.service.user;

import com.operator.common.dto.UserInfo;
import com.operator.common.dto.user.UpdateUserRequest;

import java.util.List;

/**
 * User Service Interface
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface UserService {

    /**
     * Get user by ID
     */
    UserInfo getUserById(Long id);

    /**
     * Get user profile (current user)
     */
    UserInfo getUserProfile(String username);

    /**
     * Update user profile
     */
    UserInfo updateUserProfile(String username, UpdateUserRequest request);

    /**
     * Get all users
     */
    List<UserInfo> getAllUsers();

    /**
     * Update user role (admin only)
     */
    void updateUserRole(Long userId, String role);

    /**
     * Update user status (admin only)
     */
    void updateUserStatus(Long userId, String status);

    /**
     * Delete user (admin only)
     */
    void deleteUser(Long userId);

    /**
     * Search users
     */
    List<UserInfo> searchUsers(String keyword);
}
