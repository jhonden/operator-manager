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
        # 本地模式：在新的 Terminal 窗口中启动服务
        echo "🔧 Starting backend (Local Mode - $DATABASE_TYPE)..."
        echo "🔧 Starting frontend..."

        # 打开新的 Terminal 窗口并执行命令
        osascript -e 'tell app "Terminal" to do script "cd '"$(pwd)"' && bash start-backend-local.sh '"$1"'" &'

        # 等待一段时间让服务启动
        sleep 20

        # 在另一个新窗口中启动前端
        osascript -e 'tell app "Terminal" to do script "cd '"$(pwd)"' && bash start-frontend.sh"'

        echo ""
        echo "✅ Services are starting in separate Terminal windows!"
        echo "📊 Backend API: http://localhost:8080"
        echo "📱 Frontend: http://localhost:5173"
        echo ""
        echo "🔧 Backend Database: $DATABASE_TYPE"
        echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
        echo ""
        echo "⚠️  Stop services: ./stop-all.sh $MODE $PROFILE"
        echo ""
        echo "📝 提示: 请关闭 Terminal 窗口来停止服务"

    elif [ "$MODE" == "docker" ]; then
        # Docker 模式：在新的 Terminal 窗口中启动服务
        echo "🐳 Starting backend (Docker Mode)..."
        echo "🔧 Starting frontend..."

        # 打开新的 Terminal 窗口并执行命令
        osascript -e 'tell app "Terminal" to do script "cd '"$(pwd)"' && bash start-backend.sh"'

        # 等待一段时间让服务启动
        sleep 20

        # 在另一个新窗口中启动前端
        osascript -e 'tell app "Terminal" to do script "cd '"$(pwd)"' && bash start-frontend.sh"'

        echo ""
        echo "✅ Services are starting in separate Terminal windows!"
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
        echo ""
        echo "📝 提示: 请关闭 Terminal 窗口来停止服务"

    else
        echo "❌ 不支持的操作系统: $OSTYPE"
        exit 1
    fi

echo ""
echo "✅ All services stopped"
echo ""
