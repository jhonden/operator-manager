package com.operator.api.controller;

import com.operator.common.dto.operator.*;
import com.operator.common.enums.LanguageType;
import com.operator.common.enums.OperatorStatus;
import com.operator.common.utils.ApiResponse;
import com.operator.common.utils.PageResponse;
import com.operator.common.validation.ValidationGroups;
import com.operator.core.operator.repository.OperatorRepository;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.service.operator.OperatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

/**
 * Operator Controller
 *
 * Handles operator management operations including CRUD, search, filtering,
 * code upload, parameter management, and status updates.
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/operators")
@RequiredArgsConstructor
@Tag(name = "Operators", description = "Operator management APIs")
public class OperatorController {

    private final OperatorService operatorService;
    private final OperatorRepository operatorRepository;

    /**
     * Create a new operator
     */
    @PostMapping
    @Operation(summary = "Create operator", description = "Create a new operator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OperatorResponse>> createOperator(
            @Valid @RequestBody OperatorRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("=== CONTROLLER: Creating operator: {} by user: {}", request.getName(), userPrincipal.getUsername());
        log.info("=== CONTROLLER: Request parameters count: {}, code present: {}, code length: {}",
                request.getParameters() != null ? request.getParameters().size() : 0,
                request.getCode() != null,
                request.getCode() != null ? request.getCode().length() : 0);
        log.info("=== CONTROLLER: Business logic present: {}, business logic length: {}",
                request.getBusinessLogic() != null,
                request.getBusinessLogic() != null ? request.getBusinessLogic().length() : 0);

        OperatorResponse response = operatorService.createOperator(request, userPrincipal.getUsername());

        log.info("=== CONTROLLER: Response code present: {}, code length: {}",
                response.getCode() != null,
                response.getCode() != null ? response.getCode().length() : 0);
        log.info("=== CONTROLLER: Response business logic present: {}, business logic length: {}",
                response.getBusinessLogic() != null,
                response.getBusinessLogic() != null ? response.getBusinessLogic().length() : 0);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Operator created successfully", response));
    }

    /**
     * Get operator by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get operator", description = "Get operator details by ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OperatorResponse>> getOperator(
            @Parameter(description = "Operator ID") @PathVariable Long id) {
        log.debug("Getting operator: {}", id);

        OperatorResponse response = operatorService.getOperatorById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update an existing operator
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update operator", description = "Update operator details")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OperatorResponse>> updateOperator(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @Validated(ValidationGroups.Publish.class) @RequestBody OperatorRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating operator: {} by user: {}", id, userPrincipal.getUsername());

        OperatorResponse response = operatorService.updateOperator(id, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Operator updated successfully", response));
    }

    /**
     * Delete an operator
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete operator", description = "Delete an operator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteOperator(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Deleting operator: {} by user: {}", id, userPrincipal.getUsername());

        operatorService.deleteOperator(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Operator deleted successfully"));
    }

    /**
     * Get all operators with pagination and filters
     * Supports filtering by: language, status, category (categoryId), and keyword search
     */
    @GetMapping
    @Operation(summary = "Get all operators", description = "Get all operators with pagination and optional filters")
    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PageResponse<OperatorResponse>>> getAllOperators(
            @Parameter(description = "Page number") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "20") int size,
            @Parameter(description = "Filter by language") @RequestParam(value = "language", required = false) String language,
            @Parameter(description = "Filter by status") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Filter by category ID") @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "Search keyword for name and description") @RequestParam(value = "keyword", required = false) String keyword) {

        log.debug("Getting operators - page: {}, size: {}, language: {}, status: {}, categoryId: {}, keyword: {}",
                page, size, language, status, categoryId, keyword);

        Pageable pageable = PageRequest.of(page, size);

        // Start with all operators
        List<com.operator.core.operator.domain.Operator> allOperators = operatorRepository.findAllWithAssociations();

        // Apply all filters - they should work together, not mutually exclusive
        List<com.operator.core.operator.domain.Operator> filteredOperators = new ArrayList<>(allOperators);

        // Filter by language
        if (language != null && !language.isEmpty()) {
            LanguageType langFilter = LanguageType.valueOf(language);
            filteredOperators = filteredOperators.stream()
                    .filter(op -> op.getLanguage() == langFilter)
                    .toList();
        }

        // Filter by status
        if (status != null && !status.isEmpty()) {
            OperatorStatus statusFilter = OperatorStatus.valueOf(status);
            filteredOperators = filteredOperators.stream()
                    .filter(op -> op.getStatus() == statusFilter)
                    .toList();
        }

        // Filter by keyword (search in name and description)
        if (keyword != null && !keyword.isEmpty()) {
            String lowerKeyword = keyword.toLowerCase();
            filteredOperators = filteredOperators.stream()
                    .filter(op -> (op.getName() != null && op.getName().toLowerCase().contains(lowerKeyword)) ||
                                 (op.getDescription() != null && op.getDescription().toLowerCase().contains(lowerKeyword)))
                    .toList();
        }

        // Apply pagination to filtered results
        int start = Math.min((int) pageable.getOffset(), filteredOperators.size());
        int end = Math.min((start + pageable.getPageSize()), filteredOperators.size());
        List<com.operator.core.operator.domain.Operator> paginatedOperators = filteredOperators.subList(start, end);

        Page<com.operator.core.operator.domain.Operator> operators = new PageImpl<>(
                paginatedOperators, pageable, filteredOperators.size());

        // Convert domain objects to DTOs
        List<OperatorResponse> content = operators.getContent().stream()
                .map(this::convertToResponse)
                .toList();

        PageResponse<OperatorResponse> response = PageResponse.<OperatorResponse>builder()
                .content(content)
                .currentPage(operators.getNumber())
                .pageSize(operators.getSize())
                .totalElements(operators.getTotalElements())
                .totalPages(operators.getTotalPages())
                .first(operators.isFirst())
                .last(operators.isLast())
                .empty(operators.isEmpty())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Search operators with advanced filters
     */
    @GetMapping("/search")
    @Operation(summary = "Search operators", description = "Search operators with advanced filters")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<OperatorResponse>>> searchOperators(
            @Parameter(description = "Search keyword") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "Category ID") @RequestParam(value = "categoryId", required = false) Long categoryId,
            @Parameter(description = "Language") @RequestParam(value = "language", required = false) String language,
            @Parameter(description = "Status") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "Tags") @RequestParam(value = "tags", required = false) String tags,
            @Parameter(description = "Is public") @RequestParam(value = "isPublic", required = false) Boolean isPublic,
            @Parameter(description = "Featured") @RequestParam(value = "featured", required = false) Boolean featured,
            @Parameter(description = "Sort by") @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort order") @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
            @Parameter(description = "Page number") @RequestParam(value = "page", defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(value = "size", defaultValue = "20") int size) {

        log.debug("Searching operators - keyword: {}, categoryId: {}, language: {}, status: {}, page: {}, size: {}",
                keyword, categoryId, language, status, page, size);

        OperatorSearchRequest request = OperatorSearchRequest.builder()
                .keyword(keyword)
                .categoryId(categoryId)
                .language(language)
                .status(status)
                .tags(tags)
                .isPublic(isPublic)
                .featured(featured)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();

        PageResponse<OperatorResponse> response = operatorService.searchOperators(request);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get operators by category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get operators by category", description = "Get all operators in a category")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OperatorResponse>>> getOperatorsByCategory(
            @Parameter(description = "Category ID") @PathVariable Long categoryId) {
        log.debug("Getting operators for category: {}", categoryId);

        List<OperatorResponse> operators = operatorService.getOperatorsByCategory(categoryId);

        return ResponseEntity.ok(ApiResponse.success(operators));
    }

    /**
     * Get operators by creator
     */
    @GetMapping("/creator/{username}")
    @Operation(summary = "Get operators by creator", description = "Get all operators created by a user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<OperatorResponse>>> getOperatorsByCreator(
            @Parameter(description = "Creator username") @PathVariable String username) {
        log.debug("Getting operators for creator: {}", username);

        List<OperatorResponse> operators = operatorService.getOperatorsByCreator(username);

        return ResponseEntity.ok(ApiResponse.success(operators));
    }

    /**
     * Upload operator code file
     */
    @PostMapping("/{id}/code")
    @Operation(summary = "Upload code", description = "Upload code file for an operator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OperatorResponse>> uploadCodeFile(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @Parameter(description = "Code file") @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Uploading code for operator: {} by user: {}", id, userPrincipal.getUsername());

        OperatorResponse response = operatorService.uploadCodeFile(id, file, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Code uploaded successfully", response));
    }

    /**
     * Update operator status
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Update status", description = "Update operator status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OperatorResponse>> updateOperatorStatus(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @Parameter(description = "New status") @RequestParam("status") String status,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating status for operator: {} to: {} by user: {}", id, status, userPrincipal.getUsername());

        OperatorResponse response = operatorService.updateOperatorStatus(id, status, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Status updated successfully", response));
    }

    /**
     * Add parameter to operator
     */
    @PostMapping("/{id}/parameters")
    @Operation(summary = "Add parameter", description = "Add a parameter to an operator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ParameterResponse>> addParameter(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @Valid @RequestBody ParameterRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Adding parameter to operator: {} by user: {}", id, userPrincipal.getUsername());

        ParameterResponse response = operatorService.addParameter(id, request, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Parameter added successfully", response));
    }

    /**
     * Update operator parameter
     */
    @PutMapping("/{id}/parameters/{parameterId}")
    @Operation(summary = "Update parameter", description = "Update a parameter of an operator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ParameterResponse>> updateParameter(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @Parameter(description = "Parameter ID") @PathVariable Long parameterId,
            @Valid @RequestBody ParameterRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Updating parameter: {} for operator: {} by user: {}", parameterId, id, userPrincipal.getUsername());

        ParameterResponse response = operatorService.updateParameter(id, parameterId, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Parameter updated successfully", response));
    }

    /**
     * Delete operator parameter
     */
    @DeleteMapping("/{id}/parameters/{parameterId}")
    @Operation(summary = "Delete parameter", description = "Delete a parameter from an operator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteParameter(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @Parameter(description = "Parameter ID") @PathVariable Long parameterId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Deleting parameter: {} for operator: {} by user: {}", parameterId, id, userPrincipal.getUsername());

        operatorService.deleteParameter(id, parameterId, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Parameter deleted successfully"));
    }

    /**
     * Toggle featured status
     */
    @PostMapping("/{id}/toggle-featured")
    @Operation(summary = "Toggle featured", description = "Toggle featured status of an operator")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<OperatorResponse>> toggleFeatured(
            @Parameter(description = "Operator ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("Toggling featured status for operator: {} by user: {}", id, userPrincipal.getUsername());

        OperatorResponse response = operatorService.toggleFeatured(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("Featured status toggled successfully", response));
    }

    /**
     * Increment download count
     */
    @PostMapping("/{id}/download")
    @Operation(summary = "Increment download count", description = "Increment the download count of an operator")
    public ResponseEntity<ApiResponse<Void>> incrementDownloadCount(
            @Parameter(description = "Operator ID") @PathVariable Long id) {
        log.debug("Incrementing download count for operator: {}", id);

        operatorService.incrementDownloadCount(id);

        return ResponseEntity.ok(ApiResponse.success("Download count incremented"));
    }

    /**
     * Convert Operator domain object to OperatorResponse DTO
     * This is a helper method used within getAllOperators for manual conversion
     */
    private OperatorResponse convertToResponse(com.operator.core.operator.domain.Operator operator) {
        log.debug("=== CONTROLLER convertToResponse: operator ID: {}, code present: {}, code length: {}",
                operator.getId(),
                operator.getCode() != null,
                operator.getCode() != null ? operator.getCode().length() : 0);
        log.debug("=== CONTROLLER convertToResponse: business logic present: {}, business logic length: {}",
                operator.getBusinessLogic() != null,
                operator.getBusinessLogic() != null ? operator.getBusinessLogic().length() : 0);

        return OperatorResponse.builder()
                .id(operator.getId())
                .name(operator.getName())
                .description(operator.getDescription())
                .language(convertToDtoLanguageType(operator.getLanguage()))
                .status(convertToDtoOperatorStatus(operator.getStatus()))
                .version(operator.getVersion())
                .code(operator.getCode())  // 添加缺失的 code 字段
                .codeFilePath(operator.getCodeFilePath())
                .fileName(operator.getFileName())
                .fileSize(operator.getFileSize())
                .isPublic(operator.getIsPublic())
                .featured(operator.getFeatured())
                .createdBy(operator.getCreatedBy())
                .createdAt(operator.getCreatedAt())
                .updatedAt(operator.getUpdatedAt())
                .tags(new ArrayList<>()) // Empty list since tags are removed
                .operatorCode(operator.getOperatorCode())
                .objectCode(operator.getObjectCode())
                .dataFormat(operator.getDataFormat())
                .generator(operator.getGenerator())
                .businessLogic(operator.getBusinessLogic())
                .build();
    }

    // Helper methods for enum conversion

    private LanguageType convertToDtoLanguageType(LanguageType entityType) {
        if (entityType == null) {
            return LanguageType.JAVA;
        }
        return entityType;
    }

    private OperatorStatus convertToDtoOperatorStatus(OperatorStatus entityType) {
        if (entityType == null) {
            return OperatorStatus.DRAFT;
        }
        return entityType;
    }
}
