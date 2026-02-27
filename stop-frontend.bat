@echo off
REM ========================================
REM 配置常量
REM ========================================
set FRONTEND_PORT=5173

echo 🛑 Stopping Operator Manager Frontend...
echo.

REM ========================================
REM 1. 检查 Node.js
REM ========================================
where node >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Node.js is not installed. Please install Node.js first.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed (Node.js installed)

REM ========================================
REM 2. 停止前端服务
REM ========================================
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
echo ✅ Frontend service stopped
echo.
pause
