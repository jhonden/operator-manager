package com.operator.common.dto.library;

import com.operator.common.enums.LibraryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 公共库创建/更新请求 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryRequest {

    /**
     * 公共库名称
     */
    @NotBlank(message = "公共库名称不能为空")
    @Size(max = 255, message = "公共库名称长度不能超过255")
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 版本号
     */
    @NotBlank(message = "版本号不能为空")
    @Size(max = 50, message = "版本号长度不能超过50")
    private String version;

    /**
     * 分类
     */
    @Size(max = 100, message = "分类长度不能超过100")
    private String category;

    /**
     * 库类型
     */
    @NotNull(message = "库类型不能为空")
    private LibraryType libraryType;

    /**
     * 代码文件列表
     */
    @Valid
    private List<LibraryFileRequest> files;
}
