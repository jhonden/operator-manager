# 开发规范

本文档定义了项目开发过程中的编码规范和约束。

---

## 1. 文档和注释语言规范

**重要：因为项目在中国开发，以下情况必须优先使用中文：**

### 1.1 Git 提交注释（Commit Messages）

- 必须使用中文编写提交信息
- 格式示例：`git commit -m "修复：算子列表查询 500 错误"`
- 详细规则见：[代码提交流程](./code-submission-workflow.md)

### 1.2 代码注释

- 业务逻辑注释必须使用中文
- 复杂算法或配置说明必须使用中文
- 示例：
  ```java
  // 检查 operatorCode 是否已存在，确保唯一性约束
  if (operatorRepository.existsByOperatorCode(dto.getOperatorCode())) {
      throw new BusinessException("算子编码已存在");
  }
  ```

### 1.3 文档输出

- 代码文档（Javadoc、README）优先使用中文
- 架构设计文档、API 文档使用中文
- 用户手册、开发指南使用中文

### 1.4 日志输出

- 应用日志信息使用英文
- 错误提示信息使用英文
- 示例：`log.error("user login fail：{}", username);`

### 1.5 用户界面文本

- 前端页面文本使用中文
- 错误提示、警告信息使用中文
- API 返回的错误消息使用中文

### 1.6 例外情况

- 技术术语保持英文（如 JSON、API、JWT、Exception 等）
- 变量名、类名、方法名等标识符使用英文
- 配置文件中的 key 使用英文
- 国际化（i18n）相关内容使用英文
- **代码的运行日志输出使用英文**（便于日志分析和问题排查）

---

## 7. 服务管理规范

### 7.1 服务启动

#### 7.1.1 整体启动

使用统一启动脚本同时启动前后端服务。

**启动脚本**: `start-all.sh`

**使用方式**:
```bash
# 本地 PostgreSQL 模式（默认）
./start-all.sh

# 本地 H2 模式
./start-all.sh local h2

# Docker 模式
./start-all.sh docker
```

**功能说明**:
- 支持本地模式和 Docker 模式
- 本地模式支持 H2 和 PostgreSQL 两种数据库
- 默认使用 PostgreSQL 模式
- 在后台启动并监控进程
- 显示数据库类型和模式信息

---

#### 7.1.2 前端启动

单独启动前端服务，使用前端启动脚本。

**启动脚本**: `start-frontend.sh`

**使用方式**:
```bash
./start-frontend.sh
```

**功能说明**:
- 启动前端开发服务器（Vite）
- 端口：5173
- 在后台启动

---

#### 7.1.3 后端启动

单独启动后端服务，根据环境选择不同脚本。

**本地模式启动脚本**: `start-backend-local.sh`

**使用方式**:
```bash
# 使用 PostgreSQL（默认）
./start-backend-local.sh

# 使用 H2
./start-backend-local.sh h2
```

**功能说明**:
- 本地模式支持 H2 和 PostgreSQL 两种数据库
- 默认使用 PostgreSQL 模式
- 检查并停止已有进程
- 清理并编译项目
- 在前台启动（方便查看日志）

**Docker 模式启动脚本**: `start-backend.sh`

**使用方式**:
```bash
./start-backend.sh
```

**功能说明**:
- Docker 模式启动后端
- 启动 Docker 容器中的后端服务

---

### 7.2 服务停止

#### 7.2.1 整体停止

使用统一停止脚本同时停止前后端服务。

**停止脚本**: `stop-all.sh`

**使用方式**:
```bash
# 默认停止本地 PostgreSQL
./stop-all.sh

# 停止本地 H2 模式
./stop-all.sh local h2

# 停止 Docker 模式
./stop-all.sh docker
```

**功能说明**:
- 支持本地模式和 Docker 模式
- 同时停止后端（端口 8080）和前端（端口 5173）服务
- 默认停止本地 PostgreSQL 模式
- 检查进程并验证停止成功
- 显示停止状态和进程信息

---

#### 7.2.2 前端停止

单独停止前端服务。

**停止脚本**: `stop-frontend.sh`

**使用方式**:
```bash
./stop-frontend.sh
```

