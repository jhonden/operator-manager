# 公共库功能开发进度

**需求名称**：算子包打包路径配置与公共库管理

**需求设计文档**：`docs/requirements/2026-02-21-算子包打包路径配置与公共库管理-需求设计.md`

**开始日期**：2026-02-21

---

## ✅ 阶段一：数据库和后端基础（完成）

### 1.1 数据库迁移
- **文件**：`db/migration/V5__add_common_library_support.sql`
- **内容**：
  - 创建 `common_libraries` 表（公共库主表）
  - 创建 `common_library_files` 表（公共库文件表）
  - 创建 `operator_common_libraries` 表（算子-公共库关联表）
  - 创建 `package_common_libraries` 表（算子包-公共库关联表）
  - 修改 `package_operators` 表，增加 `custom_package_path` 和 `use_custom_path` 字段
  - 修改 `operator_packages` 表，增加 `package_template` 字段

### 1.2 实体类创建
**文件**：`operator-core/src/main/java/com/operator/core/library/domain/`

- **CommonLibrary.java** - 公共库实体类
  - 字段：id, name, description, version, category, libraryType, createdBy, createdAt, updatedAt
  - 关联：files（一对多）

- **CommonLibraryFile.java** - 公共库文件实体类
  - 字段：id, library, fileName, filePath, code, orderIndex, createdAt, updatedAt

- **OperatorCommonLibrary.java** - 算子-公共库关联实体类
  - 字段：id, operator, library, createdAt, updatedAt

- **PackageCommonLibrary.java** - 算子包-公共库关联实体类
  - 字段：id, operatorPackage, library, version, orderIndex, customPackagePath, useCustomPath, createdAt, updatedAt

### 1.3 修改现有实体
**文件**：`operator-core/src/main/java/com/operator/core/pkg/domain/`

- **OperatorPackage.java** - 添加字段：`packageTemplate`
- **PackageOperator.java** - 添加字段：`customPackagePath`、`useCustomPath`

### 1.4 创建枚举
**文件**：`operator-common/src/main/java/com/operator/common/enums/`

- **LibraryType.java** - 公共库类型枚举（CONSTANT、METHOD、MODEL、CUSTOM）

### 1.5 Repository 接口
**文件**：`operator-core/src/main/java/com/operator/core/library/repository/`

- **CommonLibraryRepository.java** - 公共库 Repository
- **CommonLibraryFileRepository.java** - 公共库文件 Repository
- **OperatorCommonLibraryRepository.java** - 算子-公共库关联 Repository
- **PackageCommonLibraryRepository.java** - 算子包-公共库关联 Repository

### 1.6 DTO 类创建
**文件**：`operator-common/src/main/java/com/operator/common/dto/library/`

- **LibraryRequest.java** - 公共库创建/更新请求
- **LibraryFileRequest.java** - 公共库文件请求
- **LibraryResponse.java** - 公共库响应
- **LibraryFileResponse.java** - 公共库文件响应
- **OperatorPathConfigRequest.java** - 算子路径配置请求
- **LibraryPathConfigRequest.java** - 公共库路径配置请求
- **OperatorPathConfigResponse.java** - 算子路径配置响应
- **LibraryPathConfigResponse.java** - 公共库路径配置响应
- **PackagePathConfigRequest.java** - 算子包路径配置请求
- **PackagePathConfigResponse.java** - 算子包路径配置响应
- **AddLibraryToPackageRequest.java** - 添加公共库到算子包请求
- **BatchPathConfigRequest.java** - 批量路径配置请求
- **LibrarySearchRequest.java** - 搜索请求
- **PackagePreviewTreeNode.java** - 预览树节点
- **PackagePreviewSource.java** - 预览资源来源
- **PackagePreviewConflict.java** - 预览冲突
- **PackagePreviewResponse.java** - 预览响应

### 1.7 修改现有 DTO
**文件**：`operator-common/src/main/java/com/operator/common/dto/pkg/`

- **PackageOperatorResponse.java** - 添加字段：`customPackagePath`、`useCustomPath`
- **PackageResponse.java** - 添加字段：`packageTemplate`、`commonLibraries`

---

## ✅ 阶段二：后端 Service 层（完成）

### 2.1 公共库 Service
**文件**：`operator-service/src/main/java/com/operator/service/library/`

- **CommonLibraryService.java** - 接口定义
- **CommonLibraryServiceImpl.java** - 实现类
  - createLibrary() - 创建公共库
  - updateLibrary() - 更新公共库
  - deleteLibrary() - 删除公共库
  - getLibraryById() - 根据ID获取公共库
  - searchLibraries() - 搜索公共库（支持分页）
  - getLibrariesByType() - 按类型获取公共库
  - getLibrariesByCategory() - 按分类获取公共库

