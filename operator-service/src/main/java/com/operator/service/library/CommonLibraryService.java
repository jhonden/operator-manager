package com.operator.service.library;

import com.operator.common.dto.library.*;
import com.operator.common.utils.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 公共库 Service 接口
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface CommonLibraryService {

    /**
     * 创建公共库
     */
    LibraryResponse createLibrary(LibraryRequest request, String username);

    /**
     * 更新公共库
     */
    LibraryResponse updateLibrary(Long id, LibraryRequest request, String username);

    /**
     * 删除公共库
     */
    void deleteLibrary(Long id, String username);

    /**
     * 根据ID获取公共库
     */
    LibraryResponse getLibraryById(Long id);

    /**
     * 搜索公共库
     */
    PageResponse<LibraryResponse> searchLibraries(String keyword, String libraryType,
                                                Integer page, Integer size);

    /**
     * 根据类型获取公共库
     */
    Page<LibraryResponse> getLibrariesByType(String libraryType, Pageable pageable);

    /**
     * 根据分类获取公共库
     */
    Page<LibraryResponse> getLibrariesByCategory(String category, Pageable pageable);
}
