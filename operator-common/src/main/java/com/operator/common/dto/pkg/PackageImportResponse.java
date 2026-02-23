package com.operator.common.dto.pkg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算子包导入响应 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageImportResponse {
    /**
     * 新建的算子包 ID
     */
    private Long id;

    /**
     * 算子包名称（可能已加后缀）
     */
    private String name;

    /**
     * 更新的算子数量
     */
    private Integer operatorsUpdated;

    /**
     * 新建的算子数量
     */
    private Integer operatorsCreated;

    /**
     * 更新的公共库数量
     */
    private Integer librariesUpdated;

    /**
     * 新建的公共库数量
     */
    private Integer librariesCreated;
}
