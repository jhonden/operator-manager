package com.operator.api.controller;

import com.operator.common.dto.library.PackageDownloadResponse;
import com.operator.common.utils.ApiResponse;
import com.operator.infrastructure.security.UserPrincipal;
import com.operator.service.library.PackageBuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 算子包构建 Controller
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/v1/packages")
@RequiredArgsConstructor
@Tag(name = "Package Build", description = "算子包构建 APIs")
public class PackageBuildController {

    private final PackageBuildService packageBuildService;

    /**
     * 下载算子包
     *
     * @param id 算子包 ID
     * @param userPrincipal 用户认证信息
     * @return ZIP 压缩包字节数组
     */
    @GetMapping("/{id}/download")
    @Operation(summary = "下载算子包", description = "生成并下载算子包的压缩包")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<byte[]> downloadPackage(
            @Parameter(description = "算子包ID") @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        log.info("下载算子包：packageId={}, user={}", id, userPrincipal.getUsername());

        PackageDownloadResponse downloadResponse = packageBuildService.buildPackage(id);

        // 设置下载响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/zip"));
        headers.setContentDispositionFormData("attachment", "operator_package_" + downloadResponse.getPackageName() + ".zip");

        return ResponseEntity.ok()
                .headers(headers)
                .body(downloadResponse.getZipBytes());
    }
}
