package com.operator.common.dto.library;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库文件内容更新请求
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryFileContentUpdateRequest {

    /**
     * 代码内容
     */
    @NotBlank(message = "代码内容不能为空")
    private String code;
}
