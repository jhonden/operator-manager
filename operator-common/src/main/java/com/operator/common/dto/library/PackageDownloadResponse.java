package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算子包下载响应 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageDownloadResponse {
    /**
     * 算子包名称
     */
    private String packageName;

    /**
     * ZIP 压缩包字节数组
     */
    private byte[] zipBytes;
}
