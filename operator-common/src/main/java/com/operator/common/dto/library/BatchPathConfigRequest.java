package com.operator.common.dto.library;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量更新路径配置请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPathConfigRequest {

    /**
     * 是否使用推荐路径
     */
    @NotNull(message = "是否使用推荐路径不能为空")
    private Boolean useRecommendedPath;

    /**
     * 算子ID列表（批量更新算子路径时使用）
     */
    private List<Long> operatorIds;

    /**
     * 公共库ID列表（批量更新库路径时使用）
     */
    private List<Long> libraryIds;
}
