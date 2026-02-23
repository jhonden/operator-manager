# 算子包管理模块功能设计

> **版本**: v1.3
> **创建日期**: 2026-02-22
> **最后更新**: 2026-02-23
> **状态**: 与代码实现一致

---

## 1. 模块概述

算子包管理模块负责算子包的创建、配置和管理，支持将多个算子组合成一个算子包，并提供灵活的打包配置方案，包括打包模板选择、路径配置、打包预览等功能。

### 1.1 核心目标

- 提供算子包的创建、编辑、删除功能
- 支持算子的组合和排序
- 支持公共库的自动同步和路径配置
- 提供打包结构预览和冲突检测
- 支持多种打包模板（Legacy、Modern、Custom）
- 支持算子包打包下载

### 1.2 业务场景

1. **算子包创建流程**
   - 用户创建算子包，定义基本信息（名称、描述、业务场景）
   - 选择打包模板（Legacy / Modern / Custom）
   - 添加算子到算子包（自动同步算子的公共库依赖）
   - 配置算子和公共库的打包路径
   - 预览打包结构
   - 检查冲突和警告
   - 发布算子包

2. **算子包维护流程**
   - 查看算子包详情
   - 添加/移除算子（自动同步公共库）
   - 调整算子顺序
   - 修改打包配置
   - 更新算子包信息

3. **算子包下载流程**
   - 用户从市场下载算子包
   - 根据打包模板解析算子包结构
   - 使用算子和公共库执行业务流程

4. **算子包打包下载流程**
   - 用户在算子包详情页点击"打包下载"按钮
   - 后端生成包含算子代码、公共库文件和元数据信息的 ZIP 压缩包
   - 浏览器自动下载压缩包到本地
   - 压缩包可导入到运行态系统使用

---

## 2. 核心功能清单

### 2.1 基础 CRUD 功能

- ✅ 创建算子包
- ✅ 查询算子包列表（支持搜索、筛选、分页）
- ✅ 获取算子包详情
- ✅ 更新算子包
- ✅ 删除算子包

### 2.2 算子管理

- ✅ 添加算子到算子包
  - 算子选择弹窗（支持多选）
  - 支持搜索算子名称
  - 支持按编程语言筛选
  - 统一设置执行顺序
  - 显示已选算子数量
  - 自动触发公共库同步
  - 支持单个添加和批量添加
- ✅ 移除算子
  - 支持单个移除
  - 支持批量移除（勾选多个算子，填写移除原因）
- ✅ 算子排序
  - ✅ 编辑单个算子的执行顺序（点击 orderIndex 编辑）
  - ✅ 批量编辑算子的执行顺序（勾选多个算子）
  - orderIndex 可重复（支持并行执行）
  - orderIndex 验证（必须 >= 1）
  - ⚠️ 上移/下移（已移除）
    - ~~上移算子~~
    - ~~下移算子~~
- ✅ 算子配置
  - 启用/禁用
  - 参数映射（待开发）
  - 自定义路径（待开发）

### 2.3 公共库管理（设计变更）

- ✅ **自动同步机制**
  - 算子添加到算子包时，自动同步算子的公共库依赖
  - 算子移除时，自动移除对应的公共库
  - 算子更新公共库依赖时，自动同步到算子包
- ❌ **已移除的手动管理功能**
  - ~~手动添加公共库到算子包~~
  - ~~手动从算子包移除公共库~~
- ✅ 公共库路径配置
  - 支持自定义路径开关
  - 支持输入自定义路径模板
  - 显示推荐的默认路径

### 2.4 打包配置

- ✅ 打包模板选择
  - Legacy 模板（兼容现有格式）
  - Modern 模板（推荐新格式）
  - Custom 模板（完全自定义）
- ✅ 打包路径配置
  - 算子路径配置
  - 公共库路径配置
  - 支持路径变量（${operatorCode}、${fileName} 等）
- ✅ 打包预览
  - 树形结构展示
  - 标识文件来源（算子/库/元数据）
  - 冲突检测
  - 警告提示
- ✅ 批量配置
  - 批量使用推荐路径
  - 批量设置自定义路径
