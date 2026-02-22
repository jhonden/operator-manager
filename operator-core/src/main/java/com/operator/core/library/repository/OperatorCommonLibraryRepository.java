package com.operator.core.library.repository;

import com.operator.core.library.domain.OperatorCommonLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 算子-公共库关联 Repository
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface OperatorCommonLibraryRepository extends JpaRepository<OperatorCommonLibrary, Long> {

    /**
     * 根据算子ID查找所有依赖的公共库
     */
    List<OperatorCommonLibrary> findByOperatorId(Long operatorId);

    /**
     * 根据公共库ID查找所有使用它的算子
     */
    List<OperatorCommonLibrary> findByLibraryId(Long libraryId);

    /**
     * 检查算子是否依赖指定的公共库
     */
    boolean existsByOperatorIdAndLibraryId(Long operatorId, Long libraryId);

    /**
     * 根据算子ID查找所有依赖的公共库（带公共库详情）
     */
    @Query("SELECT ocl FROM OperatorCommonLibrary ocl " +
           "JOIN FETCH ocl.library " +
           "WHERE ocl.operator.id = :operatorId")
    List<OperatorCommonLibrary> findByOperatorIdWithLibrary(@Param("operatorId") Long operatorId);

    /**
     * 删除算子对公共库的所有依赖
     */
    void deleteByOperatorId(Long operatorId);

    /**
     * 删除公共库的所有算子依赖
     */
    void deleteByLibraryId(Long libraryId);

    /**
     * 统计使用指定公共库的算子数量
     */
    @Query("SELECT COUNT(ocl) FROM OperatorCommonLibrary ocl WHERE ocl.library.id = :libraryId")
    long countByLibraryId(@Param("libraryId") Long libraryId);
}
