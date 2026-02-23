package com.operator.common.dto.pkg;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量添加算子 Response DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchAddOperatorsResponse {

    /**
     * 总尝试添加的算子数量
     */
    private Integer total;

    /**
     * 成功添加的算子数量
     */
    private Integer successCount;

    /**
     * 失败的算子数量
     */
    private Integer failedCount;

    /**
     * 失败的算子列表（包含失败原因）
     */
    private List<FailedOperatorInfo> failedOperators;

    /**
     * 失败算子信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedOperatorInfo {
        /**
         * 算子 ID
         */
        private Long operatorId;

        /**
         * 算子名称
         */
        private String operatorName;

        /**
         * 失败原因
         */
        private String reason;
    }
}
