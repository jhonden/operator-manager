import request from '@/utils/request';
import type { ApiResponse, OperatorPackage, PackageRequest, PageResponse, PackageOperator } from '@/types';

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
};