**功能说明**:
- 检查 Node.js 是否安装
- 停止前端服务（端口 5173）
- 验证停止成功
- 显示停止状态

---

#### 7.2.3 后端停止

单独停止后端服务，根据环境选择不同脚本。

**本地模式停止脚本**: `stop-backend-local.sh`

**使用方式**:
```bash
# 使用 PostgreSQL（默认）
./stop-backend-local.sh

# 使用 H2
./stop-backend-local.sh h2
```

**功能说明**:
- 本地模式支持 H2 和 PostgreSQL 两种数据库
- 停止占用 8080 端口的进程
- 检查进程并验证停止成功
- 显示停止状态和进程信息

---

### 7.3 服务启动验证

#### 7.3.1 启动服务

**正确行为**:
- ✅ 启动服务后**不要停止**
- ✅ 等待用户自行验证
- ✅ 用户测试功能、查看日志
- ✅ 等待用户明确反馈

**禁止行为**:
- ❌ 启动后立即停止
- ❌ 未经用户同意就重启服务
- ❌ 频繁停止启动服务

---

#### 7.3.2 停止服务时机

**可以停止的情况**:
- 用户明确要求停止
- 用户确认功能测试完成
- 需要重新编译启动（用户明确同意）

**详细流程**: [代码提交流程](./code-submission-workflow.md#3-服务启动验证)
#### 7.3.3 停止服务时机

**可以停止的情况：**
- 用户明确要求停止
- 用户确认功能测试完成
- 需要重新编译启动（用户明确同意）

**详细流程见**: [代码提交流程](./code-submission-workflow.md#3-服务启动验证)

---

## 2. 后端开发规范

### 2.1 编码规范

- 遵循 Java 21 语言规范
- 使用驼峰命名法（camelCase）命名变量和方法
- 使用大驼峰命名法（PascalCase）命名类和接口
- 常量使用全大写下划线分隔（UPPER_SNAKE_CASE）

### 2.2 异常处理

- 使用业务异常类（BusinessException）
- 异常消息使用中文
- 不要捕获异常后直接忽略，至少记录日志

### 2.3 日志规范

- 使用 SLF4J 日志框架
- 日志级别使用：
  - `ERROR`: 错误信息
  - `WARN`: 警告信息
  - `INFO`: 关键业务流程
  - `DEBUG`: 调试信息（仅开发环境）
- 日志格式：使用中文描述，占位符输出关键参数

### 2.4 事务管理

- 修改数据的方法必须添加 `@Transactional` 注解
- 查询方法使用 `@Transactional(readOnly = true)`

---

## 3. 前端开发规范

### 3.1 TypeScript 规范

- 使用 TypeScript 进行类型检查
- 避免使用 `any` 类型
- 使用接口定义数据结构
- 导出类型定义到 `src/types/index.ts`

### 3.2 组件规范

- 使用函数组件（Function Components）
- 避免类组件（Class Components）
- 使用 Hooks 管理状态

### 3.3 样式规范

- 使用 Less 进行样式编写
- 避免行内样式
- 使用语义化的 class 命名

---

## 4. 测试规范

### 4.1 单元测试

- 核心业务逻辑必须有单元测试
- 测试覆盖率不低于 70%
- 测试类命名：`{类名}Test`

### 4.2 集成测试

- API 接口必须有集成测试
- 使用 Shell 脚本进行接口测试
- 测试脚本位于 `tests/` 目录

---

## 5. 安全规范

### 5.1 输入验证

- 所有用户输入必须进行验证
- 使用 Spring Validation 注解（`@NotNull`, `@Size`, `@Pattern` 等）
- 自定义验证注解放在 `operator-common` 模块

### 5.2 SQL 注入防护

- 使用 JPA Repository，避免原生 SQL
- 必须使用原生 SQL 时，使用参数化查询

### 5.3 XSS 防护

- 前端对用户输入进行转义
- 避免直接渲染 HTML 内容

---

## 6. Git 工作流规范

- 每个功能开发一个分支
- 功能完成后合并到 main 分支
- 合并前确保功能验证通过
- 详细流程见：[代码提交流程](./code-submission-workflow.md)
