package com.operator.common.dto.pkg;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 批量删除算子 Request DTO
 *
 * @author Operator Manager Team
 * @version 1.1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchRemoveRequest {

    @NotEmpty(message = "至少需要移除一个算子")
    private java.util.List<Long> packageOperatorIds;

    @NotNull(message = "删除原因不能为空")
    private String reason;
}
