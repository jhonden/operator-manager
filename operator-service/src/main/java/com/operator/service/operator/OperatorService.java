package com.operator.service.operator;

import com.operator.common.dto.operator.*;
import com.operator.common.utils.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Operator Service Interface
 *
 * @author Operator Manager Team
 * @version 1.0.0
 */
public interface OperatorService {

    /**
     * Create a new operator
     */
    OperatorResponse createOperator(OperatorRequest request, String username);

    /**
     * Update an existing operator
     */
    OperatorResponse updateOperator(Long id, OperatorRequest request, String username);

    /**
     * Delete an operator
     */
    void deleteOperator(Long id, String username);

    /**
     * Get operator by ID
     */
    OperatorResponse getOperatorById(Long id);

    /**
     * Get operator by name
     */
    OperatorResponse getOperatorByName(String name);

    /**
     * Search operators with filters
     */
    PageResponse<OperatorResponse> searchOperators(OperatorSearchRequest request);

    /**
     * Get all operators (paginated)
     */
    Page<OperatorResponse> getAllOperators(Pageable pageable);

    /**
     * Get operators by category
     */
    List<OperatorResponse> getOperatorsByCategory(Long categoryId);

    /**
     * Get operators by creator
     */
    List<OperatorResponse> getOperatorsByCreator(String username);

    /**
     * Upload operator code file
     */
    OperatorResponse uploadCodeFile(Long operatorId, MultipartFile file, String username);

    /**
     * Update operator status
     */
    OperatorResponse updateOperatorStatus(Long id, String status, String username);

    /**
     * Add parameter to operator
     */
    ParameterResponse addParameter(Long operatorId, ParameterRequest request, String username);

    /**
     * Update parameter
     */
    ParameterResponse updateParameter(Long operatorId, Long parameterId, ParameterRequest request, String username);

    /**
     * Delete parameter
     */
    void deleteParameter(Long operatorId, Long parameterId, String username);

    /**
     * Toggle featured status
     */
    OperatorResponse toggleFeatured(Long id, String username);

    /**
     * Increment download count
     */
    void incrementDownloadCount(Long id);

    /**
     * 获取算子依赖的公共库列表
     */
    List<LibraryDependencyResponse> getOperatorLibraries(Long operatorId);

    /**
     * 添加公共库依赖
     */
    LibraryDependencyResponse addLibraryDependency(Long operatorId, AddLibraryDependencyRequest request, String username);

    /**
     * 移除公共库依赖
     */
    void removeLibraryDependency(Long operatorId, Long libraryId, String username);
}
