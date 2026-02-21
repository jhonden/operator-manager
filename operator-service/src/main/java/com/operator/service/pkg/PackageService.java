package com.operator.service.pkg;

import com.operator.common.dto.*;
import com.operator.common.dto.library.*;
import com.operator.common.dto.pkg.*;
import com.operator.common.utils.PageResponse;

import java.util.List;

/**
 * Operator Package Service Interface
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface PackageService {

    /**
     * Create a new operator package
     */
    PackageResponse createPackage(PackageRequest request, String username);

    /**
     * Update an existing package
     */
    PackageResponse updatePackage(Long id, PackageRequest request, String username);

    /**
     * Delete a package
     */
    void deletePackage(Long id, String username);

    /**
     * Get package by ID
     */
    PackageResponse getPackageById(Long id);

    /**
     * Get all packages (paginated)
     */
    com.operator.common.utils.PageResponse<PackageResponse> getAllPackages(int page, int size);

    /**
     * Search packages
     */
    com.operator.common.utils.PageResponse<PackageResponse> searchPackages(String keyword, int page, int size);

    /**
     * Get packages by creator
     */
    List<PackageResponse> getPackagesByCreator(String username);

    /**
     * Add operator to package
     */
    PackageOperatorResponse addOperator(Long packageId, PackageOperatorRequest request, String username);

    /**
     * Update package operator
     */
    PackageOperatorResponse updatePackageOperator(Long packageId, Long packageOperatorId,
                                                   PackageOperatorRequest request, String username);

    /**
     * Remove operator from package
     */
    void removeOperator(Long packageId, Long packageOperatorId, String username);

    /**
     * Reorder operators in package
     */
    void reorderOperators(Long packageId, ReorderOperatorsRequest request, String username);

    /**
     * Get package operators
     */
    List<PackageOperatorResponse> getPackageOperators(Long packageId);

    /**
     * Update package status
     */
    PackageResponse updatePackageStatus(Long id, String status, String username);

    /**
     * Toggle featured status
     */
    PackageResponse toggleFeatured(Long id, String username);

    /**
     * Increment download count
     */
    void incrementDownloadCount(Long id);

    // ========== 公共库相关方法 ==========

    /**
     * 向算子包添加公共库
     */
    LibraryPathConfigResponse addLibraryToPackage(Long packageId, AddLibraryToPackageRequest request, String username);

    /**
     * 从算子包移除公共库
     */
    void removeLibraryFromPackage(Long packageId, Long packageCommonLibraryId, String username);

    /**
     * 获取算子包的打包路径配置
     */
    PackagePathConfigResponse getPackagePathConfig(Long packageId);

    /**
     * 更新算子包整体配置
     */
    PackagePathConfigResponse updatePackageConfig(Long packageId, PackageConfigRequest request, String username);

    /**
     * 更新算子打包路径配置
     */
    void updateOperatorPathConfig(Long packageId, Long operatorId, OperatorPathConfigRequest request, String username);

    /**
     * 批量更新算子打包路径配置
     */
    void batchUpdateOperatorPathConfig(Long packageId, BatchPathConfigRequest request, String username);

    /**
     * 更新公共库打包路径配置
     */
    void updateLibraryPathConfig(Long packageId, Long packageCommonLibraryId, LibraryPathConfigRequest request, String username);

    /**
     * 批量更新公共库打包路径配置
     */
    void batchUpdateLibraryPathConfig(Long packageId, BatchPathConfigRequest request, String username);
}
