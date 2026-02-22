# 算子管理模块功能设计

> **版本**: v1.1
> **创建日期**: 2026-02-22
> **最后更新**: 2026-02-22
> **状态**: 与代码实现一致

---

## 1. 模块概述

算子管理模块负责算子的全生命周期管理，包括创建、编辑、发布、删除等核心功能，以及算子的参数管理、代码编辑、业务逻辑编辑、公共库依赖管理等扩展功能。

### 1.1 核心目标

- 提供可视化的算子管理界面
- 支持算子的多种配置（参数、代码、业务逻辑、数据格式、生成方式）
- 支持算子引用公共库，实现代码复用
- 支持算子的状态管理（草稿、已发布、已归档）

### 1.2 业务场景

1. **算子开发流程**
   - 开发者创建算子，定义算子基本信息
   - 配置算子参数（输入参数、输出参数）
   - 编写算子代码（Groovy 或 Java）
   - 使用业务逻辑编辑器设计流程图
   - 引用公共库中的代码
   - 测试算子（待开发）
   - 发布算子到市场

2. **算子复用流程**
   - 从公共库中选择需要的代码库
   - 将算子打包到算子包中
   - 配置打包路径
   - 预览打包结果
   - 发布算子包到市场

---

## 2. 核心功能清单

### 2.1 基础 CRUD 功能

- ✅ 创建算子
- ✅ 查询算子列表（支持搜索、筛选、分页）
- ✅ 获取算子详情
- ✅ 更新算子
- ✅ 删除算子

### 2.2 参数管理

- ✅ 参数类型支持
  - STRING（字符串）
  - INTEGER（整数）
  - FLOAT（浮点数）
  - BOOLEAN（布尔值）
  - JSON（JSON 对象）
  - FILE（文件）
  - DATE（日期）
  - ARRAY（数组）
- ✅ 参数方向
  - INPUT（输入参数）
  - OUTPUT（输出参数）
- ✅ 参数配置
  - 必填/非必填
  - 默认值
  - 验证规则
  - 排序
- ✅ 参数管理界面
  - 动态添加/删除参数
  - 拖拽排序
  - 参数编辑表单

### 2.3 代码编辑

- ✅ Monaco Editor 集成
  - 支持 Groovy 语法高亮
  - 代码提示和自动补全
  - 格式化功能
- ✅ 文件管理
  - 显示算子代码文件列表
  - 支持多个代码文件
  - 文件上传（可选）

### 2.4 业务逻辑编辑

- ✅ ByteMD 集成
  - Markdown 文本编辑
  - Mermaid 图表支持
  - 实时预览
- ✅ 分屏显示
  - 左侧编辑
  - 右侧预览（Markdown + Mermaid）

### 2.5 数据格式和生成方式配置

- ✅ 数据格式选择
  - 静态 MML（格式 1）
  - 动态 MML（格式 10）
  - 话统（格式 12）
- ✅ 生成方式选择
  - 动态生成
  - 静态生成
- ✅ 代码路径配置
  - 代码文件路径
  - 文件名配置

### 2.6 公共库依赖管理

- ✅ 查看算子依赖的公共库列表
- ✅ 添加公共库依赖
  - 公共库选择弹窗
  - 支持关键词搜索
  - 支持按类型筛选（常量、方法、模型、自定义）
  - 单选公共库
- ✅ 移除公共库依赖
  - 二次确认
  - 支持批量移除
- ✅ **批量更新公共库依赖**（新增）
  - 算子列表页面支持批量选择算子
  - 批量操作按钮：批量更新公共库依赖
  - Transfer 穿梭框选择多个公共库
  - 支持搜索公共库
  - 同时更新多个算子的公共库依赖
  - 自动处理依赖关系（保留、删除、添加）
- ✅ **重要设计原则**
  - 算子不指定公共库版本
  - 版本在算子包层面统一管理
  - 避免数据冗余

### 2.7 状态管理

- ✅ 算子状态类型
  - DRAFT（草稿）
  - PUBLISHED（已发布）
  - ARCHIVED（已归档）
- ✅ 状态切换
  - 草稿 → 发布
  - 发布 → 归档

### 2.8 其他功能

- ✅ 算子标签管理
- ✅ 公开/私有设置
- ✅ 下载次数统计
- ✅ 精选标记
- ✅ 算子详情页展示
  - 基本信息
  - 参数列表
  - 业务逻辑
  - 代码编辑器
  - 公共库依赖列表