- ✅ 打包下载
  - 一键打包下载功能
  - 自动生成 ZIP 压缩包
  - 包含算子代码、公共库文件和元数据文件
  - 压缩包命名：operator_package_{packageName}.zip

### 2.5 其他功能

- ✅ 算子包状态管理（草稿、已发布、已归档）
- ✅ 标签管理
- ✅ 公开/私有设置
- ✅ 下载次数统计
- ✅ 精选标记
- ✅ 业务场景字段
- ✅ 版本号字段

---

## 3. 数据模型

### 3.1 OperatorPackage 实体

```java
@Entity
@Table(name = "operator_packages")
public class OperatorPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 255)
    private String businessScenario;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private PackageStatus status;  // DRAFT | PUBLISHED | ARCHIVED

    @Column(length = 255)
    private String icon;

    @Column(length = 50)
    private String version;

    @Column(length = 50)
    private String packageTemplate;  // legacy | modern | custom

    @Column(nullable = false)
    private Boolean isPublic;

    @Column(nullable = false)
    private Integer downloadsCount;

    @Column(nullable = false)
    private Boolean featured;

    @Column(nullable = false)
    private Integer operatorCount;

    @Column(length = 100)
    private String createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    // 关联关系
    @OneToMany(mappedBy = "operatorPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<PackageOperator> operators;

    @OneToMany(mappedBy = "operatorPackage", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<PackageCommonLibrary> commonLibraries;
}
```

### 3.2 PackageOperator 实体

```java
@Entity
@Table(name = "package_operators",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_package_operator",
               columnNames = {"package_id", "operator_id"}
           )
       })
public class PackageOperator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private OperatorPackage operatorPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(length = 50)
    private String version;

    @Column(columnDefinition = "TEXT")
    private String parameterMapping;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 500)
    private String customPackagePath;

    @Column(nullable = false)
    private Boolean useCustomPath;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}
```

### 3.3 PackageCommonLibrary 实体

```java
@Entity
@Table(name = "package_common_libraries",
       uniqueConstraints = {
           @UniqueConstraint(
               name = "idx_pkg_operator_library_unique",
               columnNames = {"package_id", "operator_id", "library_id"}
           )
       })
public class PackageCommonLibrary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id", nullable = false)
    private OperatorPackage operatorPackage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "operator_id", nullable = false)
    private Operator operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_id", nullable = false)
    private CommonLibrary library;

    @Column(length = 50)
    private String version;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(length = 500)
    private String customPackagePath;

    @Column(nullable = false)
    private Boolean useCustomPath;

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
| GET | `/v1/packages` | 查询算子包列表 | 是 |
| POST | `/v1/packages` | 创建算子包 | 是 |
| GET | `/v1/packages/{id}` | 获取算子包详情 | 是 |
| PUT | `/v1/packages/{id}` | 更新算子包 | 是 |
| DELETE | `/v1/packages/{id}` | 删除算子包 | 是 |

### 4.2 算子管理接口

| 方法 | 路径 | 描述 | 认证 |
| POST | `/v1/packages/{id}/operators` | 添加算子到算子包（自动同步公共库） | 是 |
| POST | `/v1/packages/{id}/operators/batch` | 批量添加算子到算子包（自动同步公共库） | 是 |
| POST | `/v1/packages/{id}/operators/batch-delete` | 批量移除算子 | 是 |
| PUT | `/v1/packages/{id}/operators/{packageOperatorId}` | 更新算子配置 | 是 |
| DELETE | `/v1/packages/{id}/operators/{packageOperatorId}` | 移除算子 | 是 |
| PUT | `/v1/packages/{id}/operators/{operatorId}/order` | 更新单个算子的执行顺序 | 是 |
| PUT | `/v1/packages/{id}/operators/batch-update-order` | 批量更新算子执行顺序 | 是 |

### 4.3 公共库管理接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/v1/packages/{id}/operators/{operatorId}/sync-libraries` | 同步算子的公共库到算子包 | 是 |
| PUT | `/v1/packages/{id}/libraries/{libraryId}/path-config` | 配置单个公共库路径 | 是 |
| PUT | `/v1/packages/{id}/libraries/batch-path-config` | 批量配置公共库路径 | 是 |

