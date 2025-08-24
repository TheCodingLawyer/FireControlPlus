#!/bin/bash

echo "=========================================="
echo "Building AdminGUI Premium with Enhanced BanManager Integration (Gradle)"
echo "=========================================="

# Navigate to the script directory
cd "$(dirname "$0")"

# Check if Gradle wrapper exists
if [ ! -f "gradlew" ]; then
    echo "ERROR: Gradle wrapper not found"
    echo "Please ensure gradlew is in the AdminGUI directory"
    echo "You may need to copy it from the main BanManager project"
    exit 1
fi

# Make gradlew executable
chmod +x gradlew

echo "Cleaning previous builds..."
./gradlew clean

echo "Compiling and building AdminGUI..."
./gradlew build

if [ $? -eq 0 ]; then
    echo
    echo "=========================================="
    echo "BUILD SUCCESSFUL!"
    echo "=========================================="
    echo
    echo "The compiled AdminGUI-Premium-5.15.0-Enhanced.jar can be found in:"
    echo "$(pwd)/build/libs/AdminGUI-Premium-5.15.0-Enhanced.jar"
    echo
    echo "To use this plugin:"
    echo "1. Make sure BanManager is installed on your server"
    echo "2. Copy AdminGUI-Premium-5.15.0-Enhanced.jar to your plugins folder"
    echo "3. Restart your server"
    echo
    echo "Features:"
    echo "- Supports Minecraft 1.21+ to latest versions"
    echo "- Direct BanManager API integration for better performance"
    echo "- All original AdminGUI functionality preserved"
    echo "- Enhanced error handling with automatic fallbacks"
else
    echo
    echo "=========================================="
    echo "BUILD FAILED!"
    echo "=========================================="
    echo
    echo "Please check the error messages above."
    echo "Common issues:"
    echo "- Missing dependencies (check internet connection)"
    echo "- Java version compatibility (requires Java 8+)"
    echo "- Gradle configuration issues"
fi

echo
read -p "Press Enter to continue..." 