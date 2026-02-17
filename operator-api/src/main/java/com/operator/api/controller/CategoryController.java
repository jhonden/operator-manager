package com.operator.api.controller;

import com.operator.common.dto.pkg.CategoryResponse;
import com.operator.common.dto.category.CategoryRequest;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.common.utils.ApiResponse;
import com.operator.service.category.CategoryService;
import com.operator.common.dto.operator.OperatorResponse;
import org.springframework.http.ResponseEntity;
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
 * Category Controller
 *
 * Handles category management operations
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management APIs")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Create a new category
     */
    @PostMapping
    @Operation(summary = "Create category", description = "Create a new category")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Creating category: {} by user: {}", request.getName(), userPrincipal.getUsername());

        CategoryResponse response = categoryService.createCategory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created successfully", response));
    }

    /**
     * Get category by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get category", description = "Get category details by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Getting category: {}", id);

        CategoryResponse response = categoryService.getCategoryById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update category
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update category", description = "Update category details")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating category: {} by user: {}", id, userPrincipal.getUsername());

        CategoryResponse response = categoryService.updateCategory(id, request);

        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }

    /**
     * Delete category
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category", description = "Delete a category")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @Parameter(description = "Category ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Deleting category: {} by user: {}", id, userPrincipal.getUsername());

        categoryService.deleteCategory(id);

        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }

    /**
     * Get category tree
     */
    @GetMapping("/tree")
    @Operation(summary = "Get category tree", description = "Get all categories as a tree structure")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategoryTree() {
        log.debug("Getting category tree");

        List<CategoryResponse> categories = categoryService.getCategoryTree();

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Get root categories
     */
    @GetMapping("/root")
    @Operation(summary = "Get root categories", description = "Get all root categories (no parent)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRootCategories() {
        log.debug("Getting root categories");

        List<CategoryResponse> categories = categoryService.getRootCategories();

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Get child categories
     */
    @GetMapping("/{parentId}/children")
    @Operation(summary = "Get child categories", description = "Get all child categories of a parent")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildCategories(
            @Parameter(description = "Parent category ID") @PathVariable Long parentId) {
        log.debug("Getting child categories for parent: {}", parentId);

        List<CategoryResponse> categories = categoryService.getChildCategories(parentId);

        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Get operators in category
     */
    @GetMapping("/{id}/operators")
    @Operation(summary = "Get operators by category", description = "Get all operators in a category")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OperatorResponse>>> getOperatorsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long id) {
        log.debug("Getting operators for category: {}", id);

        List<OperatorResponse> operators = categoryService.getOperatorsByCategory(id);

        return ResponseEntity.ok(ApiResponse.success(operators));
    }
}
