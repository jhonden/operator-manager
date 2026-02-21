package com.operator.api.controller;

import com.operator.common.dto.library.PackagePreviewResponse;
import com.operator.common.utils.ApiResponse;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.service.library.PackagePreviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 算子包预览 Controller
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/packages")
@RequiredArgsConstructor
@Tag(name = "Package Preview", description = "算子包预览 APIs")
public class PackagePreviewController {

    private final PackagePreviewService packagePreviewService;

    /**
     * 获取打包预览
     */
    @GetMapping("/{id}/preview")
    @Operation(summary = "获取打包预览", description = "生成算子包的打包结构预览")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PackagePreviewResponse>> generatePreview(
            @Parameter(description = "算子包ID") @PathVariable Long id,
            @Parameter(description = "打包模板") @RequestParam(value = "template", defaultValue = "legacy") String template,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("生成打包预览：packageId={}, template={}", id, template);

        PackagePreviewResponse response = packagePreviewService.generatePreview(id, template);

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