### 4.4 算子包导入接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| POST | `/v1/packages/import` | 导入算子包（ZIP 文件） | 是 |

**导入接口说明**：
- **请求格式**：`multipart/form-data`
- **请求参数**：`file: File`（ZIP 压缩包文件，最大 10MB）
- **响应格式（成功）**：
  ```json
  {
    "success": true,
    "message": "算子包导入成功",
    "data": {
      "id": 123,
      "name": "my_package",
      "operatorsUpdated": 3,
      "operatorsCreated": 2,
      "librariesUpdated": 5,
      "librariesCreated": 1
    }
  }
  ```
- **导入策略**：
  - 算子（Operator）：按编码智能复用（存在则更新基本信息和代码）
  - 公共库（CommonLibrary）：按名称智能复用（存在则删除旧文件，替换为新文件）
  - 算子包（OperatorPackage）：始终新建（名称冲突自动加后缀）
  - Legacy 模板：`operators/constants/`、`operators/method/`、`models/` 文件夹合并为单独的库
- **数据完整性验证**：
  - 元数据文件必须存在（`metainfo_operators.yml`）
  - 元数据中的算子必须有对应的代码文件
  - 公共库文件内容不能为空

### 4.4 打包配置接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| GET | `/v1/packages/{id}/path-config` | 获取打包路径配置 | 是 |
| PUT | `/v1/packages/{id}/config` | 更新算子包整体配置 | 是 |
| GET | `/v1/packages/{id}/preview` | 获取打包预览 | 是 |
| PUT | `/v1/packages/{id}/operators/{operatorId}/path-config` | 更新算子路径配置 | 是 |
| PUT | `/v1/packages/{id}/operators/batch-path-config` | 批量更新算子路径配置 | 是 |
| GET | `/v1/packages/{id}/download` | 下载算子包 ZIP | 是 |

**下载接口说明**：
- 响应类型：`application/zip`（二进制文件流）
- 响应头：`Content-Disposition: attachment; filename="operator_package_{packageName}.zip"`
- 压缩包结构：
  - 根路径：`{packageName}/{version}/`
  - 算子代码：`operators/groovy/{operatorCode}.groovy`（Legacy 模板）
  - 公共库：根据库类型分配到不同目录
  - 元数据文件：`operators/metainfo_operators.yml`（包含算子包和算子元数据）

### 4.5 其他接口

| 方法 | 路径 | 描述 | 认证 |
|------|------|------|------|
| PATCH | `/v1/packages/{id}/status` | 更新算子包状态 | 是 |
| PATCH | `/v1/packages/{id}/featured` | 切换精选状态 | 是 |
| POST | `/v1/packages/{id}/download` | 下载算子包 | 是 |
| GET | `/v1/packages/my-packages` | 获取我的算子包 | 是 |
| GET | `/v1/packages/search` | 搜索算子包 | 是 |

---

## 5. 前端页面清单

### 5.1 算子包列表页面

- **路由**: `/packages`
- **路径**: `operator-manager-web/src/pages/package/list.tsx`
- **功能**:
  - 算子包列表展示（卡片布局）
  - 搜索和筛选
  - 分页
  - 创建新算子包
- **筛选条件**:
  - 关键词搜索
  - 状态筛选（草稿 / 已发布 / 已归档）

### 5.2 算子包创建/编辑页面

- **路由**: `/packages/create` 和 `/packages/:id/edit`
- **路径**: `operator-manager-web/src/pages/package/create.tsx`
- **功能**:
  - 基本信息表单
  - 打包模板选择
  - 保存草稿 / 发布

### 5.3 算子包详情页面

