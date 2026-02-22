#!/bin/bash

# ========================================
# 配置常量
# ========================================
BACKEND_PORT=8080
JAVA_VERSION_MIN=21

# 可选参数
PROFILE=${1:-postgresql}  # 默认使用 postgresql

case "$PROFILE" in
    h2)
        PROFILE_NAME="dev"
        DATABASE_TYPE="H2"
        ;;
    postgresql)
        PROFILE_NAME="dev-postgresql"
        DATABASE_TYPE="PostgreSQL"
        ;;
    *)
        echo "❌ 不支持的 profile: $PROFILE"
        echo "   支持的 profile: h2, postgresql"
        echo "   使用方式: ./stop-backend-local.sh <profile>"
        echo "   示例: ./stop-backend-local.sh postgresql"
        exit 1
        ;;
esac

echo "🛑 Stopping Operator Manager Backend (Local Mode - $DATABASE_TYPE)..."
echo "   Profile: $PROFILE"
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
echo "🛑 Checking for existing backend process on port $BACKEND_PORT..."
echo "   Profile: $PROFILE"

# 查找占用端口的进程
OLD_PID=$(lsof -ti:$BACKEND_PORT 2>/dev/null)

if [ -n "$OLD_PID" ]; then
    echo "⚠️  Found existing backend process (PID: $OLD_PID), stopping it..."
    kill -9 $OLD_PID 2>/dev/null

    # 等待进程终止
    sleep 2

    # 再次检查是否停止成功
    NEW_PID=$(lsof -ti:$BACKEND_PORT 2>/dev/null)

    if [ -n "$NEW_PID" ]; then
        echo "❌ Failed to stop old process (PID: $NEW_PID is still running)"
        echo "   Please manually kill it: kill -9 $NEW_PID"
        exit 1
    fi

    echo "✅ Backend process stopped successfully (PID: $OLD_PID)"
    echo "   Profile: $PROFILE"
    echo "   Database: $DATABASE_TYPE"
else
    echo "ℹ️  No existing backend process found on port $BACKEND_PORT"
    echo "   Profile: $PROFILE"
    echo "   Database: $DATABASE_TYPE"
fi

echo ""
