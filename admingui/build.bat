@echo off
echo ==========================================
echo Building AdminGUI Premium with Enhanced BanManager Integration (Gradle)
echo ==========================================

:: Navigate to the admingui directory
cd /d "%~dp0"

:: Check if Gradle wrapper exists
if not exist "gradlew.bat" (
    echo ERROR: Gradle wrapper not found
    echo Please ensure gradlew.bat is in the AdminGUI directory
    pause
    exit /b 1
)

echo Cleaning previous builds...
gradlew.bat clean

echo Compiling and building AdminGUI...
gradlew.bat build

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ==========================================
    echo BUILD SUCCESSFUL!
    echo ==========================================
    echo.
    echo The compiled AdminGUI-Premium-5.15.0-Enhanced.jar can be found in:
    echo %CD%\build\libs\AdminGUI-Premium-5.15.0-Enhanced.jar
    echo.
    echo To use this plugin:
    echo 1. Make sure BanManager is installed on your server
    echo 2. Copy AdminGUI-Premium-5.15.0-Enhanced.jar to your plugins folder
    echo 3. Restart your server
    echo.
    echo Features:
    echo - Supports Minecraft 1.21+ to latest versions
    echo - Direct BanManager API integration for better performance
    echo - All original AdminGUI functionality preserved
    echo - Enhanced error handling with automatic fallbacks
) else (
    echo.
    echo ==========================================
    echo BUILD FAILED!
    echo ==========================================
    echo.
    echo Please check the error messages above.
    echo Common issues:
    echo - Missing dependencies (check internet connection)
    echo - Java version compatibility (requires Java 8+)
    echo - Gradle configuration issues
)

echo.
pause 