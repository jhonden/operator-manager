# 算子管理系统功能设计总览

> **版本**: v1.0
> **创建日期**: 2026-02-22
> **最后更新**: 2026-02-22
> **状态**: 与代码实现一致
> **用途**: 新会话启动时阅读，快速了解系统全貌

---

## 1. 系统概述

算子管理系统是一个基于算子（Operator）和算子包（Operator Package）的业务流程管理平台，支持算子的创建、配置、打包、发布和执行。

### 1.1 核心目标

- 提供可视化的算子管理界面
- 支持算子的复用和组合
- 实现公共代码库的共享机制
- 提供灵活的打包配置方案

### 1.2 技术栈

| 层次 | 技术选型 |
|------|---------|
| 前端框架 | React 18 + TypeScript 5.x + Vite 5.x |
| UI 组件库 | Ant Design 5.x |
| 状态管理 | Zustand |
| 路由 | React Router 6.x |
| HTTP 客户端 | Axios |
| 代码编辑器 | Monaco Editor（VS Code 同款） |
| 文档编辑器 | ByteMD（支持 Mermaid） |
| 图表 | ECharts |
| 后端框架 | Spring Boot 3.2.x |
| Java 版本 | Java 21 LTS |
| 数据库 | PostgreSQL 15+ |
| 缓存 | Redis 7.x |
| 文件存储 | MinIO（S3 兼容） |
| 认证 | JWT + Spring Security |
| 数据库迁移 | Flyway |
| API 文档 | SpringDoc OpenAPI |

---

## 2. 功能模块划分

系统按业务领域划分为以下核心模块：

| 模块名称 | 负责领域 | 状态 | 文档 |
|---------|-----------|------|------|
| 算子管理 | 算子的 CRUD、参数配置、代码编辑、业务逻辑编辑、版本管理 | 已完成 | [operator-management.md](./operator-management.md) |
| 算子包管理 | 算子包 CRUD、算子组合、打包配置、公共库同步、打包预览 | 已完成 | [package-management.md](./package-management.md) |
| 公共库管理 | 公共库 CRUD、文件管理、版本管理、类型分类 | 已完成 | [library-management.md](./library-management.md) |
| 用户与认证 | 用户注册登录、JWT 认证、权限管理、会话管理 | 已完成 | - |
| 分类管理 | 算子和算子包的分类管理 | 已完成 | - |
| 执行与调度 | 算子和算子包的执行、任务调度、执行历史 | 未开始 | - |
| 市场与分享 | 算子和算子包的发布、搜索、评价 | 未开始 | - |

---

## 3. 模块设计文档索引

各模块的详细设计文档索引：

### 3.1 核心模块

#### [算子管理模块](./operator-management.md)
- **负责领域**: 算子的全生命周期管理
- **核心功能**:
  - 算子 CRUD（增删改查）
  - 参数管理（多种参数类型）
  - 业务逻辑编辑（Markdown + Mermaid）
  - 代码编辑（Monaco Editor）
  - 数据格式和生成方式配置
  - 公共库依赖管理
  - 状态管理（草稿、已发布、已归档）
- **API 接口**: `/v1/operators/*`
- **前端页面**: `/operators/*`
- **最后更新**: 2026-02-22

#### [算子包管理模块](./package-management.md)
- **负责领域**: 算子包的创建、配置和打包
- **核心功能**:
  - 算子包 CRUD
  - 算子组合（添加、移除、排序）
  - 公共库自动同步（从算子依赖同步到算子包）
  - 打包配置（打包模板选择、路径配置）
  - 打包预览（树形结构展示、冲突检测）
- **API 接口**: `/v1/packages/*`
- **前端页面**: `/packages/*`
- **最后更新**: 2026-02-22

#### [公共库管理模块](./library-management.md)
- **负责领域**: 公共代码库的定义和管理
- **核心功能**:
  - 公共库 CRUD
  - 文件管理（多文件支持）
  - 代码编辑（Monaco Editor）
  - 类型分类（常量、方法、模型、自定义）
  - 版本管理
- **API 接口**: `/v1/libraries/*`
- **前端页面**: `/libraries/*`
- **最后更新**: 2026-02-22

### 3.2 支撑模块

#### 用户与认证模块
- 用户注册、登录、JWT Token 认证
- 角色和权限管理（基于角色的访问控制 RBAC）
- 会话管理

