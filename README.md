# 算子管理系统

基于算子和算子包的业务流程管理平台，支持算子的创建、配置、打包、发布和执行。

## 项目状态

### ✅ 已完成模块

| 模块 | 核心功能 | 状态 |
|------|---------|------|
| **算子管理** | 算子 CRUD、参数管理、业务逻辑编辑、代码编辑、公共库依赖管理 | 已完成 |
| **算子包管理** | 算子包 CRUD、算子组合、公共库自动同步、打包配置、打包预览 | 已完成 |
| **公共库管理** | 公共库 CRUD、文件管理、代码编辑、类型分类、版本管理 | 已完成 |
| **用户与认证** | 用户注册登录、JWT 认证、权限管理、会话管理 | 已完成 |
| **分类管理** | 算子和算子包的分类管理 | 已完成 |

### 🚧 待开发模块

- **执行与调度模块**：算子执行、任务调度、执行历史
- **市场与分享模块**：算子发布、搜索、评价

---

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 3.2.x | Web 框架 |
| Java | 21 LTS | 编程语言 |
| PostgreSQL | 15+ | 主数据库 |
| Redis | 7.x | 缓存和队列 |
| MinIO | - | 文件存储（S3 兼容） |
| Spring Security | - | 安全框架 |
| JWT | - | 认证方式 |
| Flyway | - | 数据库迁移 |
| SpringDoc OpenAPI | - | API 文档 |
| Maven | 3.9.x | 构建工具 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| React | 18 | UI 框架 |
| TypeScript | 5.x | 类型检查 |
| Vite | 5.x | 构建工具 |
| Ant Design | 5.x | UI 组件库 |
| Zustand | - | 状态管理 |
| React Router | 6.x | 路由管理 |
| Axios | - | HTTP 客户端 |
| Monaco Editor | - | 代码编辑器 |
| ByteMD | - | Markdown 编辑器（支持 Mermaid） |
| ECharts | - | 图表库 |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                   前端 (React)                         │
│   Ant Design + Zustand + React Router + Monaco Editor   │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓ REST API
┌─────────────────────────────────────────────────────────┐
│                   API 层 (Spring Boot)                  │
│   Controllers + Security + Exception Handlers + OpenAPI  │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────┐
│                 Service 层                              │
│    Business Logic + Transaction Management + DTO Map   │
└─────────────────────────────────────────────────────────┘
                            │
            ┌───────────────┼───────────────┐
            ↓               ↓               ↓
┌─────────────────┬─────────────────┬─────────────────┐
│  Core 层        │  基础设施层     │  Common 层      │
│  (Entities +    │  (MinIO, Git,   │  (Security +    │
│   Repositories) │   Docker, Redis)│   Utils + DTOs) │
└─────────────────┴─────────────────┴─────────────────┘
            │               │               │
            ↓               ↓               ↓
┌─────────────────┬─────────────────┬─────────────────┐
│  PostgreSQL     │  Redis          │  MinIO          │
│  (主数据库)     │  (缓存 + 队列)  │  (文件存储)     │
└─────────────────┴─────────────────┴─────────────────┘
```

---

## 快速开始

### 前置要求

- JDK 21+
- Node.js 18+
- Maven 3.9+
- PostgreSQL 15+（或使用 H2）
- Redis 7.x
- MinIO（可选，用于文件存储）

### 一键启动（推荐）

```bash
# 启动所有服务（前端 + 后端 + 数据库，默认使用 PostgreSQL）
./start-all.sh

# 启动所有服务（使用 H2 数据库）
./start-all.sh local h2

# 启动所有服务（Docker 模式）
./start-all.sh docker

# 停止所有服务
./stop-all.sh
```

### 单独启动服务

#### 后端服务

```bash
# 本地模式（使用 PostgreSQL，默认）
./start-backend-local.sh

# 本地模式（使用 H2）
./start-backend-local.sh h2

