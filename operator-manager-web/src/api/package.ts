import request from '@/utils/request';
import type {
  ApiResponse,
  OperatorPackage,
  PackageRequest,
  PageResponse,
  PackageOperator,
  PackagePathConfigRequest,
  PackagePathConfigResponse,
  OperatorPathConfigRequest,
  LibraryPathConfigRequest,
  BatchPathConfigRequest,
  AddLibraryToPackageRequest,
  PackagePreviewResponse,
} from '@/types';

export const packageApi = {
  /**
   * Get all packages with filters
   */
  getAllPackages: (
    status?: string,
    keyword?: string,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<OperatorPackage>>> => {
    return request.get<ApiResponse<PageResponse<OperatorPackage>>>('/v1/packages', {
      params: { status, keyword, page, size },
    });
  },

  /**
   * Get packages list
   */
  getPackages: (params: {
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageResponse<OperatorPackage>>> => {
    return request.get<ApiResponse<PageResponse<OperatorPackage>>>('/v1/packages', { params });
  },

  /**
   * Search packages
   */
  searchPackages: (keyword: string, page = 0, size = 20): Promise<ApiResponse<PageResponse<OperatorPackage>>> => {
    return request.get<ApiResponse<PageResponse<OperatorPackage>>>('/v1/packages/search', {
      params: { keyword, page, size },
    });
  },

  /**
   * Get package by ID
   */
  getPackage: (id: number): Promise<ApiResponse<OperatorPackage>> => {
    return request.get<ApiResponse<OperatorPackage>>(`/v1/packages/${id}`);
  },

  /**
   * Create package
   */
  createPackage: (data: PackageRequest): Promise<ApiResponse<OperatorPackage>> => {
    return request.post<ApiResponse<OperatorPackage>>('/v1/packages', data);
  },

  /**
   * Update package
   */
  updatePackage: (id: number, data: PackageRequest): Promise<ApiResponse<OperatorPackage>> => {
    return request.put<ApiResponse<OperatorPackage>>(`/v1/packages/${id}`, data);
  },

  /**
   * Delete package
   */
  deletePackage: (id: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(`/v1/packages/${id}`);
  },

  /**
   * Get my packages
   */
  getMyPackages: (): Promise<ApiResponse<OperatorPackage[]>> => {
    return request.get<ApiResponse<OperatorPackage[]>>('/v1/packages/my-packages');
  },

  /**
   * Get package operators
   */
  getPackageOperators: (packageId: number): Promise<ApiResponse<PackageOperator[]>> => {
    return request.get<ApiResponse<PackageOperator[]>>(
      `/v1/packages/${packageId}/operators`
    );
  },

  /**
   * Add operator to package
   */
  addOperator: (
    packageId: number,
    data: {
      operatorId: number;
      versionId: number;
      orderIndex?: number;
      parameterMapping?: string;
      enabled?: boolean;
      notes?: string;
    }
  ): Promise<ApiResponse<PackageOperator>> => {
    return request.post<ApiResponse<PackageOperator>>(
      `/v1/packages/${packageId}/operators`,
      data
    );
  },

  /**
   * Remove operator from package
   */
  removeOperator: (packageId: number, operatorId: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(
      `/v1/packages/${packageId}/operators/${operatorId}`
    );
  },

  /**
   * Reorder operators in package (by operator ID and direction)
   */
  reorderOperators: (
    packageId: number,
    operatorId: number,
    direction: 'up' | 'down'
  ): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>(
      `/v1/packages/${packageId}/operators/${operatorId}/reorder`,
      {},
      { params: { direction } }
    );
  },

  /**
   * Reorder operators in package (bulk update)
   */
  bulkReorderOperators: (
    packageId: number,
    operators: { packageOperatorId: number; orderIndex: number }[]
  ): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>(
      `/v1/packages/${packageId}/operators/reorder`,
      { operators }
    );
  },

  /**
   * Update package status
   */
  updatePackageStatus: (
    id: number,
    status: string
  ): Promise<ApiResponse<OperatorPackage>> => {
    return request.patch<ApiResponse<OperatorPackage>>(
      `/v1/packages/${id}/status`,
      {},
      { params: { status } }
    );
  },

  /**
   * Toggle featured status
   */
  toggleFeatured: (id: number): Promise<ApiResponse<OperatorPackage>> => {
    return request.patch<ApiResponse<OperatorPackage>>(`/v1/packages/${id}/featured`);
  },

  // ========== 公共库和打包配置相关接口 ==========

  /**
   * 向算子包添加公共库
   */
  addLibraryToPackage: (
    packageId: number,
    data: AddLibraryToPackageRequest
  ): Promise<ApiResponse<{ id: number; libraryId: number; version: string }>> => {
    return request.post<ApiResponse<{ id: number; libraryId: number; version: string }>>(
      `/v1/packages/${packageId}/libraries`,
      data
    );
  },

  /**
   * 从算子包移除公共库
   */
  removeLibraryFromPackage: (
    packageId: number,
    packageCommonLibraryId: number
  ): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(
      `/v1/packages/${packageId}/libraries/${packageCommonLibraryId}`
    );
  },

  /**
   * 获取算子包的打包路径配置
   */
  getPackagePathConfig: (packageId: number): Promise<ApiResponse<PackagePathConfigResponse>> => {
    return request.get<ApiResponse<PackagePathConfigResponse>>(
      `/v1/packages/${packageId}/path-config`
    );
  },

  /**
   * 更新算子包整体配置
   */
  updatePackageConfig: (
    packageId: number,
    data: PackagePathConfigRequest
  ): Promise<ApiResponse<PackagePathConfigResponse>> => {
    return request.put<ApiResponse<PackagePathConfigResponse>>(
      `/v1/packages/${packageId}/config`,
      data
    );
  },

  /**
   * 更新算子打包路径配置
   */
  updateOperatorPathConfig: (
    packageId: number,
    operatorId: number,
    data: OperatorPathConfigRequest
  ): Promise<ApiResponse<void>> => {
    return request.put<ApiResponse<void>>(
      `/v1/packages/${packageId}/operators/${operatorId}/path-config`,
      data
    );
  },

  /**
   * 批量更新算子路径配置
   */
  batchUpdateOperatorPathConfig: (
    packageId: number,
    data: BatchPathConfigRequest
  ): Promise<ApiResponse<void>> => {
    return request.put<ApiResponse<void>>(
      `/v1/packages/${packageId}/operators/batch-path-config`,
      data
    );
  },

  /**
   * 更新公共库打包路径配置
   */
  updateLibraryPathConfig: (
    packageId: number,
    packageCommonLibraryId: number,
    data: LibraryPathConfigRequest
  ): Promise<ApiResponse<void>> => {
    return request.put<ApiResponse<void>>(
      `/v1/packages/${packageId}/libraries/${packageCommonLibraryId}/path-config`,
      data
    );
  },

  /**
   * 批量更新公共库路径配置
   */
  batchUpdateLibraryPathConfig: (
    packageId: number,
    data: BatchPathConfigRequest
  ): Promise<ApiResponse<void>> => {
    return request.put<ApiResponse<void>>(
      `/v1/packages/${packageId}/libraries/batch-path-config`,
      data
    );
  },

  /**
   * 获取打包预览
   */
  generatePreview: (
    packageId: number,
    template: 'legacy' | 'modern' | 'custom' = 'legacy'
  ): Promise<ApiResponse<PackagePreviewResponse>> => {
    return request.get<ApiResponse<PackagePreviewResponse>>(
      `/v1/packages/${packageId}/preview`,
      { params: { template } }
    );
  },
};
