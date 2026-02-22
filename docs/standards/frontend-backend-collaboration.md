# 前后端协同开发规范

本文档定义了涉及前后端协同开发时的开发流程、测试规范和最佳实践。

## 核心原则

**先后端再前端，测试驱动开发**

所有涉及前后端协同的功能，必须严格按照以下阶段顺序执行：
1. 后端开发阶段
2. 后端测试阶段
3. 前端开发阶段
4. 前后端联调阶段
5. 代码提交阶段

---

## 第一阶段：后端开发

### 1.1 后端优先开发

**原则：**
- 涉及前后端协同的功能，**先完成后端再开发前端**
- 后端开发遵循现有分层架构规范（领域层、服务层、API层）
- 完成后端代码后进行自测

**注意事项：**
- 不要在前端未完成前就开始开发前端功能
- 确保后端 API 设计合理，考虑前端的易用性
- 使用清晰的 API 响应格式

### 1.2 后端 API 功能测试

后端开发完成后，必须对每个 API 端点进行测试：

**测试内容：**
- 接口可用性（HTTP 状态码）
- 请求参数格式和验证
- 响应数据结构
- 错误处理逻辑
- 边界条件

**测试工具：**
- 使用 curl 或 Postman 测试
- 编写 Shell 脚本进行自动化测试

**示例：**
```bash
# 测试获取库依赖列表
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/operators/1/library-dependencies

# 验证 HTTP 状态码
curl -s -w '\nHTTP_CODE:%{http_code}\n' -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/operators/1/library-dependencies | tail -1
```

### 1.3 关键功能端到端流程测试

除了单个 API 端点的测试，还必须测试完整的业务流程：

**测试内容：**
- 完整的业务链路
- 涉及多个 API 调用的串联
- 数据流转的正确性
- 业务逻辑的完整性

**示例：算子公共库依赖功能测试**
```bash
# 1. 获取库依赖列表（初始为空）
# 2. 添加库依赖
# 3. 查询验证（确认添加成功）
# 4. 获取库依赖列表（确认有数据）
# 5. 删除库依赖
# 6. 查询验证（确认删除成功）
# 7. 获取库依赖列表（确认为空）
```

### 1.4 测试用例固化到测试脚本

**要求：**
- 将测试用例编写成可执行的 Shell 脚本
- 放入 `tests/` 目录，便于复用和回归测试
- 测试脚本必须可重复执行
- 避免测试之间的依赖关系

**测试脚本位置和命名：**
- 位置：`tests/` 目录
- 命名规范：`<功能>-api-test.sh`
- 例如：`library-dependency-api-test.sh`

**测试脚本结构：**
```bash
#!/bin/bash
# <测试描述>
# 作者：xxx
# 日期：YYYY-MM-DD

# 引入公共工具
source tests/utils/assertions.sh
source tests/utils/logger.sh

# 测试准备
log_info "开始测试：算子公共库依赖 API"
TOKEN=$(login admin admin123)

# 测试用例 1：获取库依赖列表
log_info "测试：获取算子库依赖列表"
RESPONSE=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/operators/1/library-dependencies)
assert_success "$RESPONSE"
assert_field_equals "$RESPONSE" "success" "true"

# 测试用例 2：添加库依赖
log_info "测试：添加算子库依赖"
ADD_RESP=$(curl -s -X POST -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"libraryId":1}' \
  http://localhost:8080/api/v1/operators/1/library-dependencies)
assert_success "$ADD_RESP"

# 测试用例 3：验证库依赖已添加
log_info "测试：验证库依赖已添加"
VERIFY_RESP=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/operators/1/library-dependencies)
assert_contains "$VERIFY_RESP" "const"

# 测试用例 4：删除库依赖
log_info "测试：删除算子库依赖"
DEL_RESP=$(curl -s -X DELETE -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/operators/1/library-dependencies/1)
assert_success "$DEL_RESP"

# 测试用例 5：验证库依赖已删除
log_info "测试：验证库依赖已删除"
FINAL_RESP=$(curl -s -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/operators/1/library-dependencies)
assert_empty_data "$FINAL_RESP"

# 测试结果
log_success "所有测试通过！"
```

**测试工具：**
- `tests/utils/assertions.sh` - 提供断言函数
- `tests/utils/logger.sh` - 提供日志函数

