#!/bin/bash

# ========================================
# 配置常量
# ========================================
BACKEND_PORT=8080
JAVA_VERSION_MIN=21

echo "🚀 Starting Operator Manager Backend (Local Mode - PostgreSQL only)..."
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
# 2. 停止后端服务（如果有运行）
# ========================================
echo "🛑 Checking for existing backend process on port $BACKEND_PORT..."

OLD_PID=$(lsof -ti:$BACKEND_PORT 2>/dev/null)

if [ -n "$OLD_PID" ]; then
    echo "⚠️  Found existing backend process (PID: $OLD_PID), stopping it..."
    kill -9 $OLD_PID 2>/dev/null
    sleep 2
    # 再次检查是否停止成功
    NEW_PID=$(lsof -ti:$BACKEND_PORT 2>/dev/null)
    if [ -n "$NEW_PID" ]; then
        echo "❌ Failed to stop old process (PID: $NEW_PID is still running)"
        echo "   Please manually kill it: kill -9 $NEW_PID"
        exit 1
    fi
    echo "✅ Old backend process stopped successfully"
else
    echo "ℹ️  No existing backend process found"
fi

# ========================================
# 3. 编译项目
# ========================================
echo "🔨 Building project in operator-api directory..."
cd operator-api

# 先 clean，确保使用最新的编译代码
mvn clean -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Maven clean failed. Please check for errors above."
    exit 1
fi

# 编译
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check for errors above."
    exit 1
fi

echo "✅ Build successful"

# ========================================
# 4. 启动应用
# ========================================
echo "🎯 Starting backend application..."
echo "   Profile: dev-postgresql"
echo "   Port: $BACKEND_PORT"
echo "   Mode: Local (No Docker)"
echo ""

# 在前台启动，方便调试时查看日志
mvn spring-boot:run -Dspring-boot.run.profiles=dev-postgresql
