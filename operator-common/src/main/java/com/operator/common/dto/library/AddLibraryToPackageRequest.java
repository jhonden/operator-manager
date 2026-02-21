package com.operator.common.dto.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向算子包添加公共库请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddLibraryToPackageRequest {

    /**
     * 公共库ID
     */
    @NotNull(message = "公共库ID不能为空")
    private Long libraryId;

    /**
     * 使用的版本
     */
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50")
    private String version;

    /**
     * 打包顺序
     */
    private Integer orderIndex;
}
