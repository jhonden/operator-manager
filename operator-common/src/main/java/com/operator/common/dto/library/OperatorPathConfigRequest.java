package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算子打包路径配置请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorPathConfigRequest {

    /**
     * 算子ID
     */
    private Long operatorId;

    /**
     * 是否使用自定义路径
     */
    private Boolean useCustomPath;

    /**
     * 自定义打包路径（支持变量：${operatorCode}、${fileName}、${fileExt} 等）
     */
    private String customPackagePath;

    /**
     * 顺序索引
     */
    private Integer orderIndex;
}
