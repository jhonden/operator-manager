# Operator Manager 项目指南

本文档是 Operator Manager 项目的总体入口文档。本文档将指导你理解项目结构、开发规范、关键约束和工作流程。

---

## 📋 新会话开始必读

**⚠️ 重要：在开始任何开发工作之前，必须完成以下阅读步骤！**

### 第一步：理解开发规范

必须阅读以下规范文档，理解项目开发要求：

1. **[开发规范](./docs/standards/development-conventions.md)** ⭐⭐⭐
   - 文档和注释语言规范
   - 后端开发规范（Java 21、日志、事务等）
   - 前端开发规范（TypeScript、组件、样式等）
   - 测试规范
   - 安全规范（输入验证、SQL 注入防护、XSS 防护）

2. **[代码提交流程](./docs/standards/code-submission-workflow.md)** ⭐⭐⭐⭐⭐
   - 修改后必须验证
   - 验证步骤（编译、启动、功能测试）
   - 只有验证通过才能提交
   - 提交前必须得到用户确认
   - 服务启动验证要求

3. **[需求设计工作流程](./docs/standards/requirements-design-workflow.md)** ⭐⭐⭐⭐⭐
   - 需求讨论流程
   - 方案设计流程
   - 方案确认和归档流程
   - 实施规划要求

### 第二步：理解项目结构

阅读下面的**项目目录结构树**章节，了解项目组织结构和关键文件位置。

### 第三步：理解项目关键约束

阅读 **[项目关键约束](./docs/standards/project-constraints.md)**，了解：
- 技术栈约束（前端框架选型等）
- 功能范围约束（必须实现和已移除的功能）
- 工作流程约束（关键决策流程）

### 第四步：确认理解

在开始工作前，确认你已经：

- [ ] 阅读并理解了开发规范
- [ ] 阅读并理解了代码提交流程
- [ ] 阅读并理解了项目关键约束
- [ ] 阅读并理解了需求设计工作流程（如需进行需求设计）
- [ ] 了解了项目目录结构
- [ ] 理解了关键约束和禁止行为

### 关键约束提醒

**✅ 必须遵守：**
- 代码修改后必须验证，验证通过才能提交
- 提交前必须得到用户确认
- 文档和注释使用中文（除技术术语）
- Git 提交信息使用中文

**❌ 严格禁止：**
- 修改代码后立即提交（未验证）
- 在编译错误时提交
- 未进行功能测试时提交
- 未经用户确认就提交代码

---

## 📁 项目目录结构树

### 根目录

```
operator-manager/
├── operator-api/                    # API 层 - REST 控制器、处理器、安全配置
├── operator-core/                   # 领域层 - JPA 实体和仓库
├── operator-service/                # 业务逻辑层 - Service 实现
├── operator-infrastructure/           # 基础设施层 - MinIO、Git、Docker、Redis
├── operator-common/                 # 公共模块 - JWT、异常、API 包装器、DTO、枚举
├── operator-manager-web/            # 前端项目 - React + TypeScript + Vite
├── docs/                          # 文档目录
│   ├── standards/                    # 规约和规范（⭐ 必读）
│   │   ├── development-conventions.md      # 开发规范
│   │   └── code-submission-workflow.md    # 代码提交流程
│   └── requirements/                 # 需求设计文档
│       └── 2026-02-20-算子基本信息扩展-需求设计.md
│       └── 2026-02-20-算子业务逻辑字段.md
├── db/                            # 数据库相关
│   └── migration/                  # Flyway 数据库迁移脚本
│       ├── V1__init_schema.sql
│       ├── V2__add_operator_fields.sql
│       └── V3__add_business_logic.sql
├── tests/                         # 测试脚本目录
├── start-backend.sh                # 后端启动脚本（Docker 模式）
├── start-backend-local.sh           # 后端启动脚本（本地调试模式，无 Docker）
├── start-frontend.sh               # 前端启动脚本
├── CLAUDE.md                      # 本文件 - 项目总体入口
└── .cursorrules                    # 代码提交流程（已迁移到 docs/standards/）
```

### 关键文件说明

