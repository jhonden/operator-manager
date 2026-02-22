#!/bin/bash

# ========================================
# 配置常量
# ========================================
BACKEND_PORT=8080
FRONTEND_PORT=5173
JAVA_VERSION_MIN=21

# 默认参数
MODE=${1:-local}  # 默认本地模式
PROFILE=${2:-postgresql}  # 默认 PostgreSQL 模式

# ========================================
# 检查参数
# ========================================

case "$MODE" in
    local)
        echo "🛑 Stopping Operator Manager (Local Mode)..."
        ;;
    docker)
        echo "🛑 Stopping Operator Manager (Docker Mode)..."
        ;;
    *)
        echo "❌ 不支持的模式: $MODE"
        echo "   支持的模式: local, docker"
        echo "   使用方式: ./stop-all.sh <mode> [profile]"
        echo ""
        echo "   示例:"
        echo "     ./stop-all.sh local postgresql  # 停止本地 PostgreSQL 模式"
        echo "     ./stop-all.sh docker                  # 停止 Docker 模式"
        exit 1
        ;;
esac

case "$PROFILE" in
    h2)
        DATABASE_TYPE="H2"
        ;;
    postgresql)
        DATABASE_TYPE="PostgreSQL"
        ;;
    *)
        echo "❌ 不支持的 profile: $PROFILE"
        echo "   支持的 profile: h2, postgresql"
        exit 1
        ;;
esac

echo "   Mode: $MODE"
echo "   Database: $DATABASE_TYPE"
echo ""

# ========================================
# 1. 检查 Java 和 Maven
# ========================================

if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 first."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt $JAVA_VERSION_MIN ]; then
    echo "❌ Java $JAVA_VERSION_MIN or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Prerequisites check passed (Java $JAVA_VERSION, Maven installed)"

# ========================================
# 2. 停止后端服务
# ========================================

echo "🛑 Stopping backend service on port $BACKEND_PORT..."
echo "   Profile: $PROFILE"

# 查找占用端口的进程
BACKEND_PID=$(lsof -ti:$BACKEND_PORT 2>/dev/null)

if [ -n "$BACKEND_PID" ]; then
    echo "⚠️  Found backend process (PID: $BACKEND_PID), stopping it..."
    kill -9 $BACKEND_PID 2>/dev/null
    sleep 2

    # 再次检查是否停止成功
    NEW_PID=$(lsof -ti:$BACKEND_PORT 2>/dev/null)
    if [ -n "$NEW_PID" ]; then
        echo "❌ Failed to stop backend process (PID: $NEW_PID is still running)"
        echo "   Please manually kill it: kill -9 $NEW_PID"
        exit 1
    fi
    echo "✅ Backend process stopped successfully (PID: $BACKEND_PID)"
else
    echo "ℹ️  No backend process found on port $BACKEND_PORT"
fi

# ========================================
# 3. 停止前端服务
# ========================================

echo "🛑 Stopping frontend service on port $FRONTEND_PORT..."

# 查找占用端口的进程
FRONTEND_PID=$(lsof -ti:$FRONTEND_PORT 2>/dev/null)

if [ -n "$FRONTEND_PID" ]; then
    echo "⚠️  Found frontend process (PID: $FRONTEND_PID), stopping it..."
    kill -9 $FRONTEND_PID 2>/dev/null
    sleep 2

    # 再次检查是否停止成功
    NEW_PID=$(lsof -ti:$FRONTEND_PORT 2>/dev/null)
    if [ -n "$NEW_PID" ]; then
        echo "❌ Failed to stop frontend process (PID: $NEW_PID is still running)"
        echo "   Please manually kill it: kill -9 $NEW_PID"
        exit 1
    fi
    echo "✅ Frontend process stopped successfully (PID: $FRONTEND_PID)"
else
    echo "ℹ️  No frontend process found on port $FRONTEND_PORT"
fi

echo ""
echo "✅ All services stopped"
echo ""
