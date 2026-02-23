package com.operator.api.controller;

import com.operator.common.dto.pkg.PackageImportResponse;
import com.operator.common.utils.ApiResponse;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.service.library.PackageImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 算子包导入 Controller
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/packages")
@RequiredArgsConstructor
@Tag(name = "Package Import", description = "算子包导入 APIs")
public class PackageImportController {

    private final PackageImportService importService;

    /**
     * 导入算子包
     *
     * @param file ZIP 压缩包文件
     * @param userPrincipal 用户认证信息
     * @return 导入结果
     */
    @PostMapping("/import")
    @Operation(summary = "导入算子包", description = "从 ZIP 文件导入算子包")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackageImportResponse>> importPackage(
            @Parameter(description = "ZIP 压缩包文件", required = true)
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("导入算子包：fileName={}, user={}", file.getOriginalFilename(), userPrincipal.getUsername());

        try {
            // 验证文件类型
            String filename = file.getOriginalFilename();
            if (filename == null || !filename.toLowerCase().endsWith(".zip")) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("只支持 ZIP 格式文件"));
            }

            // 验证文件大小（限制 10MB）
            long maxSize = 10 * 1024 * 1024; // 10MB
            if (file.getSize() > maxSize) {
                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("文件大小超过限制（最大 10MB）"));
            }

            // 导入算子包
            PackageImportResponse response = importService.importPackage(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    userPrincipal.getUsername());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success("算子包导入成功", response));

        } catch (Exception e) {
            log.error("导入算子包失败", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("导入算子包失败：" + e.getMessage()));
        }
    }
}
