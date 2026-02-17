# Operator Manager - Quick Start Guide

快速启动指南 - 在本地运行 Operator Manager 项目

## 📋 前置要求

在启动项目之前，请确保已安装以下软件：

### 必需软件
- **Java 21** 或更高版本
  - 下载地址: https://adoptium.net/
  - 验证: `java -version`

- **Maven 3.9+**
  - 下载地址: https://maven.apache.org/download.cgi
  - 验证: `mvn -version`

- **Node.js 18+**
  - 下载地址: https://nodejs.org/
  - 验证: `node -v`

- **npm**
  - 随 Node.js 一起安装
  - 验证: `npm -v`

- **Docker & Docker Compose**
  - 下载地址: https://www.docker.com/products/docker-desktop
  - 验证: `docker --version` 和 `docker-compose --version`

### 端口占用检查
确保以下端口未被占用：
- 5432 (PostgreSQL)
- 6379 (Redis)
- 9000, 9001 (MinIO)
- 8080 (Backend API)
- 5173 (Frontend Dev Server)

## 🚀 快速启动

### 方法一：一键启动（推荐 macOS）

```bash
# 1. 克隆项目后，进入项目目录
cd /Users/gaowen/Code/operator-manager

# 2. 给脚本添加执行权限
chmod +x start-all.sh
chmod +x start-backend.sh
chmod +x start-frontend.sh

# 3. 一键启动后端和前端
./start-all.sh
```

这会自动：
1. 启动 Docker 服务（PostgreSQL, Redis, MinIO）
2. 构建并启动后端服务
3. 安装前端依赖并启动前端开发服务器

### 方法二：手动启动（推荐 Linux/Windows）

**终端 1 - 启动后端**
```bash
cd /Users/gaowen/Code/operator-manager
./start-backend.sh
```

**终端 2 - 启动前端**（等待后端完全启动后）
```bash
cd /Users/gaowen/Code/operator-manager
./start-frontend.sh
```

## 📍 访问地址

服务启动成功后，可以通过以下地址访问：

| 服务 | 地址 | 说明 |
|------|------|------|
| **前端应用** | http://localhost:5173 | React 前端界面 |
| **后端 API** | http://localhost:8080 | RESTful API |
| **API 文档** | http://localhost:8080/swagger-ui.html | Swagger UI |
| **PostgreSQL** | localhost:5432 | 数据库 |
| **Redis** | localhost:6379 | 缓存 |
| **MinIO** | http://localhost:9000 | 对象存储 |
| **MinIO Console** | http://localhost:9001 | MinIO 管理界面 |

## 🔐 默认账号

首次启动后，你需要注册一个新账号：

1. 访问 http://localhost:5173
2. 点击 "Register now"
3. 填写注册信息：
   - Username: （用户名）
   - Email: （邮箱）
   - Full Name: （全名）
   - Password: （密码）

## 📂 项目结构

```
operator-manager/
├── operator-api/              # 后端 API 模块
├── operator-core/             # 核心领域模块
├── operator-service/          # 业务服务层
├── operator-infrastructure/   # 基础设施层
├── operator-common/           # 公共模块
├── operator-manager-web/      # 前端 React 应用
├── docker-compose.yml         # Docker 服务配置
├── start-backend.sh           # 后端启动脚本
├── start-frontend.sh          # 前端启动脚本
├── start-all.sh               # 一键启动脚本
└── QUICKSTART.md             # 本文档
```

## 🛠️ 常见问题

### 1. Docker 服务启动失败
```bash
# 检查 Docker 是否运行
docker ps

# 重启 Docker Desktop
# 或手动启动服务
docker-compose up -d
```

### 2. Maven 构建失败
```bash
# 清理并重新构建
cd operator-api
mvn clean install -U

# 如果依赖下载失败，配置阿里云镜像
# 编辑 ~/.m2/settings.xml
```

### 3. 前端依赖安装失败
```bash
cd operator-manager-web
rm -rf node_modules package-lock.json
npm install
```

### 4. 端口已被占用
```bash
# 查看端口占用
lsof -i :8080
lsof -i :5173

# 杀死占用端口的进程
kill -9 <PID>
```

### 5. 后端连接数据库失败
```bash
# 检查 Docker 服务是否正常
docker-compose ps

# 查看数据库日志
docker logs operator-manager-postgres
```

## 🎯 开发模式

### 前端热更新
前端使用 Vite，支持热更新。修改代码后，浏览器会自动刷新。

### 后端热更新
后端使用 Spring Boot DevTools，修改 Java 代码后会自动重启。

### 调试
- **后端**: 在 IDE 中以 Debug 模式运行 `operator-api` 模块的 `OperatorManagerApplication`
- **前端**: 在浏览器中打开开发者工具 (F12)

## 📝 技术栈

### 后端
- Spring Boot 3.2.x
- JDK 21
- PostgreSQL 15
- Redis 7
- MinIO
- Spring Security + JWT
- SpringDoc OpenAPI

### 前端
- React 18
- TypeScript 5
- Vite 5
- Ant Design 5
- Zustand (状态管理)
- React Router 6
- Axios

## 🔄 停止服务

```bash
# 停止 Docker 服务
docker-compose down

# 停止前端 (Ctrl+C 在终端中)
# 停止后端 (Ctrl+C 在终端中)
```

## 🚀 下一步

1. ✅ 服务已启动
2. 🌐 访问前端: http://localhost:5173
3. 👤 注册账号
4. 🎉 开始使用 Operator Manager！

## 📚 更多文档

- [详细设计文档](./docs/DESIGN.md)
- [API 文档](./docs/API.md) - 运行后访问 http://localhost:8080/swagger-ui.html
- [部署指南](./docs/DEPLOYMENT.md)

## 💡 提示

- 首次启动 Maven 下载依赖可能需要较长时间，请耐心等待
- 前端依赖安装也可能需要几分钟
- 如果遇到问题，请查看 "常见问题" 部分
- 建议使用 Chrome 或 Edge 浏览器访问前端

---

**祝使用愉快！🎉**