| 文件路径 | 用途 | 重要程度 |
|---------|------|---------|
| [开发规范](./docs/standards/development-conventions.md) | 项目编码规范（语言、后端、前端、测试、安全）| ⭐⭐⭐ |
| [代码提交流程](./docs/standards/code-submission-workflow.md) | 代码修改、验证、提交流程 | ⭐⭐⭐⭐⭐ |
| [数据库初始化](./db/migration/V1__init_schema.sql) | 数据库表结构初始化脚本 | ⭐⭐ |
| [数据库迁移脚本](./db/migration/) | Flyway 数据库迁移脚本 | ⭐⭐⭐ |
| [Operator 实体](./operator-core/src/main/java/com/operator/core/operator/domain/Operator.java) | 算子领域实体 | ⭐⭐⭐⭐ |
| [OperatorController](./operator-api/src/main/java/com/operator/api/controller/OperatorController.java) | 算子 REST API 控制器 | ⭐⭐⭐ |
| [OperatorServiceImpl](./operator-service/src/main/java/com/operator/service/operator/OperatorServiceImpl.java) | 算子业务逻辑实现 | ⭐⭐⭐⭐ |
| [前端类型定义](./operator-manager-web/src/types/index.ts) | TypeScript 类型定义 | ⭐⭐⭐ |
| [前端 API 客户端](./operator-manager-web/src/api/) | 前端 API 调用函数 | ⭐⭐⭐ |
| [前端算子创建页面](./operator-manager-web/src/pages/operator/create.tsx) | 算子创建/编辑页面 | ⭐⭐⭐ |
| [前端算子详情页面](./operator-manager-web/src/pages/operator/detail.tsx) | 算子详情展示页面 | ⭐⭐⭐ |

### 后端模块说明

**operator-api/ (API 层)**
```
operator-api/
├── src/main/java/com/operator/api/
│   ├── controller/                   # REST 控制器
│   │   ├── AuthController.java        # 认证 API
│   │   ├── OperatorController.java     # 算子 API
│   │   ├── PackageController.java     # 算子包 API
│   │   ├── CategoryController.java    # 分类 API
│   │   ├── ExecutionController.java   # 执行 API
│   │   ├── VersionController.java     # 版本 API
│   │   ├── MarketController.java      # 市场 API
│   │   └── UserController.java       # 用户 API
│   ├── handler/                     # 全局异常处理器
│   │   └── GlobalExceptionHandler.java
│   ├── security/                     # 安全配置
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── UserPrincipal.java
│   └── config/                      # 配置类
├── src/main/resources/
│   ├── application.yml               # 主配置文件
│   ├── application-dev.yml           # 开发环境配置
│   └── application-prod.yml          # 生产环境配置
└── pom.xml                        # Maven 配置
```

**operator-core/ (领域层)**
```
operator-core/
├── src/main/java/com/operator/core/
│   ├── domain/                      # JPA 实体
│   │   ├── operator/
│   │   │   ├── Operator.java          # 算子实体
│   │   │   ├── Parameter.java        # 参数实体
│   │   │   └── Category.java         # 分类实体
│   │   ├── user/
│   │   │   └── User.java            # 用户实体
│   │   └── pkg/
│   │       ├── OperatorPackage.java    # 算子包实体
│   │       └── PackageOperator.java    # 包算子关联实体
│   └── repository/                  # Spring Data JPA 仓库接口
└── pom.xml                        # Maven 配置
```

**operator-service/ (业务逻辑层)**
```
operator-service/
├── src/main/java/com/operator/service/
│   ├── operator/
│   │   ├── OperatorService.java      # 算子服务接口
│   │   └── OperatorServiceImpl.java # 算子服务实现
│   ├── pkg/
│   │   ├── PackageService.java       # 算子包服务接口
│   │   └── PackageServiceImpl.java   # 算子包服务实现
│   └── scheduler/                  # 任务调度服务
│       ├── TaskScheduler.java
│       ├── TaskExecutorService.java
│       └── TaskLogWebSocketHandler.java
└── pom.xml                        # Maven 配置
```

**operator-infrastructure/ (基础设施层)**
```
operator-infrastructure/
├── src/main/java/com/operator/infrastructure/
│   ├── storage/
│   │   └── MinioStorageService.java      # MinIO 文件存储服务
│   ├── git/
│   │   └── GitIntegrationService.java    # Git 集成服务
│   ├── scheduler/
│   │   ├── TaskScheduler.java           # 任务调度器
│   │   └── RedisQueueService.java       # Redis 队列服务
│   └── publisher/
│       ├── RestPublisherService.java      # REST 发布服务
│       └── FilePublisherService.java     # 文件发布服务
└── pom.xml                        # Maven 配置
```

