package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 算子包元数据 DTO
 * 用于生成 metainfo_operators.yml 文件
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageMetadata {
    /**
     * 业务场景
     */
    private String businessName;

    /**
     * 算子包版本
     */
    private String version;

    /**
     * 算子列表
     */
    private List<OperatorMetadata> operators;
}
