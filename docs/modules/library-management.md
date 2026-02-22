# 公共库管理模块功能设计

> **版本**: v1.0
> **创建日期**: 2026-02-22
> **最后更新**: 2026-02-22
> **状态**: 与代码实现一致

---

## 1. 模块概述

公共库管理模块负责公共代码库的定义、编辑和管理，支持用户创建可复用的代码库，并在多个算子和算子包之间共享这些代码。

### 1.1 核心目标

- 提供可视化的公共库管理界面
- 支持公共库的 CRUD 操作
- 支持公共库的多文件管理
- 支持代码编辑（Monaco Editor）
- 支持按类型分类（常量、方法、模型、自定义）

### 1.2 业务场景

1. **公共库创建流程**
   - 开发者创建公共库，定义基本信息（名称、描述、版本、类型、分类）
   - 添加代码文件
   - 编写代码（支持多个文件）
   - 保存公共库

2. **公共库使用流程**
   - 算子编辑时，选择公共库并添加依赖
   - 算子包添加算子时，自动同步算子的公共库依赖
   - 配置公共库的打包路径
   - 预览和发布算子包

3. **公共库维护流程**
   - 查看公共库详情
   - 编辑基本信息
   - 添加/删除/重命名代码文件
   - 编辑代码内容
   - 更新版本

---

## 2. 核心功能清单

### 2.1 基础 CRUD 功能

- ✅ 创建公共库
- ✅ 查询公共库列表（支持搜索、筛选、分页）
- ✅ 获取公共库详情
- ✅ 更新公共库
- ✅ 删除公共库

### 2.2 文件管理

- ✅ 多文件支持
  - 创建库时支持添加多个文件
  - 文件列表展示
  - 文件排序
- ✅ 文件操作
  - 添加文件
  - 删除文件
  - 重命名文件
- ✅ 文件状态管理
  - 已保存（绿色图标）
  - 已修改未保存（黄色图标）
  - 保存中（提示文字）
  - 字符数统计

### 2.3 代码编辑

- ✅ Monaco Editor 集成
  - 支持 Groovy 语法高亮
  - 代码提示和自动补全
  - 格式化功能
- ✅ 分屏显示
  - 左侧：文件列表
  - 右侧：代码编辑器
- ✅ 代码保存
  - Ctrl+S 快捷键保存
  - 只保存当前修改的文件（isDirty=true）
  - 保存成功提示
- ✅ 文件切换功能
  - 切换文件时自动保存未保存的修改
  - 检查未保存文件

### 2.4 分类管理

- ✅ 库类型分类
  - CONSTANT（常量）
  - METHOD（方法）
  - MODEL（模型）
  - CUSTOM（自定义）
- ✅ 业务分类
  - 自定义分类字段
  - 支持按分类筛选

### 2.5 版本管理

- ✅ 版本字段
  - 每个公共库有版本号
  - 创建时指定版本
  - 更新时可修改版本
- ✅ 使用情况统计
  - 记录公共库被多少算子使用
  - 显示在列表和详情页

### 2.6 其他功能

- ✅ 公共库详情展示
  - 基本信息
  - 代码文件列表
  - 代码编辑器
  - 使用情况统计
- ✅ 搜索和筛选
  - 关键词搜索
  - 按类型筛选
  - 按分类筛选
- ✅ 删除保护
  - 如果有算子正在使用该库，禁止删除
  - 如果有算子包正在使用该库，禁止删除
  - 显示删除限制原因

---

## 3. 数据模型

### 3.1 CommonLibrary 实体

```java
@Entity
@Table(name = "common_libraries")
public class CommonLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String version;

    @Column(length = 100)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private LibraryType libraryType;  // CONSTANT | METHOD | MODEL | CUSTOM

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // 关联关系
    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<CommonLibraryFile> files;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    private List<OperatorCommonLibrary> operators;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL)
    private List<PackageCommonLibrary> packages;
}
```

### 3.2 CommonLibraryFile 实体

```java
@Entity
@Table(name = "common_library_files")
public class CommonLibraryFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private CommonLibrary library;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(length = 500)
    private String filePath;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
```

---

## 4. API 接口清单

### 4.1 基础 CRUD 接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/v1/libraries` | 查询公共库列表（支持搜索、筛选、分页） | 是 |
| POST | `/v1/libraries` | 创建公共库 | 是 |
| GET | `/v1/libraries/{id}` | 获取公共库详情 | 是 |
| PUT | `/v1/libraries/{id}` | 更新公共库 | 是 |
| DELETE | `/v1/libraries/{id}` | 删除公共库 | 是 |

### 4.2 查询接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/v1/libraries/search` | 搜索公共库（关键词） | 是 |
| GET | `/v1/libraries/type/{type}` | 按类型查询 | 是 |
| GET | `/v1/libraries/category/{category}` | 按分类查询 | 是 |

### 4.3 文件管理接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/v1/libraries/{libraryId}/files` | 创建库文件 | 是 |
| PUT | `/v1/libraries/{libraryId}/files/{fileId}` | 更新库文件名 | 是 |
| PUT | `/v1/libraries/{libraryId}/files/{fileId}/content` | 更新库文件内容 | 是 |
| DELETE | `/v1/libraries/{libraryId}/files/{fileId}` | 删除库文件 | 是 |

