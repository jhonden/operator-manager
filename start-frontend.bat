@echo off
echo 🚀 Starting Operator Manager Frontend...
echo.

REM Check if Node.js is installed
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Node.js is not installed. Please install Node.js first.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed

REM Navigate to frontend directory
cd operator-manager-web

REM Install dependencies if node_modules doesn't exist
if not exist "node_modules" (
    echo 📦 Installing dependencies...
    call npm install
)

REM Check if installation was successful
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Failed to install dependencies. Please check the errors above.
    pause
    exit /b 1
)

echo ✅ Dependencies installed

REM Start the development server
echo 🎯 Starting the development server...
echo 📱 Frontend will be available at: http://localhost:5173
echo.
echo ⚠️  Press Ctrl+C to stop the server
echo.

call npm run dev
