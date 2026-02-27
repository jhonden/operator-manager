@echo off
REM ========================================
REM 配置常量
REM ========================================
set BACKEND_PORT=8080
set JAVA_VERSION_MIN=21

REM 支持的 profile: h2 (使用 H2) 或 postgresql (使用 PostgreSQL)
REM 默认使用 postgresql
set PROFILE=%~1
if "%PROFILE%"=="" set PROFILE=postgresql

if "%PROFILE%"=="h2" (
    set PROFILE_NAME=dev
    set DATABASE_TYPE=H2
    goto PROFILE_VALID
)
if "%PROFILE%"=="postgresql" (
    set PROFILE_NAME=dev-postgresql
    set DATABASE_TYPE=PostgreSQL
    goto PROFILE_VALID
)

echo ❌ 不支持的 profile: %PROFILE%
echo    支持的 profile: h2, postgresql
echo    使用方式: start-backend-local.bat ^<profile^>
echo    示例: start-backend-local.bat postgresql
pause
exit /b 1

:PROFILE_VALID

echo 🚀 Starting Operator Manager Backend (Local Mode - %DATABASE_TYPE%)...
echo    Database: %DATABASE_TYPE%
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
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING"') do (
    set OLD_PID=%%a
    goto FOUND_PROCESS
)

echo ℹ️  No existing backend process found
goto BUILD_PROJECT

:FOUND_PROCESS
echo ⚠️  Found existing backend process (PID: !OLD_PID!), stopping it...
taskkill /F /PID !OLD_PID! >nul 2>nul
timeout /t 2 /nobreak

REM 再次检查是否停止成功
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING"') do (
    echo ❌ Failed to stop old process (PID: %%a is still running)
    echo    Please manually kill it: taskkill /F /PID %%a
    pause
    exit /b 1
)

echo ✅ Old backend process stopped successfully

:BUILD_PROJECT
REM ========================================
REM 3. 编译项目
REM ========================================
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
call mvn compile -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed. Please check for errors above.
    pause
    exit /b 1
)

echo ✅ Build successful

REM ========================================
REM 4. 启动应用
REM ========================================
echo 🎯 Starting backend application...
echo    Running from: operator-api directory
echo    Profile: %PROFILE_NAME%
echo    Port: %BACKEND_PORT%
echo    Mode: Local (No Docker)
echo.

REM 在前台启动，方便调试时查看日志
call mvn spring-boot:run -Dspring-boot.run.profiles=%PROFILE_NAME%
