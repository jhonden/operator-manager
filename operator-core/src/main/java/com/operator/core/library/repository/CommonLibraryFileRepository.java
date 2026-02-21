package com.operator.core.library.repository;

import com.operator.core.library.domain.CommonLibraryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 公共库文件 Repository
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface CommonLibraryFileRepository extends JpaRepository<CommonLibraryFile, Long> {

    /**
     * 根据公共库ID查找所有文件
     */
    List<CommonLibraryFile> findByLibraryIdOrderByOrderIndex(Long libraryId);

    /**
     * 根据公共库ID删除所有文件
     */
    void deleteByLibraryId(Long libraryId);
}
