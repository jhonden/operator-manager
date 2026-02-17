package com.operator.api.controller;

import com.operator.common.dto.pkg.PackageResponse;
import com.operator.common.dto.pkg.PackageRequest;
import com.operator.common.dto.pkg.PackageOperatorRequest;
import com.operator.common.dto.pkg.PackageOperatorResponse;
import com.operator.common.dto.pkg.ReorderOperatorsRequest;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.common.utils.ApiResponse;
import com.operator.service.pkg.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Operator Package Controller
 *
 * Handles operator package CRUD operations
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/packages")
@RequiredArgsConstructor
@Tag(name = "Operator Packages", description = "Operator package management APIs")
public class PackageController {

    private final PackageService packageService;

    /**
     * Create a new operator package
     */
    @PostMapping
    @Operation(summary = "Create package", description = "Create a new operator package")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<ApiResponse<PackageResponse>> createPackage(
            @Valid @RequestBody PackageRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Creating package: {} by user: {}", request.getName(), userPrincipal.getUsername());

        PackageResponse response = packageService.createPackage(request, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Package created successfully", response));
    }

    /**
     * Get package by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get package", description = "Get package details by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackageResponse>> getPackage(
            @Parameter(description = "Package ID") @PathVariable Long id) {
        log.debug("Getting package: {}", id);

        PackageResponse response = packageService.getPackageById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update package
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update package", description = "Update package details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackage(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @Valid @RequestBody PackageRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating package: {} by user: {}", id, userPrincipal.getUsername());

        PackageResponse response = packageService.updatePackage(id, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Package updated successfully", response));
    }

    /**
     * Delete package
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete package", description = "Delete an operator package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deletePackage(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Deleting package: {} by user: {}", id, userPrincipal.getUsername());

        packageService.deletePackage(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Package deleted successfully"));
    }

    /**
     * Get all packages
     */
    @GetMapping
    @Operation(summary = "List packages", description = "Get all packages with pagination")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.operator.common.utils.PageResponse<PackageResponse>>> getAllPackages(
            @Parameter(description = "Page number (default: 0)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(value = "size", defaultValue = "20") int size) {
        log.debug("Getting packages - page: {}, size: {}", page, size);

        com.operator.common.utils.PageResponse<PackageResponse> response = packageService.getAllPackages(page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search packages
     */
    @GetMapping("/search")
    @Operation(summary = "Search packages", description = "Search packages by keyword")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<com.operator.common.utils.PageResponse<PackageResponse>>> searchPackages(
            @Parameter(description = "Search keyword") @RequestParam(value = "keyword") String keyword,
            @Parameter(description = "Page number (default: 0)") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 20)") @RequestParam(value = "size", defaultValue = "20") int size) {
        log.info("Searching packages with keyword: {}", keyword);

        com.operator.common.utils.PageResponse<PackageResponse> response = packageService.searchPackages(keyword, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get packages by current user
     */
    @GetMapping("/my-packages")
    @Operation(summary = "Get my packages", description = "Get all packages created by current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> getMyPackages(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.debug("Getting packages for user: {}", userPrincipal.getUsername());

        List<PackageResponse> packages = packageService.getPackagesByCreator(userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success(packages));
    }

    /**
     * Add operator to package
     */
    @PostMapping("/{id}/operators")
    @Operation(summary = "Add operator", description = "Add operator to package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackageOperatorResponse>> addOperator(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @Valid @RequestBody PackageOperatorRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Adding operator {} to package: {} by user: {}", request.getOperatorId(), id, userPrincipal.getUsername());

        PackageOperatorResponse response = packageService.addOperator(id, request, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Operator added to package successfully", response));
    }

    /**
     * Update package operator
     */
    @PutMapping("/{id}/operators/{packageOperatorId}")
    @Operation(summary = "Update package operator", description = "Update operator in package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackageOperatorResponse>> updatePackageOperator(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @Parameter(description = "Package Operator ID") @PathVariable Long packageOperatorId,
            @Valid @RequestBody PackageOperatorRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating package operator: {} for package: {} by user: {}", packageOperatorId, id, userPrincipal.getUsername());

        PackageOperatorResponse response = packageService.updatePackageOperator(id, packageOperatorId, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Package operator updated successfully", response));
    }

    /**
     * Remove operator from package
     */
    @DeleteMapping("/{id}/operators/{packageOperatorId}")
    @Operation(summary = "Remove operator", description = "Remove operator from package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> removeOperator(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @Parameter(description = "Package Operator ID") @PathVariable Long packageOperatorId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Removing operator: {} from package: {} by user: {}", packageOperatorId, id, userPrincipal.getUsername());

        packageService.removeOperator(id, packageOperatorId, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Operator removed from package successfully"));
    }

    /**
     * Get package operators
     */
    @GetMapping("/{id}/operators")
    @Operation(summary = "Get package operators", description = "Get all operators in package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<PackageOperatorResponse>>> getPackageOperators(
            @Parameter(description = "Package ID") @PathVariable Long id) {
        log.debug("Getting operators for package: {}", id);

        List<PackageOperatorResponse> operators = packageService.getPackageOperators(id);

        return ResponseEntity.ok(ApiResponse.success(operators));
    }

    /**
     * Reorder operators in package
     */
    @PostMapping("/{id}/operators/reorder")
    @Operation(summary = "Reorder operators", description = "Reorder operators in package")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> reorderOperators(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @Valid @RequestBody ReorderOperatorsRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Reordering operators in package: {} by user: {}", id, userPrincipal.getUsername());

        packageService.reorderOperators(id, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Operators reordered successfully"));
    }

    /**
     * Update package status
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update package status", description = "Update package status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackageResponse>> updatePackageStatus(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating package status: {} to {} by user: {}", id, status, userPrincipal.getUsername());

        PackageResponse response = packageService.updatePackageStatus(id, status, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Package status updated successfully", response));
    }

    /**
     * Toggle package featured status
     */
    @PatchMapping("/{id}/featured")
    @Operation(summary = "Toggle featured", description = "Toggle package featured status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackageResponse>> toggleFeatured(
            @Parameter(description = "Package ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Toggling featured status for package: {} by user: {}", id, userPrincipal.getUsername());

        PackageResponse response = packageService.toggleFeatured(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Package featured status updated", response));
    }

    /**
     * Download package
     */
    @PostMapping("/{id}/download")
    @Operation(summary = "Download package", description = "Increment package download count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> download(
            @Parameter(description = "Package ID") @PathVariable Long id) {
        log.info("Incrementing download count for package: {}", id);

        packageService.incrementDownloadCount(id);

        return ResponseEntity.ok(ApiResponse.success("Download count incremented"));
    }
}
