@echo off
REM ========================================
REM 配置常量
REM ========================================
set BACKEND_PORT=8080
set JAVA_VERSION_MIN=21

echo 🚀 Starting Operator Manager Backend (Docker Mode)...
echo.

REM ========================================
REM 1. 检查 Java 和 Maven
REM ========================================

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven is not installed. Please install Maven first.
    pause
    exit /b 1
)

REM Check if Java is installed
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java is not installed. Please install Java 21 first.
    pause
    exit /b 1
)

REM Check Java version (21+ required)
for /f "tokens=3" %%v in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVA_VERSION_STRING=%%v
    set JAVA_VERSION_STRING=!JAVA_VERSION_STRING:"=!
    for /f "tokens=1,2 delims=." %%a in ("!JAVA_VERSION_STRING!") do (
        if "%%a"=="1" (
            set JAVA_MAJOR=%%b
        ) else (
            set JAVA_MAJOR=%%a
        )
    )
)

if !JAVA_MAJOR! LSS %JAVA_VERSION_MIN% (
    echo ❌ Java %JAVA_VERSION_MIN% or higher is required. Current version: !JAVA_MAJOR!
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed (Java !JAVA_MAJOR!, Maven installed)

REM ========================================
REM 2. 停止后端服务（如果有运行）
REM ========================================
echo 🛑 Checking for existing backend process on port %BACKEND_PORT%...

REM 查找占用端口的进程
set OLD_PID=
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING"') do (
    set OLD_PID=%%a
)

if defined OLD_PID (
    echo ⚠️  Found existing backend process (PID: !OLD_PID!), stopping it...
    taskkill /F /PID !OLD_PID! >nul 2>nul
    timeout /t 2 /nobreak

    REM 再次检查是否停止成功
    set NEW_PID=
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING"') do (
        set NEW_PID=%%a
    )

    if defined NEW_PID (
        echo ❌ Failed to stop old process (PID: !NEW_PID! is still running)
        echo    Please manually kill it: taskkill /F /PID !NEW_PID!
        pause
        exit /b 1
    )

    echo ✅ Old backend process stopped successfully
) else (
    echo ℹ️  No existing backend process found
)

REM ========================================
REM 3. 启动 Docker 服务
REM ========================================
echo.
echo 📦 Starting Docker services (PostgreSQL, Redis, MinIO)...
docker-compose up -d

REM 等待服务就绪（等待数据库连接可用）
echo ⏳ Waiting for Docker services to be ready...
timeout /t 8 /nobreak

REM ========================================
REM 4. 编译项目
REM ========================================
echo.
echo 🔨 Building project in operator-api directory...
cd /d "%~dp0operator-api"

REM 先 clean，确保使用最新的编译代码
call mvn clean -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven clean failed. Please check for errors above.
    pause
    exit /b 1
)

REM 编译
call mvn package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed. Please check for errors above.
    pause
    exit /b 1
)

echo ✅ Build successful

REM ========================================
REM 5. 启动应用
REM ========================================
echo.
echo 🎯 Starting backend application...
echo    Running from: operator-api directory
echo    Profile: dev
echo    Port: %BACKEND_PORT%
echo    Mode: Docker

REM 在前台启动，方便调试时查看日志
call mvn spring-boot:run -Dspring-boot.run.profiles=dev