# Docker 模式
./start-backend.sh
```

#### 前端服务

```bash
# 启动前端开发服务器
./start-frontend.sh
```

### 访问服务

| 服务 | URL |
|------|-----|
| 前端应用 | http://localhost:5173 |
| 后端 API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |
| MinIO Console | http://localhost:9001 |

### 默认管理员账号

- 用户名：`admin`
- 密码：`admin123`

---

## 项目结构

```
operator-manager/
├── operator-api/                  # API 层
│   └── src/main/java/com/operator/api/
│       ├── controller/            # REST 控制器
│       ├── handler/              # 全局异常处理器
│       ├── security/             # 安全配置
│       └── config/               # 配置类
├── operator-core/                # 领域层
│   └── src/main/java/com/operator/core/
│       ├── domain/               # JPA 实体
│       └── repository/          # Spring Data JPA 仓库接口
├── operator-service/             # 业务逻辑层
│   └── src/main/java/com/operator/service/
│       ├── operator/             # 算子服务
│       ├── pkg/                  # 算子包服务
│       ├── library/              # 公共库服务
│       └── auth/                # 认证服务
├── operator-infrastructure/       # 基础设施层
│   └── src/main/java/com/operator/infrastructure/
│       ├── storage/              # MinIO 文件存储服务
│       ├── git/                  # Git 集成服务
│       ├── scheduler/            # 任务调度服务
│       └── publisher/            # 发布服务
├── operator-common/               # 公共模块
│   └── src/main/java/com/operator/common/
│       ├── dto/                  # 数据传输对象
│       ├── enums/                # 枚举定义
│       ├── exception/            # 异常类
│       ├── security/             # JWT、UserPrincipal
│       └── wrapper/             # API 响应包装
├── operator-manager-web/         # 前端项目
│   ├── src/
│   │   ├── api/                # API 调用函数
│   │   ├── components/          # 可复用组件
│   │   ├── pages/              # 路由页面
│   │   ├── stores/             # Zustand 状态管理
│   │   ├── hooks/              # 自定义 React Hooks
│   │   ├── types/              # TypeScript 类型定义
│   │   └── utils/              # 工具函数
│   ├── package.json
│   └── vite.config.ts
├── docs/                        # 文档目录
│   ├── standards/               # 规约和规范（必读）
│   │   ├── development-conventions.md      # 开发规范
│   │   ├── code-submission-workflow.md    # 代码提交流程
│   │   ├── frontend-backend-collaboration.md  # 前后端协同开发规范
│   │   ├── requirements-design-workflow.md  # 需求设计工作流程
│   │   └── project-constraints.md        # 项目关键约束
│   ├── modules/                 # 功能模块设计文档
│   │   ├── system-overview.md   # 系统功能设计总览
│   │   ├── operator-management.md  # 算子管理模块
│   │   ├── package-management.md   # 算子包管理模块
│   │   └── library-management.md   # 公共库管理模块
│   └── requirements/            # 需求设计文档
├── db/                          # 数据库相关
│   └── migration/              # Flyway 数据库迁移脚本
├── tests/                       # 测试脚本目录
│   └── utils/                  # 测试工具函数
├── start-all.sh                # 启动所有服务
├── stop-all.sh                 # 停止所有服务
├── start-backend.sh            # 后端启动脚本（Docker 模式）
├── start-backend-local.sh      # 后端启动脚本（本地模式）
├── stop-backend-local.sh       # 后端停止脚本
├── start-frontend.sh          # 前端启动脚本
├── stop-frontend.sh           # 前端停止脚本
├── CLAUDE.md                  # Claude AI 助手项目指南
└── pom.xml                    # Maven 父 POM
```

---

## API 文档

### 认证相关

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/v1/auth/login` | 用户登录 |
| POST | `/api/v1/auth/register` | 用户注册 |
| GET | `/api/v1/auth/me` | 获取当前用户信息 |
| POST | `/api/v1/auth/change-password` | 修改密码 |
| POST | `/api/v1/auth/logout` | 用户登出 |

### 算子管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/operators` | 查询算子列表 |
| POST | `/api/v1/operators` | 创建算子 |
| GET | `/api/v1/operators/{id}` | 获取算子详情 |
| PUT | `/api/v1/operators/{id}` | 更新算子 |
| DELETE | `/api/v1/operators/{id}` | 删除算子 |
| POST | `/api/v1/operators/{id}/library-dependencies` | 添加公共库依赖 |
| DELETE | `/api/v1/operators/{id}/library-dependencies/{libraryId}` | 移除公共库依赖 |

### 算子包管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/packages` | 查询算子包列表 |
| POST | `/api/v1/packages` | 创建算子包 |
| GET | `/api/v1/packages/{id}` | 获取算子包详情 |
| PUT | `/api/v1/packages/{id}` | 更新算子包 |
| DELETE | `/api/v1/packages/{id}` | 删除算子包 |
| POST | `/api/v1/packages/{id}/operators/{operatorId}` | 添加算子到包 |
| DELETE | `/api/v1/packages/{id}/operators/{operatorId}` | 从包中移除算子 |
| POST | `/api/v1/packages/{id}/operators/{operatorId}/sync-libraries` | 同步公共库到算子包 |
| GET | `/api/v1/packages/{id}/path-config` | 获取打包配置 |
| PUT | `/api/v1/packages/{id}/config` | 更新打包配置 |
| GET | `/api/v1/packages/{id}/preview` | 获取打包预览 |

