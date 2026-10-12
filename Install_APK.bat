@echo off
setlocal enabledelayedexpansion

:: Clear conflicting legacy ANDROID_PREFS_ROOT environment variable
set ANDROID_PREFS_ROOT=

title Call Blocker - ADB Installer

:: Auto-detect ADB location from system PATH or Android Studio SDK path
set "ADB_CMD=adb"
where adb >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if exist "%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe" (
        set "ADB_CMD=%LOCALAPPDATA%\Android\Sdk\platform-tools\adb.exe"
    ) else if defined ANDROID_HOME (
        if exist "%ANDROID_HOME%\platform-tools\adb.exe" (
            set "ADB_CMD=%ANDROID_HOME%\platform-tools\adb.exe"
        )
    ) else if defined ANDROID_SDK_ROOT (
        if exist "%ANDROID_SDK_ROOT%\platform-tools\adb.exe" (
            set "ADB_CMD=%ANDROID_SDK_ROOT%\platform-tools\adb.exe"
        )
    )
)

echo ================================================================
echo   Call Blocker - ADB One-Click APK Installer
echo ================================================================
echo.

:: Check connected devices
echo [1/3] Checking connected ADB devices...
"%ADB_CMD%" devices
echo.

:MENU
echo ================================================================
echo Select the APK version you want to install onto your phone:
echo ================================================================
echo   [1] Install Debug APK   (app\build\outputs\apk\debug\call-blocker-v1.0.2-debug.apk)
echo   [2] Install Release APK (app\build\outputs\apk\release\call-blocker-v1.0.2-release.apk)
echo   [3] Exit
echo ================================================================
set /p CHOICE="Enter choice (1, 2, or 3): "

if "%CHOICE%"=="1" goto INSTALL_DEBUG
if "%CHOICE%"=="2" goto INSTALL_RELEASE
if "%CHOICE%"=="3" goto END

echo [ERROR] Invalid choice. Please try again.
echo.
goto MENU

:INSTALL_DEBUG
set "APK_PATH=app\build\outputs\apk\debug\call-blocker-v1.0.2-debug.apk"
if not exist "%APK_PATH%" (
    echo [ERROR] Debug APK not found at %APK_PATH%. Please run Build_APKs.bat first!
    echo.
    pause
    goto MENU
)
echo [2/3] Installing Debug APK onto device...
"%ADB_CMD%" install -r "%APK_PATH%"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] ADB installation failed!
) else (
    echo [SUCCESS] Debug APK installed successfully!
)
goto END

:INSTALL_RELEASE
set "APK_PATH=app\build\outputs\apk\release\call-blocker-v1.0.2-release.apk"
if not exist "%APK_PATH%" (
    echo [ERROR] Release APK not found at %APK_PATH%. Please run Build_APKs.bat first!
    echo.
    pause
    goto MENU
)
echo [2/3] Installing Release APK onto device...
"%ADB_CMD%" install -r "%APK_PATH%"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] ADB installation failed!
) else (
    echo [SUCCESS] Release APK installed successfully!
)
goto END

:END
echo.
pause
