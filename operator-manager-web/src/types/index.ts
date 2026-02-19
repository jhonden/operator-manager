// API Response types
export interface ApiResponse<T = any> {
  success: boolean;
  message?: string;
  data?: T;
  error?: string;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  currentPage: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}

// User types
export interface User {
  id: number;
  username: string;
  email: string;
  fullName?: string;
  avatarUrl?: string;
  role: 'ADMIN' | 'USER' | 'GUEST';
  status: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';
  lastLoginAt?: string;
  createdAt: string;
}

export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'DELETED';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: User;
}

// Operator types
export interface Operator {
  id: number;
  name: string;
  description?: string;
  language: 'JAVA' | 'GROOVY';
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  version?: string;
  code?: string;
  codeFilePath?: string;
  fileName?: string;
  fileSize?: number;
  isPublic: boolean;
  downloadsCount: number;
  featured: boolean;
  parameters: Parameter[];
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export type ParameterType = 'STRING' | 'INTEGER' | 'FLOAT' | 'BOOLEAN' | 'JSON' | 'FILE' | 'DATE' | 'ARRAY';

export interface OperatorFilters {
  keyword?: string;
  language?: 'JAVA' | 'GROOVY';
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
}

export interface PackageFilters {
  keyword?: string;
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
}

export interface Parameter {
  id: number;
  name: string;
  description?: string;
  type: ParameterType;
  parameterType?: ParameterType; // Backend uses this
  direction?: 'INPUT' | 'OUTPUT'; // Frontend UI uses this
  ioType?: 'INPUT' | 'OUTPUT'; // Backend API uses this
  required?: boolean; // Frontend UI uses this
  isRequired?: boolean; // Backend API uses this
  defaultValue?: string;
  validationRules?: string;
  orderIndex?: number;
  createdAt: string;
}

// Package types
export interface OperatorPackage {
  id: number;
  name: string;
  description?: string;
  businessScenario: string;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  version?: string;
  icon?: string;
  isPublic: boolean;
  downloadsCount: number;
  featured: boolean;
  operatorCount: number;
  operators: PackageOperator[];
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface PackageOperator {
  id: number;
  operatorId: number;
  operatorName: string;
  operatorLanguage: 'JAVA' | 'GROOVY';
  versionId: number;
  versionNumber: string;
  orderIndex: number;
  parameterMapping?: string;
  enabled: boolean;
  notes?: string;
  createdAt: string;
}

// Task types
export interface Task {
  id: number;
  taskName: string;
  taskType: 'OPERATOR_EXECUTION' | 'PACKAGE_EXECUTION';
  operatorId?: number;
  operatorName?: string;
  packageId?: number;
  packageName?: string;
  operatorVersionId?: number;
  packageVersionId?: number;
  userId: number;
  username?: string;
  status: 'PENDING' | 'QUEUED' | 'RUNNING' | 'SUCCESS' | 'FAILED' | 'TIMEOUT' | 'CANCELLED';
  priority: number;
  inputParameters?: Record<string, any>;
  outputData?: Record<string, any>;
  progress: number;
  startedAt?: string;
  completedAt?: string;
  errorMessage?: string;
  containerId?: string;
  retryCount: number;
  maxRetries: number;
  timeoutSeconds: number;
  createdAt: string;
  durationSeconds?: number;
}

export interface TaskLog {
  id: number;
  taskId: number;
  logLevel: 'TRACE' | 'DEBUG' | 'INFO' | 'WARN' | 'ERROR';
  message: string;
  timestamp: string;
  source?: string;
  exceptionTrace?: string;
}

// Version types
export interface Version {
  id: number;
  operatorId: number;
  operatorName?: string;
  versionNumber: string;
  description?: string;
  changelog?: string;
  status: 'DRAFT' | 'BUILD' | 'TEST' | 'REVIEW' | 'PUBLISHED' | 'ARCHIVED';
  codeFilePath?: string;
  fileName?: string;
  fileSize?: number;
  gitCommitHash?: string;
  gitTag?: string;
  isReleased: boolean;
  releaseDate?: string;
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

// Market types
export interface MarketItem {
  id: number;
  name: string;
  description?: string;
  itemType: 'OPERATOR' | 'PACKAGE';
  operatorId?: number;
  packageId?: number;
  featured: boolean;
  averageRating: number;
  ratingsCount: number;
  reviewsCount: number;
  downloadsCount: number;
  viewsCount: number;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED' | 'REMOVED';
  publishedDate?: string;
  businessScenario?: string;
  createdAt: string;
  operatorLanguage?: string;
  operatorDescription?: string;
  packageBusinessScenario?: string;
}

export interface Review {
  id: number;
  marketItemId: number;
  userId: number;
  userName: string;
  content: string;
  rating?: number;
  likesCount: number;
  parentId?: number;
  replies?: Review[];
  createdAt: string;
}

// Form types
export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
  fullName?: string;
}

export interface OperatorRequest {
  name: string;
  description?: string;
  language: 'JAVA' | 'GROOVY';
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  version?: string;
  isPublic?: boolean;
  parameters?: ParameterRequest[];
}

export interface ParameterRequest {
  name: string;
  description?: string;
  parameterType: 'STRING' | 'INTEGER' | 'FLOAT' | 'BOOLEAN' | 'JSON' | 'FILE' | 'DATE' | 'ARRAY';
  ioType: 'INPUT' | 'OUTPUT';
  isRequired?: boolean;
  defaultValue?: string;
  validationRules?: string;
  orderIndex?: number;
}

export interface PackageRequest {
  name: string;
  description?: string;
  businessScenario: string;
  status?: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  icon?: string;
  isPublic?: boolean;
}

export interface TaskRequest {
  taskName: string;
  taskType: 'OPERATOR_EXECUTION' | 'PACKAGE_EXECUTION';
  operatorId?: number;
  packageId?: number;
  operatorVersionId?: number;
  packageVersionId?: number;
  inputParameters?: Record<string, any>;
  priority?: number;
  timeoutSeconds?: number;
}

export interface MarketSearchRequest {
  keyword?: string;
  itemType?: 'OPERATOR' | 'PACKAGE';
  sortBy?: string;
  sortOrder?: 'asc' | 'desc';
  page?: number;
  size?: number;
}

export interface RatingRequest {
  rating: number;
}

export interface ReviewRequest {
  content: string;
  rating?: number;
}
