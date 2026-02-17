@echo off
echo 🚀 Starting Operator Manager Backend...
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Maven is not installed. Please install Maven first.
    pause
    exit /b 1
)

echo ✅ Prerequisites check passed

REM Start Docker services
echo 📦 Starting Docker services (PostgreSQL, Redis, MinIO)...
docker-compose up -d

REM Wait for services to be ready
echo ⏳ Waiting for services to be ready...
timeout /t 15 /nobreak

REM Build the project
echo 🔨 Building the project...
cd operator-api
call mvn clean package -DskipTests

REM Check if build was successful
if %ERRORLEVEL% NEQ 0 (
    echo ❌ Build failed. Please check the errors above.
    pause
    exit /b 1
)

echo ✅ Build successful

REM Run the application
echo 🎯 Starting the application...
echo.
echo 📊 Backend will be available at: http://localhost:8080
echo 📚 API Documentation: http://localhost:8080/swagger-ui.html
echo.
echo ⚠️  Press Ctrl+C to stop the server
echo.

call mvn spring-boot:run -Dspring-boot.run.profiles=dev
