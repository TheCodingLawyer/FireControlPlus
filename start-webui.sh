#!/bin/bash

echo "🚀 BanManager WebUI Startup Script"
echo "=================================="

# Set working directory to WebUI
cd WebUI

# Check if migration is needed by looking for the table
echo "🔍 Checking if database migration is needed..."

# Try to run migration (will fail gracefully if already done)
echo "🔄 Running database migration..."
node ../run-migration.js || echo "⚠️  Migration script not available, continuing..."

# Alternative: try standard migration
node cli/index.js migrate 2>/dev/null || echo "📝 Standard migration not available"

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
fi

# Build the application
echo "🔨 Building WebUI..."
npm run build || echo "⚠️  Build failed, using existing build"

# Start the server
echo "🌐 Starting WebUI server..."
exec npm start 