### 公共库管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/v1/libraries` | 查询公共库列表 |
| POST | `/api/v1/libraries` | 创建公共库 |
| GET | `/api/v1/libraries/{id}` | 获取公共库详情 |
| PUT | `/api/v1/libraries/{id}` | 更新公共库 |
| DELETE | `/api/v1/libraries/{id}` | 删除公共库 |

更多 API 文档请访问：http://localhost:8080/swagger-ui.html

---

## 前端路由

```
/                           # 首页（市场）
/auth                       # 认证相关
  /login                     # 登录页
  /register                  # 注册页
/operators                   # 算子管理
  /                          # 算子列表
  /:id                       # 算子详情
  /create                    # 创建算子
  /:id/edit                  # 编辑算子
/packages                    # 算子包管理
  /                          # 算子包列表
  /:id                       # 算子包详情
  /create                    # 创建算子包
  /:id/edit                  # 编辑算子包
/libraries                   # 公共库管理
  /                          # 公共库列表
  /:id                       # 公共库详情
  /create                    # 创建公共库
  /:id/edit                  # 编辑公共库
  /:id/code-editor           # 公共库代码编辑器
```

---

## 开发规范

在开始开发前，请务必阅读以下规约文档：

| 文档 | 路径 | 用途 |
|------|------|------|
| **开发规范** | [docs/standards/development-conventions.md](./docs/standards/development-conventions.md) | 编码规范、前后端开发规范、测试规范 |
| **代码提交流程** | [docs/standards/code-submission-workflow.md](./docs/standards/code-submission-workflow.md) | 代码修改、验证、提交流程 |
| **前后端协同开发规范** | [docs/standards/frontend-backend-collaboration.md](./docs/standards/frontend-backend-collaboration.md) | 前后端开发流程、测试规范 |
| **需求设计工作流程** | [docs/standards/requirements-design-workflow.md](./docs/standards/requirements-design-workflow.md) | 需求讨论、方案设计、确认归档流程 |
| **项目关键约束** | [docs/standards/project-constraints.md](./docs/standards/project-constraints.md) | 技术栈约束、功能范围约束 |

### 关键原则

1. **代码修改后必须验证**：编译、启动、功能测试通过后才能提交
2. **提交前必须得到用户确认**：未经用户确认不得提交代码
3. **文档和注释使用中文**：除技术术语外，优先使用中文
4. **Git 提交信息使用中文**：清晰描述改动内容
5. **前后端协同先做后端**：后端开发完成并测试通过后，再开发前端

---

## 环境变量配置

编辑 `operator-api/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/operator_manager_dev
    username: dev_user
    password: dev_pass
  data:
    redis:
      host: localhost
      port: 6379

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin

jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时
```

---

## 添加新功能

### 后端开发

1. 在 `operator-core` 中创建实体类（Entity）
2. 在 `operator-core` 中创建仓库接口（Repository）
3. 在 `operator-common` 中创建 DTO 类（DTO）
4. 在 `operator-service` 中创建服务实现（Service）
5. 在 `operator-api` 中创建控制器（Controller）
6. 编写测试脚本到 `tests/` 目录

### 前端开发

1. 在 `src/api/` 中创建 API 调用函数
2. 在 `src/types/` 中定义 TypeScript 类型
3. 如需要，在 `src/stores/` 中创建 Zustand store
4. 在 `src/pages/` 中创建页面组件
5. 在 `App.tsx` 中添加路由配置
6. 遵循前后端协同开发规范：**先完成后端再开发前端**

---

## 需求设计文档

查看需求设计文档：[需求文档目录](./docs/requirements/README.md)

---

## 数据模型关系

```
User (用户)
  ├─ Operator (算子)
  │    ├─ Parameter (参数)
  │    ├─ OperatorCommonLibrary (算子-公共库关联)
  │    │    └─ CommonLibrary (公共库)
  │    │         └─ CommonLibraryFile (公共库文件)
  │    └─ PackageOperator (算子包-算子关联)
  │
  └─ OperatorPackage (算子包)
       ├─ PackageOperator (算子包-算子关联) ← 关联 Operator
       └─ PackageCommonLibrary (算子包-公共库关联)
            └─ CommonLibrary (公共库)
```

---

## 许可证

Apache License 2.0

---

## 团队

Operator Manager 开发团队

---

## 版本

1.0.0-SNAPSHOT
