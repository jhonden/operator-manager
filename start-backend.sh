#!/bin/bash

echo "🚀 Starting Operator Manager Backend..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed. Please install Maven first."
    exit 1
fi

# Check if Java 21 is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 first."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "❌ Java 21 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Prerequisites check passed"

# Start Docker services
echo "📦 Starting Docker services (PostgreSQL, Redis, MinIO)..."
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."
sleep 10

# Build the project
echo "🔨 Building the project..."
cd operator-api
mvn clean package -DskipTests

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "❌ Build failed. Please check the errors above."
    exit 1
fi

echo "✅ Build successful"

# Run the application
echo "🎯 Starting the application..."
mvn spring-boot:run -Dspring-boot.run.profiles=dev