**operator-common/ (公共模块)**
```
operator-common/
├── src/main/java/com/operator/common/
│   ├── constants/                   # 常量定义
│   ├── dto/                         # 数据传输对象
│   │   ├── operator/
│   │   │   ├── OperatorRequest.java
│   │   │   └── OperatorResponse.java
│   │   └── pkg/
│   ├── enums/                       # 枚举定义
│   │   ├── LanguageType.java           # 编程语言枚举
│   │   ├── OperatorStatus.java         # 算子状态枚举
│   │   ├── Generator.java             # 生成方式枚举
│   │   └── DataFormat.java           # 数据格式枚举
│   ├── exception/                    # 异常类
│   │   ├── BusinessException.java
│   │   └── ResourceNotFoundException.java
│   ├── security/                     # 安全相关
│   │   ├── JwtTokenProvider.java      # JWT Token 生成器
│   │   └── UserPrincipal.java       # 用户信息
│   ├── util/                        # 工具类
│   ├── validation/                   # 验证注解
│   │   ├── OperatorCode.java          # 算子编码验证注解
│   │   └── OperatorCodeValidator.java # 验证器实现
│   └── wrapper/                     # API 响应包装
│       └── ApiResponse.java           # 统一 API 响应格式
└── pom.xml                        # Maven 配置
```

### 前端项目说明

**operator-manager-web/ (前端项目)**
```
operator-manager-web/
├── src/
│   ├── api/                        # API 调用
│   │   ├── operator.ts             # 算子 API
│   │   ├── auth.ts                 # 认证 API
│   │   └── ...
│   ├── components/                  # 可复用组件
│   │   ├── code/
│   │   │   └── CodeEditor.tsx    # 代码编辑器
│   │   ├── operator/
│   │   │   └── ParameterForm.tsx  # 参数表单
│   │   ├── editor/
│   │   │   ├── BusinessLogicEditor.tsx     # 业务逻辑编辑器
│   │   │   └── BusinessLogicViewer.tsx     # 业务逻辑查看器
│   │   └── common/
│   │       ├── Header.tsx        # 页面头部
│   │       ├── Layout.tsx        # 布局
│   │       └── Sidebar.tsx       # 侧边栏
│   ├── pages/                      # 路由页面
│   │   ├── operator/
│   │   │   ├── create.tsx        # 算子创建/编辑
│   │   │   ├── detail.tsx        # 算子详情
│   │   │   └── list.tsx          # 算子列表
│   │   └── package/
│   ├── stores/                     # Zustand 状态管理
│   ├── hooks/                     # 自定义 React Hooks
│   ├── types/                     # TypeScript 类型定义
│   ├── utils/                     # 工具函数
│   ├── App.tsx                    # 根组件
│   └── main.tsx                   # 入口文件
├── public/                      # 静态资源
├── index.html                   # HTML 模板
├── vite.config.ts               # Vite 配置
├── tsconfig.json               # TypeScript 配置
└── package.json               # NPM 配置
```

---

## Technology Stack

### Backend

- Spring Boot 3.2.x
- Java 21 LTS
- PostgreSQL 15+
- Redis 7.x
- MinIO (S3-compatible storage)
- JWT + Spring Security
- Groovy 4.x (for dynamic operators)
- JGit (Git integration)
- SpringDoc OpenAPI

### Frontend

- React 18 + TypeScript 5.x
- Vite 5.x (build tool)
- Ant Design 5.x (UI library)
- Zustand (state management)
- React Router 6.x
- Axios (HTTP client)
- Monaco Editor (code editor)
- ByteMD (Markdown editor with Mermaid support)
- ECharts (charts)

---

## Service URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |
| MinIO Console | http://localhost:9001 |

---

## Default Admin Credentials

- Username: `admin`
- Password: `admin123`

---

## Adding a New Feature

1. **Backend** - Follows established pattern:
   - Create entity in `operator-core` (domain package)
   - Create repository interface in `operator-core`
   - Create DTOs in `operator-common` (dto package)
   - Create service in `operator-service`
   - Create controller in `operator-api`

2. **Frontend** - Organize by feature:
   - Create API client function in `api/`
   - Create Zustand store if state needed
   - Create page components in `pages/`
   - Add route to `App.tsx`

---

## Testing

The test framework uses shell scripts with curl for API testing:
- Test utilities are in `tests/utils/` (logger.sh, assertions.sh)
- API base URL: `http://localhost:8080/api/v1`
- Test scripts must be executable: `chmod +x <script>.sh`

---

## Error Handling

All API responses use `ApiResponse<T>` wrapper with:
- `success`: boolean
- `message`: string
- `data`: T | null
- `error`: string | null

Global exception handler is in `operator-api/src/main/java/com/operator/api/handler/GlobalExceptionHandler.java`

---

## JWT Authentication

All API endpoints except `/api/v1/auth/*` require a JWT token in the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```