#### 分类管理模块
- 算子和算子包的分类管理
- 支持多级分类

### 3.3 待开发模块

#### 执行与调度模块
- 算子独立执行、批量执行
- 任务调度（定时任务）
- 执行历史和日志

#### 市场与分享模块
- 算子和算子包的市场发布
- 搜索和筛选
- 用户评价和评分

---

## 4. 模块间依赖关系

```
┌─────────────────────────────────────────────────────────────┐
│                     用户与认证模块                        │
│                          ↓                               │
│  ┌─────────────────────────────────────────────────────┐  │
│  │               公共库管理模块                    │  │
│  │                                                    │  │
│  │         ┌───────────┐         ┌───────────┐   │  │
│  │         │ 算子管理 │         │算子包管理 │   │  │
│  │         └─────┬─────┘         └─────┬─────┘   │  │
│  │               ↓                       ↓            │  │
│  │         ┌─────────────────────────────────┐           │  │
│  │         │     执行与调度模块         │           │  │
│  │         └─────────────────────────────────┘           │  │
│  └─────────────────────────────────────────────────────┘  │
│                                                     │
│              ┌─────────────────────────────────┐            │
│              │     市场与分享模块         │            │
│              └─────────────────────────────────┘            │
└─────────────────────────────────────────────────────────────┘
```

**依赖说明**：
- 所有模块都依赖「用户与认证」模块
- 「算子管理」和「算子包管理」依赖「公共库管理」模块
- 「执行与调度」模块依赖「算子管理」和「算子包管理」模块
- 「市场与分享」模块依赖「算子管理」和「算子包管理」模块

---

## 5. 数据模型关系

### 5.1 核心实体关系图

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

### 5.2 关键设计原则

1. **算子和公共库解耦**
   - 算子不包含打包路径配置
   - 公共库是纯粹的代码库定义
   - 打包配置在算子包层面统一管理

2. **灵活复用**
   - 同一个算子可以在不同算子包中使用不同的打包路径
   - 同一个公共库可以被多个算子和多个算子包引用

3. **向后兼容**
   - 支持 Legacy 和 Modern 两种打包模板
   - 兼容现有的算子包格式

---

## 6. 技术架构要点

### 6.1 前端架构

```
operator-manager-web/
├── src/
│   ├── api/              # API 调用函数
│   ├── components/        # 可复用组件
│   ├── pages/            # 路由页面
│   ├── stores/           # Zustand 状态管理
│   ├── hooks/           # 自定义 React Hooks
│   ├── types/           # TypeScript 类型定义
│   └── utils/           # 工具函数
```

**技术亮点**：
- React 18 Hooks 全量使用
- TypeScript 严格类型检查
- Zustand 轻量级状态管理
- Vite 快速热更新

### 6.2 后端架构

```
operator-manager/
├── operator-api/         # API 层（REST 控制器）
├── operator-service/      # 业务逻辑层（Service 实现）
├── operator-core/        # 领域层（JPA 实体和仓库）
├── operator-common/       # 公共模块（DTO、枚举、工具）
└── operator-infrastructure/  # 基础设施层（MinIO、Git、Docker、Redis）
```

**技术亮点**：
- 领域驱动设计（DDD）
- 清晰的分层架构
- Spring Boot 3.2 + Java 21
- Spring Security + JWT 认证

### 6.3 数据库设计

- PostgreSQL 关系型数据库
- Flyway 数据库版本管理
- 外键约束和级联删除
- 索引优化

---

## 7. API 路由总览

### 7.1 API 基础路径

```
POST   /api/v1/auth/login                   # 用户登录
POST   /api/v1/auth/register               # 用户注册
GET    /api/v1/operators                   # 查询算子列表
POST   /api/v1/operators                   # 创建算子
GET    /api/v1/operators/{id}             # 获取算子详情
PUT    /api/v1/operators/{id}             # 更新算子
DELETE /api/v1/operators/{id}             # 删除算子
POST   /api/v1/operators/{id}/library-dependencies       # 添加公共库依赖
DELETE /api/v1/operators/{id}/library-dependencies/{libraryId} # 移除公共库依赖
GET    /api/v1/packages                    # 查询算子包列表
POST   /api/v1/packages                    # 创建算子包
GET    /api/v1/packages/{id}              # 获取算子包详情
PUT    /api/v1/packages/{id}              # 更新算子包
DELETE /api/v1/packages/{id}              # 删除算子包
POST   /api/v1/packages/{id}/operators/{operatorId}/sync-libraries # 同步公共库
GET    /api/v1/packages/{id}/path-config  # 获取打包配置
PUT    /api/v1/packages/{id}/config        # 更新打包配置
GET    /api/v1/packages/{id}/preview       # 获取打包预览
GET    /api/v1/libraries                   # 查询公共库列表
POST   /api/v1/libraries                   # 创建公共库
GET    /api/v1/libraries/{id}             # 获取公共库详情
PUT    /api/v1/libraries/{id}             # 更新公共库
DELETE /api/v1/libraries/{id}             # 删除公共库
```

