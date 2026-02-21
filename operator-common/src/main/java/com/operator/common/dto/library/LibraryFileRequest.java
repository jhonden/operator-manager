package com.operator.common.dto.library;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共库文件请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryFileRequest {

    /**
     * 文件名
     */
    @NotBlank(message = "文件名不能为空")
    @jakarta.validation.constraints.Size(max = 255, message = "文件名长度不能超过255")
    private String fileName;

    /**
     * 文件路径（MinIO 存储路径）
     */
    private String filePath;

    /**
     * 代码内容
     */
    private String code;

    /**
     * 文件顺序
     */
    private Integer orderIndex;
}
