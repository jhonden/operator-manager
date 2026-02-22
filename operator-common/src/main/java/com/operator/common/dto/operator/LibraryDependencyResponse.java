package com.operator.common.dto.operator;

import com.operator.common.enums.LibraryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 算子依赖的公共库响应
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Schema(description = "算子依赖的公共库响应")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryDependencyResponse {

    @Schema(description = "关联ID")
    private Long id;

    @Schema(description = "公共库ID")
    private Long libraryId;

    @Schema(description = "公共库名称")
    private String libraryName;

    @Schema(description = "公共库描述")
    private String libraryDescription;

    @Schema(description = "公共库版本")
    private String version;

    @Schema(description = "公共库分类")
    private String category;

    @Schema(description = "公共库类型")
    private LibraryType libraryType;

    @Schema(description = "文件数量")
    private Integer fileCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
