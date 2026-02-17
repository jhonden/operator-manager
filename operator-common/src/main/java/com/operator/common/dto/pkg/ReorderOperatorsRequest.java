package com.operator.common.dto.pkg;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Reorder Package Operators Request DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderOperatorsRequest {

    @NotNull(message = "Operator orders are required")
    private List<OperatorOrderItem> operators;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OperatorOrderItem {
        private Long packageOperatorId;
        private Integer orderIndex;
    }
}
