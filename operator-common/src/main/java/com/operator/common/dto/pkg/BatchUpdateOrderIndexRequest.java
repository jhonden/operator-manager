package com.operator.common.dto.pkg;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量更新算子执行顺序请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUpdateOrderIndexRequest {

    /**
     * 执行顺序索引
     */
    @NotNull(message = "执行顺序不能为空")
    @Min(value = 1, message = "执行顺序必须大于等于1")
    private Integer orderIndex;

    /**
     * 算子包-算子关联ID列表
     */
    @NotNull(message = "算子列表不能为空")
    private List<Long> packageOperatorIds;
}
