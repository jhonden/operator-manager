#!/bin/bash

# ========================================
# 配置常量
# ========================================
FRONTEND_PORT=5173
JAVA_VERSION_MIN=21

echo "🛑 Stopping Operator Manager Frontend..."
echo ""

# ========================================
# 1. 检查 Java 和 Node
# ========================================

if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    exit 1
fi

echo "✅ Prerequisites check passed (Node.js installed)"

# ========================================
# 2. 停止前端服务
# ========================================

echo "🛑 Stopping frontend service on port $FRONTEND_PORT..."

# 查找占用端口的进程
FRONTEND_PID=$(lsof -ti:$FRONTEND_PORT 2>/dev/null)

if [ -n "$FRONTEND_PID" ]; then
    echo "⚠️  Found frontend process (PID: $FRONTEND_PID), stopping it..."
    kill -9 $FRONTEND_PID 2>/dev/null

    # 等待进程终止
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
echo "✅ Frontend service stopped"
echo ""