---

## 5. 前端页面清单

### 5.1 公共库列表页面

- **路由**: `/libraries`
- **路径**: `operator-manager-web/src/pages/library/list.tsx`
- **功能**:
  - 公共库列表展示（卡片布局）
  - 搜索和筛选
  - 分页
  - 创建新公共库
- **筛选条件**:
  - 关键词搜索
  - 库类型筛选（常量 / 方法 / 模型 / 自定义）
  - 分类筛选

### 5.2 公共库代码编辑器页面

- **路由**: `/libraries/:id/code-editor`
- **路径**: `operator-manager-web/src/pages/library/code-editor.tsx`
- **功能**:
  - VS Code 风格布局
  - 左侧：文件列表
    - 文件切换
    - 添加文件（弹出对话框）
    - 删除文件
    - 重命名文件
    - 显示文件状态（已保存 / 已修改 / 保存中）
    - 字符数统计
  - 右侧：代码编辑器
    - Monaco Editor 集成
    - Groovy 语法高亮
    - 代码格式化
    - Ctrl+S 保存
    - 显示当前文件状态
  - 返回列表页时检查未保存文件
- **文件名编辑**:
  - 点击文件名旁的编辑图标（笔）
  - 文件名变为可编辑输入框
  - 显示确认图标（勾号）
  - 支持 Enter 键确认
  - 失焦自动保存

### 5.3 关键组件

| 组件名称 | 路径 | 用途 |
|---------|------|------|
| CodeEditor.tsx | components/code/ | Monaco Editor 代码编辑器 |
| LibraryFormModal.tsx | components/library/ | 公共库创建/编辑弹窗 |

---

## 6. 关键技术方案

### 6.1 文件列表状态管理

**状态标识**：
1. `isDirty`: 标记文件是否有未保存的修改
2. `isSaving`: 标记文件是否正在保存
3. `originalCode`: 保存从数据库读取的原始代码
4. `currentCode`: 当前编辑器中的代码

**自动检测修改**：
- 对比 `currentCode` 和 `originalCode`
- 如果不一致，设置 `isDirty = true`
- 如果一致，设置 `isDirty = false`

**文件图标**：
- 🟢 已保存（CheckCircleOutlined）
- 🟡 已修改未保存（EditOutlined）
- 🔵 保存中（文字提示）

### 6.2 代码编辑器方案

**Monaco Editor 配置**：
- 使用 Web Worker 避免警告
- 配置 Groovy 语言支持
- 支持代码格式化
- 支持自动保存（Ctrl+S）

**使用 isUpdatingValueRef**：
- 区分用户编辑和 setValue 触发的 onChange
- 避免切换文件时误触发保存

### 6.3 删除保护机制

**删除前检查**：
1. 查询 `operator_common_libraries` 表，检查是否有算子正在使用该库
2. 查询 `package_common_libraries` 表，检查是否有算子包正在使用该库

**删除限制提示**：
- 如果有算子或算子包在使用，显示错误消息
- 禁止删除操作
- 提示用户先移除引用

---

## 7. 已知限制和注意事项

### 7.1 功能限制

1. **公共库版本管理**
   - 当前不支持公共库的多版本管理
   - 所有修改在同一版本上进行

2. **文件上传**
   - 当前使用字符串存储代码内容
   - 文件上传通过 MinIO 存储（可选功能，但未实现）

3. **代码预置库**
   - 当前不支持预置常用公共库
   - 需要用户手动创建

### 7.2 技术注意事项

1. **文件操作**
   - 文件删除和重命名直接更新数据库
   - 删除文件时会自动从数据库中移除
   - 代码内容实时保存到前端状态

2. **代码保存**
   - 点击"保存"按钮或按 Ctrl+S
   - 只保存当前修改的文件（isDirty=true）
   - 保存成功后显示提示消息
   - 返回列表页时检查是否有未保存的文件

3. **依赖关系**
   - 公共库被多个算子引用时，不能删除
   - 公共库被多个算子包使用时，不能删除
   - 需要显示具体的使用情况

---

## 8. 后续优化方向

### 8.1 高优先级

1. **公共库版本管理**
   - 支持公共库的多版本
   - 版本对比功能
   - 版本回滚功能

2. **文件上传功能**
   - 支持从本地文件系统上传代码文件
   - 支持 MinIO 文件存储
   - 支持大文件分片上传

### 8.2 中优先级

3. **公共库模板**
   - 提供常用公共库模板
   - 快速创建公共库

4. **公共库导入导出**
   - 支持从文件导入公共库
   - 支持导出公共库为文件

### 8.3 低优先级

5. **公共库市场**
   - 公共库的发布和订阅
   - 公共库评价和评分
   - 使用统计

6. **公共库复制**
   - 支持复制现有公共库
   - 自动生成唯一的名称

---

## 9. 变更历史

| 日期 | 版本 | 变更内容 | 作者 |
|------|------|---------|------|
| 2026-02-22 | v1.0 | 初始版本，基于当前代码实现创建 | Claude |

---

## 10. 相关文档

- [系统总览文档](./system-overview.md)
- [算子管理模块](./operator-management.md)
- [算子包管理模块](./package-management.md)
- [开发规范](../standards/development-conventions.md)
