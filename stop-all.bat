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
if "%MODE%"=="local" (
    echo 🛑 Stopping Operator Manager (Local Mode)...
    goto MODE_VALID
)
if "%MODE%"=="docker" (
    echo 🛑 Stopping Operator Manager (Docker Mode)...
    goto MODE_VALID
)

echo ❌ 不支持的模式: %MODE%
echo    支持的模式: local, docker
echo    使用方式: stop-all.bat ^<mode^> [profile]
echo.
echo    示例:
echo      stop-all.bat local postgresql  # 停止本地 PostgreSQL 模式
echo      stop-all.bat docker                  # 停止 Docker 模式
pause
exit /b 1

:MODE_VALID

if "%PROFILE%"=="h2" (
    set DATABASE_TYPE=H2
) else if "%PROFILE%"=="postgresql" (
    set DATABASE_TYPE=PostgreSQL
) else (
    echo ❌ 不支持的 profile: %PROFILE%
    echo    支持的 profile: h2, postgresql
    pause
    exit /b 1
)

echo    Mode: %MODE%
echo    Database: %DATABASE_TYPE%
echo.

REM ========================================
REM 1. 停止后端服务
REM ========================================
echo 🛑 Stopping backend service on port %BACKEND_PORT%...
echo    Profile: %PROFILE%

set BACKEND_PID=
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING"') do (
    set BACKEND_PID=%%a
)

if defined BACKEND_PID (
    echo ⚠️  Found backend process (PID: %BACKEND_PID%), stopping it...
    taskkill /F /PID %BACKEND_PID% >nul 2>nul
    timeout /t 2 /nobreak

    REM 再次检查是否停止成功
    set NEW_PID=
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%BACKEND_PORT%" ^| findstr "LISTENING"') do (
        set NEW_PID=%%a
    )

    if defined NEW_PID (
        echo ❌ Failed to stop backend process (PID: %NEW_PID% is still running)
        echo    Please manually kill it: taskkill /F /PID %NEW_PID%
        pause
        exit /b 1
    )

    echo ✅ Backend process stopped successfully (PID: %BACKEND_PID%)
) else (
    echo ℹ️  No backend process found on port %BACKEND_PORT%
)

REM ========================================
REM 2. 停止前端服务
REM ========================================
echo.
echo 🛑 Stopping frontend service on port %FRONTEND_PORT%...

set FRONTEND_PID=
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%FRONTEND_PORT%" ^| findstr "LISTENING"') do (
    set FRONTEND_PID=%%a
)

if defined FRONTEND_PID (
    echo ⚠️  Found frontend process (PID: %FRONTEND_PID%), stopping it...
    taskkill /F /PID %FRONTEND_PID% >nul 2>nul
    timeout /t 2 /nobreak

    REM 再次检查是否停止成功
    set NEW_PID=
    for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":%FRONTEND_PORT%" ^| findstr "LISTENING"') do (
        set NEW_PID=%%a
    )

    if defined NEW_PID (
        echo ❌ Failed to stop frontend process (PID: %NEW_PID% is still running)
        echo    Please manually kill it: taskkill /F /PID %NEW_PID%
        pause
        exit /b 1
    )

    echo ✅ Frontend process stopped successfully (PID: %FRONTEND_PID%)
) else (
    echo ℹ️  No frontend process found on port %FRONTEND_PORT%
)

echo.
echo ✅ All services stopped
echo.
pause