**常用断言函数：**
```bash
assert_success "$RESPONSE"        # 验证 success 为 true
assert_field_equals "$RESPONSE" "field" "value"  # 验证字段值
assert_contains "$RESPONSE" "text"  # 验证包含文本
assert_empty_data "$RESPONSE"      # 验证 data 为空
```

**测试用例管理：**
- 每个功能开发完成后，测试用例必须固化到脚本
- 测试脚本必须可重复执行
- 避免测试之间的依赖关系
- 测试失败时必须清晰地指出错误原因

---

## 第二阶段：前端开发

### 2.1 后端 API 契约验证

**原则：**
- 在开发前端前，使用测试脚本验证后端 API
- 确认 API 接口、请求/响应格式正确
- 避免前端开发基于错误的 API

**验证步骤：**
1. 运行测试脚本验证后端功能
2. 使用 curl 手动测试 API 端点
3. 确认请求参数格式
4. 确认响应数据结构
5. 确认错误处理逻辑

### 2.2 模块化组件设计

**原则：**
- 将复杂功能拆分为独立组件
- 单一职责原则
- 代码可维护性强

**示例：**
```typescript
// LibrarySelectorModal.tsx - 独立的公共库选择弹窗
const LibrarySelectorModal: React.FC<LibrarySelectorModalProps> = ({
  visible,
  onCancel,
  onLibrarySelect,
}) => {
  // 只负责：显示公共库列表、选择、搜索
  // 不负责：添加/删除库依赖的业务逻辑
};

// create.tsx - 算子编辑页面
// 只负责：页面布局、状态管理、业务逻辑调用
```

### 2.3 关键位置日志输出

**原则：**
- API 调用添加日志
- 状态变更添加日志
- 关键操作添加日志
- 统一前缀格式，便于过滤搜索

**日志前缀格式：**
- `[Operator API]` - 算子 API 调用
- `[Operator Page]` - 算子页面操作
- `[Library Selector Modal]` - 公共库选择弹窗操作
- `[Library API]` - 公共库 API 调用

**示例：**
```typescript
// API 调用日志
console.log('[Operator API] 添加公共库依赖, operatorId:', operatorId, 'libraryId:', data.libraryId);

// 页面操作日志
console.log('[Operator Page] Adding library dependency, operatorId:', id, 'libraryId:', selectedLibraryId);

// 弹窗操作日志
console.log('[Library Selector Modal] Selected library:', selectedLibrary);

// 错误日志
console.error('[Operator Page] Error adding library dependency:', error);
```

**日志内容要求：**
- 包含关键参数（ID、状态等）
- 包含响应数据摘要
- 错误日志包含完整的错误信息
- 使用中文描述（便于理解）
- 技术术语保留英文（如 operatorId、libraryId）

### 2.4 增量编译验证

**原则：**
- 开发过程中定期运行 `npm run build`
- 每次修复后立即验证编译状态
- 问题早发现早解决

**验证频率：**
- 每完成一个功能点，运行一次编译
- 每修复一个 TypeScript 错误，运行一次编译
- 发现编译错误立即修复，不积累问题

**示例：**
```bash
# 开发过程中
npm run build

# 如果有错误，修复后再运行
npm run build
```

### 2.5 类型系统优先

**原则：**
- 先完善类型定义
- 确保类型导入正确后再写业务逻辑
- TypeScript 编译错误必须全部修复

**类型定义顺序：**
1. 在 `src/types/library.ts` 定义完整类型
2. 在 `src/types/index.ts` 导出类型
3. 在 API 文件导入类型
4. 在页面/组件中使用类型

**示例：**
```typescript
// 1. 先在 library.ts 定义完整类型
export interface LibraryDependencyResponse {
    id: number;
    libraryId: number;
    libraryName: string;
    // ...
}

// 2. 再在 index.ts 导出类型
export type { LibraryDependencyResponse, AddLibraryDependencyRequest } from './library';

// 3. 在 API 文件导入类型
import type { LibraryDependencyResponse, AddLibraryDependencyRequest } from '@/types/library';

// 4. 在页面中使用类型
const [libraries, setLibraries] = useState<LibraryDependencyResponse[]>([]);
```

**类型错误处理：**
- TypeScript 编译错误必须全部修复
- 避免使用 `any` 类型
- 使用类型断言时要谨慎

---

## 第三阶段：前后端联调测试

### 3.1 后端 API 端到端测试复用

**原则：**
- 使用已固化的测试脚本验证后端
- 确保后端功能在前后端联调前仍然正常

