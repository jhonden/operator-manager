package com.operator.api.controller;

import com.operator.common.dto.library.*;
import com.operator.common.utils.ApiResponse;
import com.operator.common.utils.PageResponse;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.service.library.CommonLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 公共库 Controller
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/libraries")
@RequiredArgsConstructor
@Tag(name = "Libraries", description = "公共库管理 APIs")
public class LibraryController {

    private final CommonLibraryService libraryService;

    /**
     * 创建公共库
     */
    @PostMapping
    @Operation(summary = "创建公共库", description = "创建新的公共库")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LibraryResponse>> createLibrary(
            @Valid @RequestBody LibraryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("创建公共库：name={}, version={}", request.getName(), request.getVersion());

        LibraryResponse response = libraryService.createLibrary(request, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("公共库创建成功", response));
    }

    /**
     * 更新公共库
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新公共库", description = "更新公共库信息")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LibraryResponse>> updateLibrary(
            @Parameter(description = "公共库ID") @PathVariable Long id,
            @Valid @RequestBody LibraryRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("更新公共库：id={}, name={}", id, request.getName());

        LibraryResponse response = libraryService.updateLibrary(id, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("公共库更新成功", response));
    }

    /**
     * 删除公共库
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除公共库", description = "删除指定的公共库")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteLibrary(
            @Parameter(description = "公共库ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("删除公共库：id={}", id);

        libraryService.deleteLibrary(id, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("公共库删除成功"));
    }

    /**
     * 根据ID获取公共库
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取公共库", description = "根据ID获取公共库详情")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LibraryResponse>> getLibraryById(
            @Parameter(description = "公共库ID") @PathVariable Long id) {
        log.debug("获取公共库：id={}", id);

        LibraryResponse response = libraryService.getLibraryById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 搜索公共库
     */
    @GetMapping
    @Operation(summary = "搜索公共库", description = "搜索公共库，支持分页")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LibraryResponse>>> searchLibraries(
            @Parameter(description = "搜索关键词") @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "库类型") @RequestParam(value = "libraryType", required = false) String libraryType,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") Integer size) {
        log.debug("搜索公共库：keyword={}, libraryType={}, page={}, size={}", keyword, libraryType, page, size);

        PageResponse<LibraryResponse> response = libraryService.searchLibraries(keyword, libraryType, page, size);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据类型获取公共库
     */
    @GetMapping("/type/{libraryType}")
    @Operation(summary = "按类型获取公共库", description = "根据库类型获取公共库列表")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LibraryResponse>>> getLibrariesByType(
            @Parameter(description = "库类型") @PathVariable String libraryType,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "20") Integer size) {
        log.debug("按类型获取公共库：libraryType={}, page={}, size={}", libraryType, page, size);

        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<LibraryResponse> pageResult = libraryService.getLibrariesByType(libraryType, pageable);

        PageResponse<LibraryResponse> response = PageResponse.<LibraryResponse>builder()
                .content(pageResult.getContent())
                .currentPage(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .empty(pageResult.isEmpty())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 根据分类获取公共库
     */
    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取公共库", description = "根据分类获取公共库列表")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PageResponse<LibraryResponse>>> getLibrariesByCategory(
            @Parameter(description = "分类") @PathVariable String category,
            @Parameter(description = "页码") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "20") Integer size) {
        log.debug("按分类获取公共库：category={}, page={}, size={}", category, page, size);

        Pageable pageable = PageRequest.of(page, size);
        org.springframework.data.domain.Page<LibraryResponse> pageResult = libraryService.getLibrariesByCategory(category, pageable);

        PageResponse<LibraryResponse> response = PageResponse.<LibraryResponse>builder()
                .content(pageResult.getContent())
                .currentPage(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .first(pageResult.isFirst())
                .last(pageResult.isLast())
                .empty(pageResult.isEmpty())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 创建库文件（空文件）
     */
    @PostMapping(value = "/{libraryId}/files")
    @Operation(summary = "创建库文件", description = "在公共库中创建新文件（空文件）")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<LibraryFileResponse>> createLibraryFile(
            @Parameter(description = "公共库ID") @PathVariable(name = "libraryId") Long id,
            @Valid @RequestBody LibraryFileCreateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("创建库文件：libraryId={}, fileName={}", id, request.getFileName());

        LibraryFileResponse response = libraryService.createLibraryFile(id, request, userPrincipal.getUsername());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("文件创建成功", response));
    }

    /**
     * 更新库文件名
     */
    @PutMapping(value = "/{libraryId}/files/{fileId}")
    @Operation(summary = "更新库文件名", description = "更新库文件的元数据（文件名）")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateLibraryFileName(
            @Parameter(description = "公共库ID") @PathVariable(name = "libraryId") Long id,
            @Parameter(description = "文件ID") @PathVariable(name = "fileId") Long fileId,
            @Valid @RequestBody LibraryFileRenameRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("更新库文件名：libraryId={}, fileId={}, newFileName={}", id, fileId, request.getFileName());

        libraryService.updateLibraryFileName(id, fileId, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("文件名更新成功"));
    }

    /**
     * 更新库文件内容
     */
    @PutMapping(value = "/{libraryId}/files/{fileId}/content")
    @Operation(summary = "更新库文件内容", description = "更新库文件的代码内容")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> updateLibraryFileContent(
            @Parameter(description = "公共库ID") @PathVariable(name = "libraryId") Long id,
            @Parameter(description = "文件ID") @PathVariable(name = "fileId") Long fileId,
            @Valid @RequestBody LibraryFileContentUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("更新库文件内容：libraryId={}, fileId={}", id, fileId);

        libraryService.updateLibraryFileContent(id, fileId, request, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("文件内容更新成功"));
    }

    /**
     * 删除库文件
     */
    @DeleteMapping(value = "/{libraryId}/files/{fileId}")
    @Operation(summary = "删除库文件", description = "删除公共库中的文件")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> deleteLibraryFile(
            @Parameter(description = "公共库ID") @PathVariable(name = "libraryId") Long id,
            @Parameter(description = "文件ID") @PathVariable(name = "fileId") Long fileId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("删除库文件：libraryId={}, fileId={}", id, fileId);

        libraryService.deleteLibraryFile(id, fileId, userPrincipal.getUsername());

        return ResponseEntity.ok(ApiResponse.success("文件删除成功"));
    }
}
