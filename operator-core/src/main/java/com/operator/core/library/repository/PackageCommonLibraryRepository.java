package com.operator.core.library.repository;

import com.operator.core.library.domain.PackageCommonLibrary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 算子包-公共库关联 Repository
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
@Repository
public interface PackageCommonLibraryRepository extends JpaRepository<PackageCommonLibrary, Long> {

    /**
     * 根据算子包ID查找所有包含的公共库
     */
    List<PackageCommonLibrary> findByOperatorPackageIdOrderByOrderIndex(Long packageId);

    /**
     * 根据公共库ID查找所有包含它的算子包
     */
    List<PackageCommonLibrary> findByLibraryId(Long libraryId);

    /**
     * 检查算子包是否包含指定的公共库
     */
    boolean existsByOperatorPackageIdAndLibraryId(Long packageId, Long libraryId);

    /**
     * 根据算子包和公共库ID查找关联
     */
    java.util.Optional<PackageCommonLibrary> findByOperatorPackageIdAndLibraryId(Long packageId, Long libraryId);

    /**
     * 根据算子包ID查找所有包含的公共库（带公共库详情）
     */
    @Query("SELECT pcl FROM PackageCommonLibrary pcl " +
           "JOIN FETCH pcl.library " +
           "WHERE pcl.operatorPackage.id = :packageId " +
           "ORDER BY pcl.orderIndex ASC")
    List<PackageCommonLibrary> findByOperatorPackageIdWithLibrary(@Param("packageId") Long packageId);

    /**
     * 删除算子包的所有公共库
     */
    void deleteByOperatorPackageId(Long packageId);

    /**
     * 删除公共库的所有包关联
     */
    void deleteByLibraryId(Long libraryId);
}
