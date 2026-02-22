package com.operator.common.dto.library;

import com.operator.common.enums.LibraryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共库打包路径配置响应 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryPathConfigResponse {

    private Long libraryId;
    private String libraryName;
    private LibraryType libraryType;
    private String version;
    private String description;
    private String currentPath;
    private String recommendedPath;
    private Boolean useCustomPath;
    private Integer orderIndex;
    private Integer relatedOperators;
}
