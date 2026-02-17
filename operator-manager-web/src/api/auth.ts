import request from '@/utils/request';
import type {
  ApiResponse,
  AuthResponse,
  LoginRequest,
  RegisterRequest,
  User,
} from '@/types';

export const authApi = {
  /**
   * User login
   */
  login: async (data: LoginRequest): Promise<ApiResponse<AuthResponse>> => {
    return request.post<ApiResponse<AuthResponse>>('/v1/auth/login', data);
  },

  /**
   * User registration
   */
  register: (data: RegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    return request.post<ApiResponse<AuthResponse>>('/v1/auth/register', data);
  },

  /**
   * Refresh access token
   */
  refreshToken: async (refreshToken: string): Promise<ApiResponse<AuthResponse>> => {
    return request.post<ApiResponse<AuthResponse>>('/v1/auth/refresh', { refreshToken });
  },

  /**
   * Get current user info
   */
  getCurrentUser: (): Promise<ApiResponse<User>> => {
    return request.get<ApiResponse<User>>('/v1/auth/me');
  },

  /**
   * Change password
   */
  changePassword: async (data: {
    oldPassword: string;
    newPassword: string;
  }): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>('/v1/auth/change-password', data);
  },

  /**
   * Logout
   */
  logout: (): Promise<ApiResponse<void>> => {
    return request.post<ApiResponse<void>>('/v1/auth/logout');
  },
};

export default authApi;
