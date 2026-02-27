@echo off
REM ========================================
REM 配置常量
REM ========================================
set BACKEND_PORT=8080
set FRONTEND_PORT=5173
set JAVA_VERSION_MIN=21

REM 默认参数
set MODE=%~1
if "%MODE%"=="" set MODE=local

set PROFILE=%~2
if "%PROFILE%"=="" set PROFILE=postgresql

REM ========================================
REM 检查参数
REM ========================================
if "%MODE%"=="local" goto MODE_VALID
if "%MODE%"=="docker" goto MODE_VALID
echo ❌ 不支持的模式: %MODE%
echo    支持的模式: local, docker
echo    使用方式: start-all.bat ^<mode^> [profile]
echo.
echo    示例:
echo      start-all.bat local postgresql  # 本地 PostgreSQL 模式
echo      start-all.bat local h2           # 本地 H2 模式
echo      start-all.bat docker                  # Docker 模式
pause
exit /b 1

:MODE_VALID

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
pause
exit /b 1

:PROFILE_VALID

echo 🎉 Starting Operator Manager (%MODE% Mode - %DATABASE_TYPE%)...
echo.

REM ========================================
REM 检查操作系统
REM ========================================
if not "%OS%"=="Windows_NT" (
    echo ❌ 此脚本仅支持 Windows 系统
    pause
    exit /b 1
)

echo 📱 Detected Windows
echo.

if "%MODE%"=="local" (
    REM 本地模式：在新的 CMD 窗口中启动服务
    echo 🔧 Starting backend (Local Mode - %DATABASE_TYPE%)...
    echo 🔧 Starting frontend...
    echo.

    REM 在新的 CMD 窗口中启动后端
    start "Operator Manager Backend" cmd /k "cd /d %~dp0 && start-backend-local.bat %PROFILE%"

    REM 等待一段时间让服务启动
    timeout /t 20 /nobreak

    REM 在新的 CMD 窗口中启动前端
    start "Operator Manager Frontend" cmd /k "cd /d %~dp0 && start-frontend.bat"

    echo.
    echo ✅ Services are starting in separate windows!
    echo 📊 Backend API: http://localhost:8080
    echo 📱 Frontend: http://localhost:5173
    echo.
    echo 🔧 Backend Database: %DATABASE_TYPE%
    echo 📚 API Documentation: http://localhost:8080/swagger-ui.html
    echo.
    echo ⚠️  Stop services: stop-all.bat %MODE% %PROFILE%
    echo.
    echo 📝 提示: 请关闭窗口来停止服务
) else if "%MODE%"=="docker" (
    REM Docker 模式：在新的 CMD 窗口中启动服务
    echo 🐳 Starting backend (Docker Mode)...
    echo 🔧 Starting frontend...
    echo.

    REM 在新的 CMD 窗口中启动后端
    start "Operator Manager Backend" cmd /k "cd /d %~dp0 && start-backend.bat"

    REM 等待一段时间让服务启动
    timeout /t 20 /nobreak

    REM 在新的 CMD 窗口中启动前端
    start "Operator Manager Frontend" cmd /k "cd /d %~dp0 && start-frontend.bat"

    echo.
    echo ✅ Services are starting in separate windows!
    echo 📊 Backend API: http://localhost:8080
    echo 📱 Frontend: http://localhost:5173
    echo.
    echo 🐳 Docker Services:
    echo    - PostgreSQL: localhost:5432
    echo    - Redis: localhost:6379
    echo    - MinIO: http://localhost:9000 (Console: http://localhost:9001)
    echo.
    echo 📚 API Documentation: http://localhost:8080/swagger-ui.html
    echo.
    echo ⚠️  Stop services: stop-all.bat %MODE% %PROFILE%
    echo.
    echo 📝 提示: 请关闭窗口来停止服务
)

echo.
pause
