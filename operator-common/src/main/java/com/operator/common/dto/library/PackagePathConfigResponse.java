package com.operator.common.dto.library;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 算子包打包路径配置响应 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackagePathConfigResponse {

    /**
     * 打包模板（legacy/modern/custom）
     */
    private String packageTemplate;

    /**
     * 算子路径配置列表
     */
    private List<OperatorPathConfigResponse> operatorConfigs;

    /**
     * 公共库路径配置列表
     */
    private List<LibraryPathConfigResponse> libraryConfigs;
}
