@echo off
REM ========================================
REM 配置常量
REM ========================================
set BACKEND_PORT=8080
set JAVA_VERSION_MIN=21

REM 可选参数
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
echo    使用方式: stop-backend-local.bat ^<profile^>
echo    示例: stop-backend-local.bat postgresql
pause
exit /b 1

:PROFILE_VALID

echo 🛑 Stopping Operator Manager Backend (Local Mode - %DATABASE_TYPE%)...
echo    Profile: %PROFILE%
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
REM 2. 停止后端服务
REM ========================================
echo 🛑 Checking for existing backend process on port %BACKEND_PORT%...
echo    Profile: %PROFILE%

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
        echo ❌ Failed to stop old process (PID: !NEW_PID% is still running)
        echo    Please manually kill it: taskkill /F /PID !NEW_PID!
        pause
        exit /b 1
    )

    echo ✅ Backend process stopped successfully (PID: !OLD_PID!)
    echo    Profile: %PROFILE%
    echo    Database: %DATABASE_TYPE%
) else (
    echo ℹ️  No existing backend process found on port %BACKEND_PORT%
    echo    Profile: %PROFILE%
    echo    Database: %DATABASE_TYPE%
)

echo.
pause