### 2.2 打包路径解析器
**文件**：`operator-service/src/main/java/com/operator/service/library/`

- **PackagePathResolver.java** - 打包路径解析器
  - resolveOperatorPath() - 解析算子打包路径
  - resolveLibraryPath() - 解析公共库打包路径
  - getRecommendedOperatorPath() - 获取算子推荐路径
  - getRecommendedLibraryPath() - 获取公共库推荐路径
  - resolveVariables() - 解析路径变量
  - 支持三种模板：Legacy/Modern/Custom

### 2.3 打包预览服务
**文件**：`operator-service/src/main/java/com/operator/service/library/`

- **PackagePreviewService.java** - 打包预览服务
  - generatePreview() - 生成打包预览
  - buildStructure() - 构建打包结构树
  - detectConflicts() - 检测路径冲突
  - generateWarnings() - 生成警告信息

### 2.4 算子包 Service 扩展
**文件**：`operator-service/src/main/java/com/operator/service/pkg/`

- **PackageService.java** - 接口扩展
  - addLibraryToPackage() - 向算子包添加公共库
  - removeLibraryFromPackage() - 从算子包移除公共库
  - getPackagePathConfig() - 获取算子包打包路径配置
  - updatePackageConfig() - 更新算子包整体配置
  - updateOperatorPathConfig() - 更新算子打包路径配置
  - batchUpdateOperatorPathConfig() - 批量更新算子路径配置
  - updateLibraryPathConfig() - 更新公共库打包路径配置
  - batchUpdateLibraryPathConfig() - 批量更新公共库路径配置
  - **修改的方法**：
    - mapToResponse() - 添加 packageTemplate 和 commonLibraries 字段映射
    - loadPackageCommonLibraries() - 加载公共库配置

---

## ✅ 阶段三：后端 API 层（完成）

### 3.1 公共库 Controller
**文件**：`operator-api/src/main/java/com/operator/api/controller/`

- **LibraryController.java** - 公共库管理 API
  - POST `/v1/libraries` - 创建公共库
  - PUT `/v1/libraries/{id}` - 更新公共库
  - DELETE `/v1/libraries/{id}` - 删除公共库
  - GET `/v1/libraries/{id}` - 根据ID获取公共库详情
  - GET `/v1/libraries` - 搜索公共库（支持分页、关键字、库类型过滤）
  - GET `/v1/libraries/type/{libraryType}` - 按类型获取公共库
  - GET `/v1/libraries/category/{category}` - 按分类获取公共库

### 3.2 算子包预览 Controller
**文件**：`operator-api/src/main/java/com/operator/api/controller/`

- **PackagePreviewController.java** - 打包预览 API
  - GET `/v1/packages/{id}/preview?template={template}` - 获取打包预览

### 3.3 算子包 Controller 扩展
**文件**：`operator-api/src/main/java/com/operator/api/controller/`

- **PackageController.java** - 扩展现有算子包 Controller
  - **新增公共库相关接口**：
    - POST `/v1/packages/{id}/libraries` - 向算子包添加公共库
    - DELETE `/v1/packages/{id}/libraries/{packageCommonLibraryId}` - 从算子包移除公共库
    - GET `/v1/packages/{id}/path-config` - 获取算子包的打包路径配置
    - PUT `/v1/packages/{id}/config` - 更新算子包整体配置
    - PUT `/v1/packages/{id}/operators/{operatorId}/path-config` - 更新算子打包路径配置
    - PUT `/v1/packages/{id}/operators/batch-path-config` - 批量更新算子路径配置
    - PUT `/v1/packages/{id}/libraries/{libraryId}/path-config` - 更新公共库打包路径配置
    - PUT `/v1/packages/{id}/libraries/batch-path-config` - 批量更新公共库路径配置

---

## 📊 阶段四：前端开发（进行中）

### 4.1 类型定义
**文件**：`operator-manager-web/src/types/library.ts`

已创建所有公共库相关的 TypeScript 类型定义：
- `LibraryType` - 枚举（CONSTANT、METHOD、MODEL、CUSTOM）
- `LibraryRequest` - 公共库请求
- `LibraryFileRequest` - 文件请求
- `LibraryResponse` - 公共库响应
- `LibraryFileResponse` - 文件响应
- `OperatorPathConfigRequest` - 算子路径配置请求
- `LibraryPathConfigRequest` - 公共库路径配置请求
- `OperatorPathConfigResponse` - 算子路径配置响应
- `LibraryPathConfigResponse` - 公共库路径配置响应
- `PackagePathConfigRequest` - 算子包路径配置请求
- `PackagePathConfigResponse` - 算子包路径配置响应
- `AddLibraryToPackageRequest` - 添加公共库到算子包请求
- `BatchPathConfigRequest` - 批量路径配置请求
- `LibrarySearchRequest` - 搜索请求
- `PackagePreviewTreeNode` - 预览树节点
- `PackagePreviewSource` - 预览资源来源
- `PackagePreviewConflict` - 预览冲突
- `PackagePreviewResponse` - 预览响应

