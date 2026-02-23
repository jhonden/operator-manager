package com.operator.common.dto.pkg;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量移除算子 Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRemoveOperatorsRequest {

    @NotEmpty(message = "至少需要移除一个算子")
    private List<Long> packageOperatorIds;

    @NotNull(message = "删除原因不能为空")
    private String reason;
}
