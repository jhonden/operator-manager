package com.operator.api.controller;

import com.operator.infrastructure.security.UserPrincipal;
import com.operator.common.utils.ApiResponse;
import com.operator.common.dto.version.VersionRequest;
import com.operator.common.dto.version.VersionResponse;
import com.operator.service.version.VersionService;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Version Controller
 *
 * Handles version management operations
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/versions")
@RequiredArgsConstructor
@Tag(name = "Versions", description = "Version management APIs")
public class VersionController {

    private final VersionService versionService;

    /**
     * Create a new version
     */
    @PostMapping
    @Operation(summary = "Create version", description = "Create a new version")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<VersionResponse>> createVersion(
            @Valid @RequestBody VersionRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Creating version by user: {}", userPrincipal.getUsername());

        VersionResponse response = versionService.createVersion(request, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Version created successfully", response));
    }

    /**
     * Get version by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get version", description = "Get version details by ID")
    public ResponseEntity<ApiResponse<VersionResponse>> getVersion(
            @Parameter(description = "Version ID") @PathVariable Long id) {
        log.debug("Getting version: {}", id);

        VersionResponse response = versionService.getVersionById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update version status
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update version status", description = "Update version status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VersionResponse>> updateVersionStatus(
            @Parameter(description = "Version ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating version status: {} to {} by user: {}", id, status, userPrincipal.getUsername());

        VersionResponse response = versionService.updateVersionStatus(id, status, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Version status updated successfully", response));
    }

    /**
     * Release version
     */
    @PostMapping("/{id}/release")
    @Operation(summary = "Release version", description = "Mark version as released")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<VersionResponse>> releaseVersion(
            @Parameter(description = "Version ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Releasing version: {} by user: {}", id, userPrincipal.getUsername());

        VersionResponse response = versionService.releaseVersion(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Version released successfully", response));
    }

    /**
     * Compare two versions
     */
    @GetMapping("/compare")
    @Operation(summary = "Compare versions", description = "Compare two versions")
    public ResponseEntity<ApiResponse<com.operator.service.version.VersionService.VersionComparison>> compareVersions(
            @Parameter(description = "First version ID") @RequestParam Long versionId1,
            @Parameter(description = "Second version ID") @RequestParam Long versionId2) {
        log.debug("Comparing versions: {} and {}", versionId1, versionId2);

        com.operator.service.version.VersionService.VersionComparison comparison = versionService.compareVersions(versionId1, versionId2);

        return ResponseEntity.ok(ApiResponse.success(comparison));
    }

    /**
     * Delete version
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete version", description = "Delete a version")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteVersion(
            @Parameter(description = "Version ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Deleting version: {} by user: {}", id, userPrincipal.getUsername());

        versionService.deleteVersion(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Version deleted successfully"));
    }
}
