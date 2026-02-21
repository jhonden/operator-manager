import request from '@/utils/request';
import type { ApiResponse } from '@/types';
import type {
  LibraryRequest,
  LibraryResponse,
  LibrarySearchRequest,
  LibraryType,
} from '@/types/library';

/**
 * 公共库 API
 */
export const libraryApi = {
  /**
   * 创建公共库
   */
  createLibrary: (data: LibraryRequest): Promise<ApiResponse<LibraryResponse>> => {
    return request.post<ApiResponse<LibraryResponse>>('/v1/libraries', data);
  },

  /**
   * 更新公共库
   */
  updateLibrary: (id: number, data: LibraryRequest): Promise<ApiResponse<LibraryResponse>> => {
    return request.put<ApiResponse<LibraryResponse>>(`/v1/libraries/${id}`, data);
  },

  /**
   * 删除公共库
   */
  deleteLibrary: (id: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(`/v1/libraries/${id}`);
  },

  /**
   * 根据ID获取公共库
   */
  getLibraryById: (id: number): Promise<ApiResponse<LibraryResponse>> => {
    return request.get<ApiResponse<LibraryResponse>>(`/v1/libraries/${id}`);
  },

  /**
   * 搜索公共库
   */
  searchLibraries: (params: LibrarySearchRequest): Promise<ApiResponse<{ content: LibraryResponse[]; currentPage: number; pageSize: number; totalElements: number; totalPages: number }>> => {
    return request.get<ApiResponse<{ content: LibraryResponse[]; currentPage: number; pageSize: number; totalElements: number; totalPages: number }>>('/v1/libraries', { params });
  },

  /**
   * 根据类型获取公共库
   */
  getLibrariesByType: (libraryType: LibraryType, page = 0, size = 20): Promise<ApiResponse<{ content: LibraryResponse[]; currentPage: number; pageSize: number; totalElements: number; totalPages: number }>> => {
    return request.get<ApiResponse<{ content: LibraryResponse[]; currentPage: number; pageSize: number; totalElements: number; totalPages: number }>>(
      `/v1/libraries/type/${libraryType}`,
      { params: { page, size } }
    );
  },

  /**
   * 根据分类获取公共库
   */
  getLibrariesByCategory: (category: string, page = 0, size = 20): Promise<ApiResponse<{ content: LibraryResponse[]; currentPage: number; pageSize: number; totalElements: number; totalPages: number }>> => {
    return request.get<ApiResponse<{ content: LibraryResponse[]; currentPage: number; pageSize: number; totalElements: number; totalPages: number }>>(
      `/v1/libraries/category/${category}`,
      { params: { page, size } }
    );
  },

  /**
   * 创建库文件（空文件）
   */
  createLibraryFile: (libraryId: number, data: { fileName: string; orderIndex?: number }): Promise<ApiResponse<LibraryFileResponse>> => {
    return request.post<ApiResponse<LibraryFileResponse>>(`/v1/libraries/${libraryId}/files`, data);
  },

  /**
   * 更新库文件名
   */
  updateLibraryFileName: (libraryId: number, fileId: number, data: { fileName: string }): Promise<ApiResponse<void>> => {
    return request.put<ApiResponse<void>>(`/v1/libraries/${libraryId}/files/${fileId}`, data);
  },

  /**
   * 更新库文件内容
   */
  updateLibraryFileContent: (libraryId: number, fileId: number, code: string): Promise<ApiResponse<void>> => {
    return request.put<ApiResponse<void>>(`/v1/libraries/${libraryId}/files/${fileId}/content`, { code });
  },

  /**
   * 删除库文件
   */
  deleteLibraryFile: (libraryId: number, fileId: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(`/v1/libraries/${libraryId}/files/${fileId}`);
  },
};
