#!/bin/bash

echo "🚀 Starting Operator Manager Frontend..."

# Check if Node.js is installed
if ! command -v node &> /dev/null; then
    echo "❌ Node.js is not installed. Please install Node.js first."
    exit 1
fi

# Check if npm is installed
if ! command -v npm &> /dev/null; then
    echo "❌ npm is not installed. Please install npm first."
    exit 1
fi

echo "✅ Prerequisites check passed"

# Navigate to frontend directory
cd operator-manager-web

# Install dependencies if node_modules doesn't exist
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Check if installation was successful
if [ $? -ne 0 ]; then
    echo "❌ Failed to install dependencies. Please check the errors above."
    exit 1
fi

echo "✅ Dependencies installed"

# Start the development server
echo "🎯 Starting the development server..."
echo "📱 Frontend will be available at: http://localhost:5173"
npm run dev
