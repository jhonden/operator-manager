package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 算子打包路径配置响应 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatorPathConfigResponse {

    private Long operatorId;
    private String operatorCode;
    private String operatorName;
    private String currentPath;
    private String recommendedPath;
    private Boolean useCustomPath;
    private Integer orderIndex;
}
