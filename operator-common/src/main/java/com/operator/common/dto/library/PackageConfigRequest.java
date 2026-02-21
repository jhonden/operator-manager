package com.operator.common.dto.library;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 算子包整体配置请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageConfigRequest {

    /**
     * 打包模板（legacy/modern/custom）
     */
    @NotBlank(message = "打包模板不能为空")
    @Size(max = 50, message = "打包模板长度不能超过50")
    private String packageTemplate;

    /**
     * 算子路径配置列表
     */
    @Valid
    private List<OperatorPathConfigRequest> operatorConfigs;

    /**
     * 公共库路径配置列表
     */
    @Valid
    private List<LibraryPathConfigRequest> libraryConfigs;
}