---

## 3. 数据模型

### 3.1 Operator 实体

```java
@Entity
@Table(name = "operators")
public class Operator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Language language;  // JAVA | GROOVY

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private OperatorStatus status;  // DRAFT | PUBLISHED | ARCHIVED

    @Column(length = 50)
    private String version;

    @Column(length = 255, nullable = false, unique = true)
    private String operatorCode;

    @Column(length = 255, nullable = false)
    private String objectCode;

    @Column(length = 10)
    private DataFormat dataFormat;  // 1 | 10 | 12

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Generator generator;  // dynamic | static

    @Column(columnDefinition = "TEXT")
    private String businessLogic;

    @Column(length = 20)
    private String codeFilePath;

    @Column(length = 255)
    private String fileName;

    @Column
    private Long fileSize;

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Integer downloadsCount;

    @Column(nullable = false)
    private Boolean featured;

    @Column(length = 100)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // 关联关系
    @OneToMany(mappedBy = "operator", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<Parameter> parameters;

    @OneToMany(mappedBy = "operator", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperatorCommonLibrary> commonLibraries;
}
```

### 3.2 Parameter 实体

```java
@Entity
@Table(name = "operator_parameters")
public class Parameter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private ParameterType type;  // STRING | INTEGER | FLOAT | BOOLEAN | JSON | FILE | DATE | ARRAY

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private ParameterIOType ioType;  // INPUT | OUTPUT

    @Column(nullable = false)
    private Boolean isRequired;

    @Column(length = 500)
    private String defaultValue;

    @Column(columnDefinition = "TEXT")
    private String validationRules;

    @Column(nullable = false)
    private Integer orderIndex;
}
```

### 3.3 OperatorCommonLibrary 实体

```java
@Entity
@Table(name = "operator_common_libraries",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_operator_library",
               columnNames = {"operator_id", "library_id"}
           )
       })
public class OperatorCommonLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private CommonLibrary library;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
```

---

## 4. API 接口清单

### 4.1 基础 CRUD 接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/v1/operators` | 查询算子列表（支持搜索、筛选、分页） | 是 |
| POST | `/v1/operators` | 创建算子 | 是 |
| GET | `/v1/operators/{id}` | 获取算子详情 | 是 |
| PUT | `/v1/operators/{id}` | 更新算子 | 是 |
| DELETE | `/v1/operators/{id}` | 删除算子 | 是 |

### 4.2 公共库依赖接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/v1/operators/{id}/library-dependencies` | 获取算子依赖的公共库列表 | 是 |
| POST | `/v1/operators/{id}/library-dependencies` | 添加公共库依赖 | 是 |
| DELETE | `/v1/operators/{id}/library-dependencies/{libraryId}` | 移除公共库依赖 | 是 |
| POST | `/v1/operators/batch-library-dependencies` | 批量更新多个算子的公共库依赖 | 是 |

---

## 5. 前端页面清单

### 5.1 算子列表页面

- **路由**: `/operators`
- **路径**: `operator-manager-web/src/pages/operator/list.tsx`
- **功能**:
  - 算子列表展示（卡片或表格）
  - 搜索和筛选
  - 分页
  - 创建新算子
  - **批量操作**（新增）：
    - 批量选择算子
    - 批量更新公共库依赖
- **筛选条件**:
  - 关键词搜索
  - 语言筛选（Java / Groovy）
  - 状态筛选（草稿 / 已发布 / 已归档）

### 5.2 算子创建/编辑页面

- **路由**: `/operators/create` 和 `/operators/:id/edit`
- **路径**: `operator-manager-web/src/pages/operator/create.tsx`
- **功能**:
  - 分步表单（5个步骤）
  - Step 1: 基本信息
  - Step 2: 参数管理
  - Step 3: 代码编辑
  - Step 4: 业务逻辑编辑
  - Step 5: 公共库依赖
  - 保存草稿 / 发布

### 5.3 算子详情页面

- **路由**: `/operators/:id`
- **路径**: `operator-manager-web/src/pages/operator/detail.tsx`
- **功能**:
  - 基本信息展示
  - 参数列表展示
  - 业务逻辑展示（Markdown + Mermaid 渲染）
  - 代码展示
  - 公共库依赖列表
  - 编辑 / 删除 / 发布操作