- **路由**: `/packages/:id`
- **路径**: `operator-manager-web/src/pages/package/detail.tsx`
- **功能**:
  - 基本信息展示
  - 算子管理标签页
    - 算子列表
    - 添加/移除/排序算子
    - 参数映射（待开发）
  - 公共库配置标签页
    - 自动同步的公共库列表
    - 公共库路径配置
  - 打包配置标签页
    - 打包模板选择
    - 打包结构预览（树形展示）
    - 冲突和警告检测
    - 算子路径配置
    - 公共库路径配置
    - 打包下载按钮（一键下载 ZIP 压缩包）
  - 数据流标签页（待开发）

### 5.4 关键组件

| 组件名称 | 路径 | 用途 |
|---------|------|------|
| 路径编辑弹窗 | pages/package/detail.tsx（内嵌） | 编辑单个算子或公共库路径 |
| 批量配置弹窗 | pages/package/detail.tsx（内嵌） | 批量使用推荐路径 |
| 打包预览组件 | pages/package/detail.tsx（内嵌） | 树形结构预览 |

---

## 6. 关键技术方案

### 6.1 公共库自动同步方案

**核心设计**：
- 算子添加到算子包时，自动触发公共库同步
- 删除该算子在 `package_common_libraries` 中的旧记录
- 为算子的每个公共库依赖创建新的 `package_common_libraries` 记录

**同步时机**：
1. 添加算子到算子包时
2. 算子添加/移除公共库依赖时（自动同步到所有包含该算子的算子包）

**优点**：
- 消除数据冗余
- 确保数据一致性
- 避免手动管理错误

### 6.2 打包模板方案

**Legacy 模板（兼容现有格式）**：
```
{包名}/
├── {版本号}/                       # 根路径包含版本号
│   ├── models/                     # 类型为 MODEL 的库
│   │   └── ${fileName}
│   └── operators/
│       ├── constants/              # 类型为 CONSTANT 的库
│       │   └── ${fileName}
│       ├── groovy/                # 算子代码
│       │   └── ${operatorCode}.groovy
│       ├── method/                # 类型为 METHOD 的库
│       │   └── ${fileName}
│       └── metainfo_operators.yml # 元数据文件（YAML 格式）
```

**Modern 模板（推荐新格式）**：
```
{包名}/
├── {版本号}/                       # 根路径包含版本号
│   ├── lib/                       # 所有公共库
│   │   └── ${libraryName}/
│   │       └── ${fileName}
│   ├── operators/                  # 算子代码
│   │   └── ${operatorCode}/
│   │       └── ${fileName}
│   ├── package.json
│   └── manifest.json
```

**Custom 模板（完全自定义）**：
- 每个资源都可以单独配置自定义打包路径
- 完全灵活的打包结构

### 6.3 路径变量方案

**支持的变量**：
| 变量 | 说明 | 示例值 |
|-----|------|--------|
| `${libraryName}` | 公共库名称 | `DateUtils` |
| `${libraryVersion}` | 公共库版本 | `1.0` |
| `${operatorCode}` | 算子编码 | `my_operator` |
| `${packageName}` | 算子包名称 | `my_package` |
| `${fileName}` | 文件名 | `DateUtils.groovy` |
| `${fileExt}` | 文件扩展名 | `.groovy` |

### 6.4 默认路径规则表

| 资源类型 | 库类型 | Legacy 模板路径 | Modern 模板路径 |
|---------|-------|----------------|----------------|
| 公共库 | CONSTANT | `{version}/operators/constants/${fileName}` | `{version}/lib/${libraryName}/${fileName}` |
| 公共库 | METHOD | `{version}/operators/method/${fileName}` | `{version}/lib/${libraryName}/${fileName}` |
| 公共库 | MODEL | `{version}/models/${fileName}` | `{version}/lib/${libraryName}/${fileName}` |
| 公共库 | CUSTOM | `{version}/lib/${libraryName}/${fileName}` | `{version}/lib/${libraryName}/${fileName}` |
| 算子代码 | - | `{version}/operators/groovy/${operatorCode}.groovy` | `{version}/operators/${operatorCode}/${fileName}` |
| 元数据文件 | - | `{version}/operators/metainfo_operators.yml` | `{version}/manifest.json` |

