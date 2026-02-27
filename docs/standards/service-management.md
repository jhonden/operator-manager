# 服务管理规范

本文档定义了服务的启动、停止和验证规范。

---

## 1. 服务启动

### 1.1 整体启动

使用统一启动脚本同时启动前后端服务。

**Linux/Mac 启动脚本**: `start-all.sh`

**使用方式**:
```bash
# 本地 PostgreSQL 模式（默认）
./start-all.sh

# 本地 H2 模式
./start-all.sh local h2

# Docker 模式
./start-all.sh docker
```

**Windows 启动脚本**: `start-all.bat`

**使用方式**:
```cmd
# 本地 PostgreSQL 模式（默认）
start-all.bat local postgresql

# 本地 H2 模式
start-all.bat local h2

# Docker 模式
start-all.bat docker
```

**功能说明**:
- 支持本地模式和 Docker 模式
- 本地模式支持 H2 和 PostgreSQL 两种数据库
- 默认使用 PostgreSQL 模式
- 在新窗口中启动服务，便于查看日志
- 显示数据库类型和模式信息

---

### 1.2 前端启动

单独启动前端服务。

**Linux/Mac 启动脚本**: `start-frontend.sh`

**Windows 启动脚本**: `start-frontend.bat`

**功能说明**:
- 启动前端开发服务器（Vite）
- 端口：5173
- 自动检查 Node.js 安装
- 自动安装依赖（如需要）

---

### 1.3 后端启动

单独启动后端服务，根据环境选择不同脚本。

#### 本地模式

**Linux/Mac 启动脚本**: `start-backend-local.sh`

**Windows 启动脚本**: `start-backend-local.bat`

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
- 自动检查并停止已有进程
- 清理并编译项目
- 在前台启动（方便查看日志）

#### Docker 模式

**Linux/Mac 启动脚本**: `start-backend.sh`

**Windows 启动脚本**: `start-backend.bat`

**功能说明**:
- 自动启动 Docker 服务（PostgreSQL、Redis、MinIO）
- 等待 Docker 服务就绪
- 编译项目
- 在前台启动后端服务

---

## 2. 服务停止

### 2.1 整体停止

使用统一停止脚本同时停止前后端服务。

**Linux/Mac 停止脚本**: `stop-all.sh`

**Windows 停止脚本**: `stop-all.bat`

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

### 2.2 前端停止

单独停止前端服务。

**Linux/Mac 停止脚本**: `stop-frontend.sh`

**Windows 停止脚本**: `stop-frontend.bat`

**功能说明**:
- 停止占用 5173 端口的进程
- 验证停止成功
- 显示停止状态

---

### 2.3 后端停止

单独停止后端服务。

#### 本地模式

**Linux/Mac 停止脚本**: `stop-backend-local.sh`

**Windows 停止脚本**: `stop-backend-local.bat`

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

#### Docker 模式

**Linux/Mac 停止脚本**: `stop-backend.sh`

**Windows 停止脚本**: `stop-backend.bat`

**功能说明**:
- 停止后端进程
- Docker 容器保持运行（如需停止，使用 `docker-compose down`）

---

## 3. 服务启动验证

### 3.1 启动服务

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

### 3.2 停止服务时机

**可以停止的情况**:
- 用户明确要求停止
- 用户确认功能测试完成
- 需要重新编译启动（用户明确同意）

---

## 4. 访问地址

| 服务 | URL |
|------|-----|
| 前端 | http://localhost:5173 |
| 后端 API | http://localhost:8080/api |
| Swagger 文档 | http://localhost:8080/swagger-ui.html |
| PostgreSQL (Docker) | localhost:5432 |
| Redis (Docker) | localhost:6379 |
| MinIO (Docker) | http://localhost:9000 |
| MinIO Console (Docker) | http://localhost:9001 |

---

## 5. 常见问题

### Q1: 服务启动失败怎么办？

**A:** 检查以下几点：
1. 端口是否被占用（8080、5173）
2. Java/Maven/Node.js 是否正确安装
3. Docker 是否运行（Docker 模式）
4. 查看错误日志，根据提示修复

### Q2: 如何查看日志？

**A:**
- Linux/Mac：查看启动窗口的输出
- Windows：查看启动窗口的输出
- Docker 模式：使用 `docker logs <container-name>` 查看容器日志

---

**相关文档**:
- [代码提交流程](./code-submission-workflow.md) - 代码验证和提交流程
- [前后端协同开发规范](./frontend-backend-collaboration.md) - 前后端协同开发流程

---

**文档维护者**: Claude AI Assistant
**最后更新**: 2026-02-27
