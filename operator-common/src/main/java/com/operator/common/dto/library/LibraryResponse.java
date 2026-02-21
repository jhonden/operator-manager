package com.operator.common.dto.library;

import com.operator.common.enums.LibraryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 公共库响应 DTO
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryResponse {

    private Long id;
    private String name;
    private String description;
    private String version;
    private String category;
    private LibraryType libraryType;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<LibraryFileResponse> files;
    private Long usageCount; // 使用该库的算子数量
}