**注意**：
- 路径中的 `{version}` 会替换为算子包的版本号，默认为 `1.0.0`
- 元数据文件在 Legacy 模板中为 `metainfo_operators.yml`（YAML 格式），在 Modern 模板中为 `manifest.json`（JSON 格式）

---

## 7. 已知限制和注意事项

### 7.1 功能限制

1. **算子包版本管理**
   - 当前不支持算子包的多版本管理
   - 所有修改在同一版本上进行

3. **参数映射**
   - 参数映射功能尚未实现
   - 无法在算子包级别配置算子参数

4. **打包下载**
   - 仅支持 Legacy 模板的完整实现（包括元数据文件）
   - Modern 和 Custom 模板的元数据文件生成待完善
   - 打包过程在内存中进行，大文件可能占用较多内存

### 7.2 技术注意事项

1. **公共库同步**
   - 算子包的公共库来自算子依赖
   - `package_common_libraries` 表包含 `operator_id` 字段记录来源算子
   - 删除算子时，关联的 `package_common_libraries` 会级联删除

2. **路径配置**
   - 算子和公共库都可以使用自定义路径
   - `use_custom_path` 标识是否使用自定义路径
   - 未使用自定义路径时，根据模板自动计算推荐路径

3. **打包预览**
   - 预览基于当前配置实时生成
   - 不会保存到数据库
   - 用于用户确认打包结构

---

## 8. 后续优化方向

### 8.1 高优先级

1. **~批量配置公共库功能~** ✅ 已完成
   - ✅ 在公共库配置标签页添加批量配置按钮
   - ✅ 支持批量使用推荐路径
   - ✅ 后端批量更新 API 已实现

2. **~打包下载功能~** ✅ 已完成
   - ✅ 后端 PackageBuildService 实现打包逻辑
   - ✅ 前端下载按钮和处理函数
   - ✅ 自动生成元数据文件
   - ✅ 浏览器自动下载 ZIP 压缩包

3. **参数映射功能**
   - 在算子包层面配置算子参数
   - 支持参数映射和重命名
   - 支持参数默认值覆盖

### 8.2 中优先级

3. **算子包版本管理**
   - 支持算子包的多版本
   - 版本对比功能
   - 版本回滚功能

4. **~算子包导入功能~** ✅ 已完成
   - ✅ 后端 PackageImportService 实现导入逻辑
   - ✅ 后端 PackageImportController 提供导入接口
   - ✅ 前端导入按钮和文件上传功能
   - ✅ 支持 ZIP 文件解析
   - ✅ 智能复用算子和公共库
   - ✅ 数据完整性验证
   - ✅ 算子包名称冲突处理
   - ✅ Legacy 模板库合并（constants、method、models）
   - ✅ 正确建立包级别和算子级别关联关系

### 8.3 低优先级

5. **算子包模板**
   - 提供常用算子包模板
   - 快速创建算子包

6. **算子包复制**
   - 支持复制现有算子包
   - 自动生成唯一的配置

---

## 9. 变更历史

| 日期 | 版本 | 变更内容 | 作者 |
|------|------|---------|------|
| 2026-02-22 | v1.0 | 初始版本，基于当前代码实现创建 | Claude |
| 2026-02-23 | v1.1 | 新增打包下载功能，更新打包模板路径（添加版本号），更新已知限制和优化方向 | Claude |
| 2026-02-23 | v1.2 | 新增批量添加算子功能，支持多选、搜索、语言筛选，优化界面布局（执行顺序置顶，确认按钮固定底部） | Claude |
| 2026-02-23 | v1.3 | 新增批量移除算子功能，支持勾选多个算子后批量移除，填写移除原因 | Claude |
| 2026-02-24 | v1.4 | 新增算子包导入功能，支持 ZIP 文件导入、智能复用算子和公共库、数据完整性验证、Legacy 模板库合并，更新 API 接口清单和已知限制 | Claude |

---

## 10. 相关文档

- [系统总览文档](./system-overview.md)
- [算子管理模块](./operator-management.md)
- [公共库管理模块](./library-management.md)
- [算子包打包路径配置与公共库管理需求设计](../requirements/2026-02-21-算子包打包路径配置与公共库管理-需求设计.md)
