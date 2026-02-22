#!/bin/bash

# ========================================
# 配置常量
# ========================================
MODE=${1:-local}  # 默认本地模式
PROFILE=${2:-postgresql}  # 默认 PostgreSQL 模式

# ========================================
# 检查参数
# ========================================
if [ "$MODE" != "local" ] && [ "$MODE" != "docker" ]; then
    echo "❌ 不支持的模式: $MODE"
    echo "   支持的模式: local, docker"
    echo "   使用方式: ./start-all.sh <mode> [profile]"
    echo ""
    echo "   示例:"
    echo "     ./start-all.sh local postgresql  # 本地 PostgreSQL 模式"
    echo "     ./start-all.sh local h2           # 本地 H2 模式"
    echo "     ./start-all.sh docker                  # Docker 模式"
    exit 1
fi

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
        echo "   使用方式: ./start-all.sh <mode> [profile]"
        exit 1
        ;;
esac

echo "🎉 Starting Operator Manager ($MODE Mode - $DATABASE_TYPE)..."
echo ""

# ========================================
# 检查操作系统
# ========================================

if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "📱 Detected macOS"

    if [ "$MODE" == "local" ]; then
        # 本地模式：使用 start-backend-local.sh 和 start-frontend.sh
        echo "🔧 Starting backend (Local Mode - $DATABASE_TYPE)..."
        bash start-backend-local.sh $PROFILE &
        BACKEND_PID=$!

        # 等待后端启动
        echo "⏳ Waiting for backend to start..."
        sleep 15

        echo "🎨 Starting frontend..."
        bash start-frontend.sh &
        FRONTEND_PID=$!

        echo ""
        echo "✅ Services are starting in background..."
        echo "📊 Backend API: http://localhost:8080"
        echo "📱 Frontend: http://localhost:5173"
        echo ""
        echo "🔧 Backend Database: $DATABASE_TYPE"
        echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
        echo ""
        echo "⚠️  Stop services: ./stop-all.sh $MODE $PROFILE"

        # 监控进程
        wait $BACKEND_PID $FRONTEND_PID

    elif [ "$MODE" == "docker" ]; then
        # Docker 模式：使用 start-backend.sh 和 start-frontend.sh
        echo "🐳 Starting backend (Docker Mode)..."
        bash start-backend.sh &
        BACKEND_PID=$!

        # 等待后端启动
        echo "⏳ Waiting for backend to start..."
        sleep 15

        echo "🎨 Starting frontend..."
        bash start-frontend.sh &
        FRONTEND_PID=$!

        echo ""
        echo "✅ Services are starting in background..."
        echo "📊 Backend API: http://localhost:8080"
        echo "📱 Frontend: http://localhost:5173"
        echo ""
        echo "🐳 Docker Services:"
        echo "   - PostgreSQL: localhost:5432"
        echo "   - Redis: localhost:6379"
        echo "   - MinIO: http://localhost:9000 (Console: http://localhost:9001)"
        echo ""
        echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
        echo ""
        echo "⚠️  Stop services: ./stop-all.sh $MODE"

        # 监控进程
        wait $BACKEND_PID $FRONTEND_PID

    else
        echo "❌ 不支持的模式: $MODE"
        exit 1
    fi

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "📱 Detected Linux"

    if [ "$MODE" == "local" ]; then
        # Linux 本地模式
        echo "🔧 Starting backend (Local Mode - $DATABASE_TYPE)..."
        bash start-backend-local.sh $PROFILE &
        BACKEND_PID=$!

        echo "⏳ Waiting for backend to start..."
        sleep 15

        echo "🎨 Starting frontend..."
        bash start-frontend.sh &
        FRONTEND_PID=$!

        echo ""
        echo "✅ Services are starting in background..."
        echo "📊 Backend API: http://localhost:8080"
        echo "📱 Frontend: http://localhost:5173"
        echo ""
        echo "🔧 Backend Database: $DATABASE_TYPE"
        echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
        echo ""
        echo "⚠️  Stop services: ./stop-all.sh $MODE $PROFILE"

        # 监控进程
        wait $BACKEND_PID $FRONTEND_PID

    elif [ "$MODE" == "docker" ]; then
        # Linux Docker 模式
        echo "🐳 Starting backend (Docker Mode)..."
        bash start-backend.sh &
        BACKEND_PID=$!

        echo "⏳ Waiting for backend to start..."
        sleep 15

        echo "🎨 Starting frontend..."
        bash start-frontend.sh &
        FRONTEND_PID=$!

        echo ""
        echo "✅ Services are starting in background..."
        echo "📊 Backend API: http://localhost:8080"
        echo "📱 Frontend: http://localhost:5173"
        echo ""
        echo "🐳 Docker Services:"
        echo "   - PostgreSQL: localhost:5432"
        echo "   - Redis: localhost:6379"
        echo "   - MinIO: http://localhost:9000 (Console: http://localhost:9001)"
        echo ""
        echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
        echo ""
        echo "⚠️  Stop services: ./stop-all.sh $MODE $PROFILE"

        # 监控进程
        wait $BACKEND_PID $FRONTEND_PID

    else
        echo "❌ 不支持的模式: $MODE"
        exit 1
    fi

else
    echo "❌ 不支持的操作系统: $OSTYPE"
    echo "Please run start-backend.sh and start-frontend.sh separately in different terminals."
    exit 1
fi

echo ""
echo "✅ All services stopped"
