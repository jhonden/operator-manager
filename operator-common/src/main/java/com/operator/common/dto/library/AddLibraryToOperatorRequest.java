package com.operator.common.dto.library;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 向算子添加公共库依赖请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddLibraryToOperatorRequest {

    /**
     * 公共库ID列表
     */
    @NotNull(message = "公共库ID列表不能为空")
    private List<Long> libraryIds;
}