### 4.2 API 调用函数
**文件**：`operator-manager-web/src/api/library.ts`

已创建公共库 API 调用函数：
- `createLibrary()` - 创建公共库
- `updateLibrary()` - 更新公共库
- `deleteLibrary()` - 删除公共库
- `getLibraryById()` - 获取公共库详情
- `searchLibraries()` - 搜索公共库（支持分页、关键字、库类型过滤）
- `getLibrariesByType()` - 按类型获取公共库
- `getLibrariesByCategory()` - 按分类获取公共库

**文件**：`operator-manager-web/src/api/package.ts`

已扩展算子包 API，添加打包配置相关接口：
- `addLibraryToPackage()` - 向算子包添加公共库
- `removeLibraryFromPackage()` - 从算子包移除公共库
- `getPackagePathConfig()` - 获取算子包打包路径配置
- `updatePackageConfig()` - 更新算子包整体配置
- `updateOperatorPathConfig()` - 更新算子打包路径配置
- `batchUpdateOperatorPathConfig()` - 批量更新算子路径配置
- `updateLibraryPathConfig()` - 更新公共库打包路径配置
- `batchUpdateLibraryPathConfig()` - 批量更新公共库路径配置
- `generatePreview()` - 获取打包预览

---

## 📝 待完成工作

### 阶段四：前端开发（继续进行）
需要创建以下前端页面和组件：

1. **公共库管理页面**
   - 公共库列表页（支持分页、搜索、类型过滤、分类过滤）
   - 公共库创建/编辑弹窗
   - 公共库详情查看

2. **算子包编辑页面扩展**
   - 新增"打包配置"标签页
   - 算子路径配置表格
   - 公共库路径配置表格
   - 批量配置按钮
   - 打包预览功能

3. **弹窗组件**
   - 路径编辑弹窗（支持变量提示）
   - 批量配置弹窗（算子/公共库）

4. **打包预览组件**
   - 树形结构展示
   - 冲突提示
   - 变量说明

### 阶段五：测试和优化（待开始）

---

## 🔧 编译状态

- ✅ **阶段一**：BUILD SUCCESS
- ✅ **阶段二**：BUILD SUCCESS
- ✅ **阶段三**：BUILD SUCCESS

---

## 📝 关键设计决策

1. **打包模板策略**
   - Legacy：兼容现有格式（`operators/groovy/${operatorCode}.groovy`）
   - Modern：推荐新格式（`operators/${operatorCode}/${fileName}`）
   - Custom：完全自定义路径

2. **路径变量支持**
   - 算子：`${operatorCode}`、`${fileName}`、`${fileExt}`
   - 公共库：`${libraryName}`、`${libraryVersion}`、`${fileName}`、`${fileExt}`

3. **打包预览**
   - 树形展示目录结构
   - 冲突检测（多个资源映射到同一路径）
   - 警告提示（空包、依赖缺失等）

---

## 📌 注意事项

1. **编译修复记录**
   - LibraryController.java 在阶段三过程中遇到编译错误
   - 问题：Service 返回 Spring Data 的 Page<LibraryResponse>，但 Controller 需要返回自定义的 PageResponse<LibraryResponse>
   - 解决方案：手动使用 PageResponse.builder() 构建响应对象
   - 验证状态：BUILD SUCCESS

2. **前端开发建议**
   - 由于代码量较大，建议分多次会话逐步完成前端开发
   - 优先级建议：
     1. 公共库管理页面
     2. 算子包编辑页面扩展（打包配置标签页）
     3. 打包预览组件
     4. 弹窗组件

---

## 📋 恢复指南

在新会话中恢复开发进度：

1. **阅读需求设计文档**
   - 文件：`docs/requirements/2026-02-21-算子包打包路径配置与公共库管理-需求设计.md`

2. **阅读规约文档**
   - `docs/standards/development-conventions.md`
   - `docs/standards/code-submission-workflow.md`

3. **阅读本文档**
   - 查看各阶段完成情况
   - 了解待完成工作内容
   - 遵循开发规范和提交流程

4. **继续开发**
   - 从当前进度阶段继续工作
   - 按照需求设计实现剩余功能

5. **代码提交**
   - 每次功能完成后必须编译验证
   - 启动服务进行功能测试
   - 确认无误后提交代码
   - 提交信息格式：`类型：简短描述`

---

**最后更新时间**：2026-02-21
**更新人**：Claude Sonnet 4.5
