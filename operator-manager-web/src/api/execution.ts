import request from '@/utils/request';
import type { ApiResponse, Task, TaskRequest, PageResponse, TaskLog, TaskStatistics } from '@/types';

export const executionApi = {
  /**
   * Create task
   */
  createTask: (data: TaskRequest): Promise<ApiResponse<Task>> => {
    return request.post<ApiResponse<Task>>('/v1/execution/tasks', data);
  },

  /**
   * Get task by ID
   */
  getTask: (id: number): Promise<ApiResponse<Task>> => {
    return request.get<ApiResponse<Task>>(`/v1/execution/tasks/${id}`);
  },

  /**
   * Get all tasks
   */
  getAllTasks: (status?: string, page = 0, size = 20): Promise<ApiResponse<PageResponse<Task>>> => {
    return request.get<ApiResponse<PageResponse<Task>>>('/v1/execution/tasks', {
      params: { status, page, size },
    });
  },

  /**
   * Get my tasks
   */
  getMyTasks: (
    status?: string,
    page = 0,
    size = 20
  ): Promise<ApiResponse<PageResponse<Task>>> => {
    return request.get<ApiResponse<PageResponse<Task>>>('/v1/execution/my-tasks', {
      params: { status, page, size },
    });
  },

  /**
   * Get task logs
   */
  getTaskLogs: (id: number): Promise<ApiResponse<TaskLog[]>> => {
    return request.get<ApiResponse<TaskLog[]>>(`/v1/execution/tasks/${id}/logs`);
  },

  /**
   * Cancel task
   */
  cancelTask: (id: number): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>(`/v1/execution/tasks/${id}/cancel`);
  },

  /**
   * Retry task
   */
  retryTask: (id: number): Promise<ApiResponse<Task>> => {
    return request.post<ApiResponse<Task>>(`/v1/execution/tasks/${id}/retry`);
  },

  /**
   * Delete task
   */
  deleteTask: (id: number): Promise<ApiResponse<void>> => {
    return request.delete<ApiResponse<void>>(`/v1/execution/tasks/${id}`);
  },

  /**
   * Get statistics
   */
  getStatistics: (): Promise<ApiResponse<TaskStatistics>> => {
    return request.get<ApiResponse<TaskStatistics>>('/v1/execution/statistics');
  },
};
