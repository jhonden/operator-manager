#!/bin/bash

echo "🎉 Starting Operator Manager (Backend + Frontend)..."
echo ""

# Check if running on macOS
if [[ "$OSTYPE" == "darwin"* ]]; then
    echo "📱 Detected macOS"

    # Start backend in a new Terminal window
    osascript -e 'tell app "Terminal" to do script "cd '"$(pwd)"' && bash start-backend.sh"'

    # Wait a bit for backend to start
    echo "⏳ Waiting for backend to start..."
    sleep 15

    # Start frontend in a new Terminal window
    osascript -e 'tell app "Terminal" to do script "cd '"$(pwd)"' && bash start-frontend.sh"'

    echo ""
    echo "✅ Services are starting in separate Terminal windows!"
    echo ""
    echo "📊 Backend API: http://localhost:8080"
    echo "📱 Frontend: http://localhost:5173"
    echo ""
    echo "🔧 Docker Services:"
    echo "   - PostgreSQL: localhost:5432"
    echo "   - Redis: localhost:6379"
    echo "   - MinIO: http://localhost:9000 (Console: http://localhost:9001)"
    echo ""
    echo "📚 API Documentation: http://localhost:8080/swagger-ui.html"
    echo ""
    echo "⚠️  Press Ctrl+C in each Terminal window to stop the services"

elif [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "📱 Detected Linux"

    # Start backend in background
    echo "🔧 Starting backend..."
    gnome-terminal -- bash -c "cd $(pwd) && bash start-backend.sh; exec bash" &

    # Wait for backend to start
    echo "⏳ Waiting for backend to start..."
    sleep 15

    # Start frontend in background
    echo "🎨 Starting frontend..."
    gnome-terminal -- bash -c "cd $(pwd) && bash start-frontend.sh; exec bash" &

    echo ""
    echo "✅ Services are starting in separate Terminal windows!"

else
    echo "❌ Unsupported operating system: $OSTYPE"
    echo "Please run start-backend.sh and start-frontend.sh separately in different terminals."
    exit 1
fi
