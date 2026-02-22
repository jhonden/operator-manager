package com.operator.common.dto.operator;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Batch Update Operator Library Dependencies Request
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchLibraryDependenciesRequest {

    @NotEmpty(message = "算子ID列表不能为空")
    private List<Long> operatorIds;

    @NotEmpty(message = "公共库ID列表不能为空")
    private List<Long> libraryIds;
}
