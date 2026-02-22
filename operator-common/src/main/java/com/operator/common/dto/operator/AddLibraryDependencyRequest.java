package com.operator.common.dto.operator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 添加公共库依赖请求
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Schema(description = "添加公共库依赖请求")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddLibraryDependencyRequest {

    @NotNull(message = "公共库ID不能为空")
    @Schema(description = "公共库ID", required = true, example = "1")
    private Long libraryId;
}