**测试步骤：**
1. 运行测试脚本验证后端
2. 检查测试结果是否全部通过
3. 如有失败，先修复后端问题
4. 确认后端稳定后，再开始前端测试

### 3.2 前端功能测试

**原则：**
- 用户在浏览器中测试前端 UI 交互
- 验证功能完整性
- 检查浏览器控制台日志输出

**测试内容：**
- UI 显示是否正确
- 用户交互是否流畅
- 数据加载和展示是否正确
- 错误处理是否友好
- 日志输出是否符合规范

**测试步骤：**
1. 打开浏览器访问应用
2. 打开浏览器控制台（F12）
3. 执行完整的业务流程
4. 检查控制台日志
5. 验证功能是否正常

---

## 第四阶段：代码提交

### 4.1 代码审查和提交

**原则：**
- 提交前运行 `git status` 和 `git diff` 确认改动
- 编写中文提交信息，清晰描述改动内容
- 包含 `Co-Authored-By` 标识
- 推送到远程仓库

**提交信息格式：**
```
<类型>：<简要描述>

详细描述（可选）

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

**类型示例：**
- `新增：添加 xxx 功能`
- `修复：修复 xxx 问题`
- `优化：优化 xxx 性能`
- `重构：重构 xxx 代码`

**示例：**
```
新增：算子公共库依赖管理前端功能

新增功能：
- 添加 LibrarySelectorModal 组件用于选择公共库
- 在算子创建/编辑页面新增 Step 5 公共库依赖管理
- 实现库依赖的添加、删除、列表展示功能
- 支持按类型筛选和关键词搜索公共库

技术改进：
- 修复 TypeScript 类型导入错误
- 为 Parameter 接口添加 ioType 属性
- 添加关键位置日志输出

Co-Authored-By: Claude Sonnet 4.5 <noreply@anthropic.com>
```

### 4.2 提交流程

**步骤：**
1. 运行 `git status` 查看改动
2. 运行 `git diff --stat` 查看改动统计
3. 添加改动到暂存区：`git add <files>`
4. 创建提交：`git commit -m "提交信息"`
5. 运行 `git status` 验证提交
6. 推送到远程：`git push origin main`
7. 运行 `git status` 验证推送

---

## 开发流程图

```
需求确认
    ↓
┌─────────────────────────────┐
│  后端开发阶段              │
│  1. 后端优先开发          │
│  2. API 功能测试          │
│  3. 端到端流程测试        │
│  4. 测试用例固化          │
└─────────────────────────────┘
    ↓
┌─────────────────────────────┐
│  前端开发阶段              │
│  5. 后端验证先行          │
│  6. 模块化组件设计        │
│  7. 关键位置日志          │
│  8. 增量编译验证          │
│  9. 类型系统优先          │
└─────────────────────────────┘
    ↓
┌─────────────────────────────┐
│  联调和提交阶段            │
│  10. 后端端到端测试复用  │
│  11. 前端功能测试        │
│  12. 代码提交规范        │
└─────────────────────────────┘
```

---

## 关键实践总结

### 后端开发阶段
1. ✅ 后端优先开发
2. ✅ 后端 API 功能测试
3. ✅ 后端端到端流程测试
4. ✅ 测试用例固化到脚本

### 前端开发阶段
5. ✅ 后端验证先行再开发前端
6. ✅ 模块化组件设计
7. ✅ 关键位置日志输出
8. ✅ 增量编译验证
9. ✅ 类型系统优先

### 联调和提交阶段
10. ✅ 前后端联调测试
11. ✅ 用户测试验证
12. ✅ 代码提交规范

---

## 注意事项

### 禁止行为
- ❌ 在后端未完成时就开始开发前端
- ❌ 在后端 API 未测试时就开发前端
- ❌ 开发过程中不运行编译检查
- ❌ 编译错误未修复就提交代码
- ❌ 不添加日志就提交代码
- ❌ 不进行用户测试就提交代码

### 推荐行为
- ✅ 严格按照四个阶段顺序执行
- ✅ 后端测试用例必须固化到脚本
- ✅ 前端开发前必须验证后端 API
- ✅ 关键位置必须添加日志
- ✅ 用户确认功能正常后再提交代码

---

## 版本历史

- **v1.0** (2026-02-22) - 初始版本，基于算子公共库依赖功能的开发经验总结