### 7.2 Swagger 文档

- URL: `http://localhost:8080/swagger-ui.html`
- 自动生成 API 文档
- 支持在线测试

---

## 8. 前端路由总览

```
/                           # 首页（市场）
/auth                       # 认证相关
  /login                      # 登录页
  /register                   # 注册页
/operators                   # 算子管理
  /                          # 算子列表
  /:id                       # 算子详情
  /create                    # 创建算子
  /:id/edit                  # 编辑算子
/packages                   # 算子包管理
  /                          # 算子包列表
  /:id                       # 算子包详情
  /create                    # 创建算子包
  /:id/edit                  # 编辑算子包
/libraries                  # 公共库管理
  /                          # 公共库列表
  /:id                       # 公共库详情
  /create                    # 创建公共库
  /:id/edit                  # 编辑公共库
  /:id/code-editor           # 公共库代码编辑器
```

---

## 9. 开发规范索引

| 文档名称 | 路径 | 用途 |
|---------|------|------|
| 开发规范 | [../standards/development-conventions.md](../standards/development-conventions.md) | 编码规范、前后端开发规范、测试规范 |
| 代码提交流程 | [../standards/code-submission-workflow.md](../standards/code-submission-workflow.md) | 修改验证、提交流程 |
| 前后端协同开发规范 | [../standards/frontend-backend-collaboration.md](../standards/frontend-backend-collaboration.md) | 前后端开发流程、测试规范 |
| 需求设计工作流程 | [../standards/requirements-design-workflow.md](../standards/requirements-design-workflow.md) | 需求讨论、方案设计、确认归档流程 |
| 项目关键约束 | [../standards/project-constraints.md](../standards/project-constraints.md) | 技术栈约束、功能范围约束 |

---

## 10. 已知限制和注意事项

### 10.1 功能限制

1. **算子版本管理**
   - 当前不支持算子的多版本管理
   - 所有修改在同一版本上进行

2. **执行与调度**
   - 执行功能尚未实现
   - 没有任务调度系统

3. **国际化**
   - 当前仅支持中文界面
   - i18n 基础设施已就绪，但未实现语言切换

### 10.2 技术注意事项

1. **后端 API 设计**
   - 所有 API 需要认证（除登录注册外）
   - 使用 JWT Token，放在 `Authorization: Bearer <token>` 头中

2. **数据库操作**
   - 使用 JPA 的级联删除
   - 删除实体时自动删除关联数据
   - 注意外键约束

3. **前端状态管理**
   - 优先使用 Zustand 全局状态
   - 组件内部状态使用 useState
   - 避免不必要的状态提升

---

## 11. 变更历史

| 日期 | 版本 | 变更内容 | 作者 |
|------|------|---------|------|
| 2026-02-22 | v1.0 | 初始版本，创建模块功能设计体系 | Claude |

---

## 12. 快速导航

**新会话启动建议**：

1. 阅读 CLAUDE.md 了解项目概况
2. 点击 CLAUDE.md 中的知识索引链接进入本文档
3. 阅读本文档了解系统全貌和模块划分
4. 根据需要跳转到具体的模块文档深入了解

**开发新功能前建议**：

1. 先阅读相关模块的详细设计文档
2. 了解现有的数据模型、API 接口、前端页面
3. 参考模块文档的"已知限制和注意事项"
4. 设计新方案时考虑与现有功能的兼容性
5. 完成开发后更新对应的模块文档

---

**相关文档**：

- [CLAUDE.md](../CLAUDE.md) - 项目总体入口
- [开发规范](../standards/development-conventions.md) - 编码规范
- [前后端协同开发规范](../standards/frontend-backend-collaboration.md) - 开发流程
- [需求设计文档目录](../requirements/) - 优化需求历史记录
