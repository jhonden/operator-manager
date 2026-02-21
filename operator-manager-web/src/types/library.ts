/**
 * 公共库类型定义
 */

/**
 * 公共库类型
 */
export enum LibraryType {
  CONSTANT = 'CONSTANT',
  METHOD = 'METHOD',
  MODEL = 'MODEL',
  CUSTOM = 'CUSTOM'
}

/**
 * 公共库请求
 */
export interface LibraryRequest {
  name: string;
  description?: string;
  version: string;
  category?: string;
  libraryType: LibraryType;
  files?: LibraryFileRequest[];
}

/**
 * 公共库文件请求
 */
export interface LibraryFileRequest {
  fileName: string;
  filePath?: string;
  code?: string;
  orderIndex?: number;
}

/**
 * 公共库响应
 */
export interface LibraryResponse {
  id: number;
  name: string;
  description: string;
  version: string;
  category: string;
  libraryType: LibraryType;
  createdBy?: string;
  createdAt: string;
  updatedAt: string;
  files: LibraryFileResponse[];
  usageCount: number;
}

/**
 * 公共库文件响应
 */
export interface LibraryFileResponse {
  id: number;
  libraryId: number;
  fileName: string;
  filePath: string;
  code: string;
  orderIndex: number;
  createdAt: string;
  updatedAt: string;
}

/**
 * 算子包路径配置请求
 */
export interface PackagePathConfigRequest {
  packageTemplate: 'legacy' | 'modern' | 'custom';
  operatorConfigs?: OperatorPathConfigRequest[];
  libraryConfigs?: LibraryPathConfigRequest[];
}

/**
 * 算子路径配置请求
 */
export interface OperatorPathConfigRequest {
  operatorId: number;
  useCustomPath: boolean;
  customPackagePath?: string;
  orderIndex?: number;
}

/**
 * 公共库路径配置请求
 */
export interface LibraryPathConfigRequest {
  libraryId: number;
  useCustomPath: boolean;
  customPackagePath?: string;
  orderIndex?: number;
}

/**
 * 算子包路径配置响应
 */
export interface OperatorPathConfigResponse {
  operatorId: number;
  operatorCode: string;
  operatorName: string;
  currentPath: string;
  recommendedPath: string;
  useCustomPath: boolean;
  orderIndex: number;
}

/**
 * 公共库路径配置响应
 */
export interface LibraryPathConfigResponse {
  libraryId: number;
  libraryName: string;
  libraryType: LibraryType;
  version?: string;
  currentPath: string;
  recommendedPath: string;
  useCustomPath: boolean;
  orderIndex: number;
}

/**
 * 算子包路径配置响应
 */
export interface PackagePathConfigResponse {
  packageTemplate: 'legacy' | 'modern' | 'custom';
  operatorConfigs: OperatorPathConfigResponse[];
  libraryConfigs: LibraryPathConfigResponse[];
}

/**
 * 添加公共库到算子包请求
 */
export interface AddLibraryToPackageRequest {
  libraryId: number;
  version: string;
  orderIndex?: number;
}

/**
 * 批量路径配置请求
 */
export interface BatchPathConfigRequest {
  useRecommendedPath: boolean;
  operatorIds?: number[];
  libraryIds?: number[];
}

/**
 * 打包预览树节点
 */
export interface PackagePreviewTreeNode {
  type: 'directory' | 'file';
  path: string;
  children?: PackagePreviewTreeNode[];
  source?: PackagePreviewSource;
}

/**
 * 打包预览资源来源
 */
export interface PackagePreviewSource {
  type: 'operator' | 'library';
  id: number;
  name: string;
  version?: string;
}

/**
 * 打包预览冲突
 */
export interface PackagePreviewConflict {
  type: 'path_conflict' | 'dependency_missing';
  path: string;
  message: string;
  conflictingResources: PackagePreviewSource[];
}

/**
 * 打包预览响应
 */
export interface PackagePreviewResponse {
  packageName: string;
  template: 'legacy' | 'modern' | 'custom';
  structure: PackagePreviewTreeNode[];
  conflicts: PackagePreviewConflict[];
  warnings: string[];
}

/**
 * 公共库搜索请求
 */
export interface LibrarySearchRequest {
  keyword?: string;
  libraryType?: LibraryType;
  page?: number;
  size?: number;
}
