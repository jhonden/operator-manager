package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共库打包路径配置请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryPathConfigRequest {

    /**
     * 公共库ID
     */
    private Long libraryId;

    /**
     * 使用的版本
     */
    private String version;

    /**
     * 是否使用自定义路径
     */
    private Boolean useCustomPath;

    /**
     * 自定义打包路径（支持变量：${libraryName}、${libraryVersion}、${fileName}、${fileExt} 等）
     */
    private String customPackagePath;

    /**
     * 顺序索引
     */
    private Integer orderIndex;
}
