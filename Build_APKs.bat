@echo off
setlocal enabledelayedexpansion

:: Clear conflicting legacy ANDROID_PREFS_ROOT environment variable for AGP 9.x compatibility
set ANDROID_PREFS_ROOT=

:: ================================================================
:: CallBlocker Automated Build Script
:: Batch builds Debug and Release APKs
:: ================================================================

title CallBlocker Build Script

set "VERSION=1.0.2"

echo ================================================================
echo   CallBlocker Build Script (v%VERSION%)
echo ================================================================
echo.

echo [1/2] Building Debug APKs...
call gradlew.bat assembleDebug --daemon
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Debug build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] Debug APKs built successfully!
echo.

echo [2/2] Building Release APKs...
call gradlew.bat assembleRelease --daemon
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Release build failed!
    pause
    exit /b %ERRORLEVEL%
)
echo [SUCCESS] Release APKs built successfully!
echo.

echo ================================================================
echo   ALL BUILDS COMPLETED SUCCESSFULLY!
echo ================================================================
echo Output APK Locations:
echo   [Debug]   app\build\outputs\apk\debug\call-blocker-v%VERSION%-debug.apk
echo   [Release] app\build\outputs\apk\release\call-blocker-v%VERSION%-release.apk
echo ================================================================
echo.

pause
