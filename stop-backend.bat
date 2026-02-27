@echo off
REM ========================================
REM 配置常量
REM ========================================
set BACKEND_PORT=8080
set JAVA_VERSION_MIN=21

echo 🛑 Stopping Operator Manager Backend (Docker Mode)...
echo.

REM ========================================
REM 1. 检查 Java 和 Maven
REM ========================================
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven is not installed. Please install Maven first.
    pause
    exit /b 1
)

where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Java is not installed. Please install Java 21 first.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed (Java and Maven installed)

REM ========================================
REM 2. 停止后端服务
REM ========================================
echo 🛑 Checking for existing backend process on port %BACKEND_PORT%...

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

    echo ✅ Backend process stopped successfully (PID: !OLD_PID!)
) else (
    echo ℹ️  No existing backend process found on port %BACKEND_PORT%
)

echo.
pause
