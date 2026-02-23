package com.operator.common.dto.pkg;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量添加算子到算子包 Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAddOperatorsRequest {

    @NotEmpty(message = "至少需要选择一个算子")
    private List<Long> operatorIds;

    @NotNull(message = "执行顺序不能为空")
    @Min(value = 1, message = "执行顺序必须大于等于1")
    private Integer orderIndex;

    @Builder.Default
    private Boolean enabled = true;
}