### 5.4 关键组件

| 组件名称 | 路径 | 用途 |
|---------|------|------|
| ParameterForm.tsx | components/operator/ | 参数表单（支持拖拽排序） |
| CodeEditor.tsx | components/code/ | Monaco Editor 代码编辑器 |
| BusinessLogicEditor.tsx | components/editor/ | 业务逻辑编辑器（左侧编辑，右侧预览） |
| LibrarySelectorModal.tsx | components/library/ | 公共库选择弹窗 |
| **BatchLibraryDependenciesModal.tsx** | **components/operator/** | **批量更新公共库依赖弹窗（新增）** |

---

## 6. 关键技术方案

### 6.1 参数管理方案

**动态表单**：
- 使用 Ant Design 的 Form.List 组件
- 支持动态添加/删除参数行
- 每个参数是一个独立的 Form 实例
- 支持拖拽排序（使用 react-dnd）

**参数验证规则**：
- 字符串类型：可选长度限制
- 数值类型：范围验证
- 布尔类型：无额外验证
- JSON 类型：JSON 格式验证
- 文件类型：文件类型限制
- 日期类型：日期格式验证
- 数组类型：元素类型验证

### 6.2 代码编辑方案

**Monaco Editor 配置**：
- 使用 Web Worker 避免警告
- 配置 Groovy 语言支持
- 支持代码格式化
- 支持自动保存（Ctrl+S）

**多文件支持**：
- 左侧文件列表，右侧代码编辑器
- 文件切换功能
- 文件状态管理（isDirty、isSaving）

### 6.3 业务逻辑编辑方案

**ByteMD 集成**：
- Markdown 文本编辑器
- 支持 Mermaid 图表语法
- 实时预览（左侧编辑，右侧预览）
- 导出 Markdown 和 HTML

**数据格式**：
- 编辑器内容存储在 `businessLogic` 字段（TEXT 类型）
- 支持图片上传（可选，通过 MinIO）

### 6.4 公共库依赖方案

**设计原则**：
- 算子不指定公共库版本
- 版本在算子包层面统一管理
- 避免数据冗余

**实现方式**：
- 使用 LibrarySelectorModal 组件
- 支持关键词搜索
- 支持按类型筛选（常量、方法、模型、自定义）
- 单选公共库

---

## 7. 已知限制和注意事项

### 7.1 功能限制

1. **算子版本管理**
   - 当前不支持算子的多版本管理
   - 所有修改在同一版本上进行

2. **算子测试**
   - 当前不支持算子测试功能
   - 无法在线测试算子逻辑

### 7.2 技术注意事项

1. **参数数据类型**
   - API 使用 `ioType`（INPUT / OUTPUT）
   - 前端 UI 使用 `direction`
   - 需要注意类型转换

2. **公共库依赖**
   - 算子添加公共库时，不指定版本
   - 版本由算子包在打包时统一管理

3. **代码文件上传**
   - 当前使用字符串存储代码内容
   - 文件上传通过 MinIO 存储（可选功能）

---

## 8. 后续优化方向

### 8.1 高优先级

1. **算子版本管理**
   - 支持算子的多版本
   - 版本对比功能
   - 版本回滚功能

### 8.2 中优先级

2. **算子测试功能**
   - 单元测试
   - 集成测试
   - 测试用例管理

4. **算子导入导出**
   - 支持从文件导入算子
   - 支持导出算子为文件

### 8.3 低优先级

5. **算子模板**
   - 提供常用算子模板
   - 快速创建算子

6. **算子复制**
   - 支持复制现有算子
   - 自动生成唯一的 operatorCode

---

## 9. 变更历史

| 日期 | 版本 | 变更内容 | 作者 |
|------|------|---------|------|
| 2026-02-22 | v1.1 | 新增：批量更新算子公共库依赖功能<br/>- 后端：新增批量更新 API<br/>- 前端：新增批量选择和批量更新弹窗<br/>- 文档：更新功能设计文档 | Claude |
| 2026-02-22 | v1.0 | 初始版本，基于当前代码实现创建 | Claude |

---

## 10. 相关文档

- [系统总览文档](./system-overview.md)
- [算子包管理模块](./package-management.md)
- [公共库管理模块](./library-management.md)
- [开发规范](../standards/development-conventions.md)
