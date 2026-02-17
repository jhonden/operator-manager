import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios';
import { message } from 'antd';
import useAuthStore from '@/stores/useAuthStore';

const BASE_URL = '/api';

class Request {
  private instance: AxiosInstance;

  constructor() {
    this.instance = axios.create({
      baseURL: BASE_URL,
      timeout: 60000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.setupInterceptors();
  }

  private setupInterceptors() {
    // Request interceptor
    this.instance.interceptors.request.use(
      (config) => {
        const token = useAuthStore.getState().token;
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => {
        return Promise.reject(error);
      }
    );

    // Response interceptor
    this.instance.interceptors.response.use(
      (response: AxiosResponse) => {
        return response.data;
      },
      (error) => {
        const { response } = error;

        if (response) {
          const status = response.status;
          const data = response.data;

          switch (status) {
            case 401:
              // Unauthorized - clear token and redirect to login
              useAuthStore.getState().clearAuth();
              window.location.href = '/login';
              message.error('Session expired. Please login again.');
              break;
            case 403:
              message.error('You do not have permission to perform this action.');
              break;
            case 404:
              message.error('Resource not found.');
              break;
            case 500:
              message.error('Server error. Please try again later.');
              break;
            default:
              if (data?.error) {
                message.error(data.error);
              } else if (data?.message) {
                message.error(data.message);
              } else {
                message.error('Request failed. Please try again.');
              }
          }
        } else if (error.code === 'ECONNABORTED') {
          message.error('Network error. Please check your connection.');
        } else {
          message.error('Request failed. Please try again.');
        }

        return Promise.reject(error);
      }
    );
  }

  public async get<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.get(url, config);
  }

  public async post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.post(url, data, config);
  }

  public async put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.put(url, data, config);
  }

  public async patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.patch(url, data, config);
  }

  public async delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return this.instance.delete(url, config);
  }

  // File upload
  public async uploadFile(url: string, file: File, onProgress?: (progress: number) => void): Promise<any> {
    const formData = new FormData();
    formData.append('file', file);

    return this.instance.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total && onProgress) {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
          onProgress(percentCompleted);
        }
      },
    });
  }
}

const request = new Request();
export default request;
