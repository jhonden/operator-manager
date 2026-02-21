package com.operator.core.library.repository;

import com.operator.common.enums.LibraryType;
import com.operator.core.library.domain.CommonLibrary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 公共库 Repository
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface CommonLibraryRepository extends JpaRepository<CommonLibrary, Long> {

    /**
     * 根据库名称和版本查找公共库
     */
    Optional<CommonLibrary> findByNameAndVersion(String name, String version);

    /**
     * 检查库名称和版本是否已存在
     */
    boolean existsByNameAndVersion(String name, String version);

    /**
     * 根据库类型查找
     */
    List<CommonLibrary> findByLibraryType(LibraryType libraryType);

    /**
     * 根据分类查找
     */
    List<CommonLibrary> findByCategory(String category);

    /**
     * 搜索公共库（按名称或描述）
     */
    @Query("SELECT l FROM CommonLibrary l WHERE " +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<CommonLibrary> searchLibraries(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据类型搜索公共库（按名称或描述）
     */
    @Query("SELECT l FROM CommonLibrary l WHERE " +
           "l.libraryType = :libraryType AND (" +
           "LOWER(l.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<CommonLibrary> searchLibrariesByType(@Param("libraryType") LibraryType libraryType,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);

    /**
     * 根据类型查找公共库（分页）
     */
    @Query("SELECT l FROM CommonLibrary l WHERE l.libraryType = :libraryType")
    Page<CommonLibrary> findByLibraryType(@Param("libraryType") LibraryType libraryType, Pageable pageable);

    /**
     * 查找所有公共库及其文件
     */
    @Query("SELECT DISTINCT l FROM CommonLibrary l " +
           "LEFT JOIN FETCH l.files " +
           "ORDER BY l.createdAt DESC")
    List<CommonLibrary> findAllWithFiles();

    /**
     * 根据ID查找公共库及其文件
     */
    @Query("SELECT l FROM CommonLibrary l " +
           "LEFT JOIN FETCH l.files " +
           "WHERE l.id = :id")
    Optional<CommonLibrary> findByIdWithFiles(@Param("id") Long id);

    /**
     * 根据创建人查找
     */
    List<CommonLibrary> findByCreatedBy(String createdBy);
}
