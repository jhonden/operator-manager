import request from '@/utils/request';
import type { ApiResponse, Operator, OperatorRequest, PageResponse, Parameter } from '@/types';
import type { LibraryDependencyResponse, AddLibraryDependencyRequest } from '@/types/library';

export const operatorApi = {
  /**
   * Get all operators with filters
   */
  getAllOperators: (params?: {
    language?: string;
    status?: string;
    keyword?: string;
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageResponse<Operator>>> => {
    const { page = 0, size = 20, ...queryParams } = params || {};
    return request.get<ApiResponse<PageResponse<Operator>>>('/v1/operators', {
      params: queryParams,
    });
  },

  /**
   * Get operators list
   */
  getOperators: (params: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
  }): Promise<ApiResponse<PageResponse<Operator>>> => {
    return request.get<ApiResponse<PageResponse<Operator>>>('/v1/operators', { params });
  },

  /**
   * Search operators
   */
  searchOperators: (data: {
    keyword?: string;
    language?: string;
    status?: string;
    isPublic?: boolean;
    featured?: boolean;
    sortBy?: string;
    sortOrder?: string;
    page?: number;
    size?: number;
  }): Promise<ApiResponse<PageResponse<Operator>>> => {
    return request.post<ApiResponse<PageResponse<Operator>>>('/v1/operators/search', data);
  },

  /**
   * Get operator by ID
   */
  getOperator: (id: number): Promise<ApiResponse<Operator>> => {
    return request.get<ApiResponse<Operator>>(`/v1/operators/${id}`);
  },

  /**
   * Create operator
   */
  createOperator: (data: OperatorRequest): Promise<ApiResponse<Operator>> => {
    return request.post<ApiResponse<Operator>>('/v1/operators', data);
  },

  /**
   * Update operator
   */
  updateOperator: (id: number, data: OperatorRequest): Promise<ApiResponse<Operator>> => {
    return request.put<ApiResponse<Operator>>(`/v1/operators/${id}`, data);
  },

  /**
   * Delete operator
   */
  deleteOperator: (id: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(`/v1/operators/${id}`);
  },

  /**
   * Get my operators
   */
  getMyOperators: (): Promise<ApiResponse<Operator[]>> => {
    return request.get<ApiResponse<Operator[]>>('/v1/operators/my-operators');
  },

  /**
   * Upload operator code file
   */
  uploadCodeFile: (
    id: number,
    file: File,
    onProgress?: (progress: number) => void
  ): Promise<ApiResponse<Operator>> => {
    return request.uploadFile(`/v1/operators/${id}/upload`, file, onProgress);
  },

  /**
   * Update operator status
   */
  updateOperatorStatus: (
    id: number,
    status: string
  ): Promise<ApiResponse<Operator>> => {
    return request.patch<ApiResponse<Operator>>(
      `/v1/operators/${id}/status`,
      {},
      { params: { status } }
    );
  },

  /**
   * Add parameter to operator
   */
  addParameter: (
    operatorId: number,
    data: {
      name: string;
      description?: string;
      parameterType: string;
      ioType: string;
      isRequired?: boolean;
      defaultValue?: string;
      validationRules?: string;
      orderIndex?: number;
    }
  ): Promise<ApiResponse<Parameter>> => {
    return request.post<ApiResponse<Parameter>>(
      `/v1/operators/${operatorId}/parameters`,
      data
    );
  },

  /**
   * Delete parameter
   */
  deleteParameter: (operatorId: number, parameterId: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(
      `/v1/operators/${operatorId}/parameters/${parameterId}`
    );
  },

  /**
   * Toggle featured status
   */
  toggleFeatured: (id: number): Promise<ApiResponse<Operator>> => {
    return request.patch<ApiResponse<Operator>>(`/v1/operators/${id}/featured`);
  },

  /**
   * Increment download count
   */
  incrementDownload: (id: number): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>(`/v1/operators/${id}/download`);
  },

  /**
   * 获取算子依赖的公共库列表
   */
  getOperatorLibraries: (operatorId: number): Promise<ApiResponse<LibraryDependencyResponse[]>> => {
    console.log('[Operator API] 获取算子依赖的公共库列表, operatorId:', operatorId);
    return request.get<ApiResponse<LibraryDependencyResponse[]>>(`/v1/operators/${operatorId}/library-dependencies`);
  },

  /**
   * 添加公共库依赖
   */
  addLibraryDependency: (operatorId: number, data: AddLibraryDependencyRequest): Promise<ApiResponse<LibraryDependencyResponse>> => {
    console.log('[Operator API] 添加公共库依赖, operatorId:', operatorId, 'libraryId:', data.libraryId);
    return request.post<ApiResponse<LibraryDependencyResponse>>(`/v1/operators/${operatorId}/library-dependencies`, data);
  },

  /**
   * 移除公共库依赖
   */
  removeLibraryDependency: (operatorId: number, libraryId: number): Promise<ApiResponse<void>> => {
    console.log('[Operator API] 移除公共库依赖, operatorId:', operatorId, 'libraryId:', libraryId);
    return request.delete<ApiResponse<void>>(`/v1/operators/${operatorId}/library-dependencies/${libraryId}`);
  },

  /**
   * 批量更新算子公共库依赖
   */
  batchUpdateLibraryDependencies: (data: {
    operatorIds: number[];
    libraryIds: number[];
  }): Promise<ApiResponse<void>> => {
    console.log('[Operator API] 批量更新算子公共库依赖, operatorIds:', data.operatorIds, 'libraryIds:', data.libraryIds);
    return request.post<ApiResponse<void>>('/v1/operators/batch-library-dependencies', data);
  },
};
