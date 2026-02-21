package com.operator.common.dto.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 库文件创建请求
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryFileCreateRequest {

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过 255 个字符")
    private String fileName;

    /**
     * 顺序号
     */
    private Integer orderIndex;
}
