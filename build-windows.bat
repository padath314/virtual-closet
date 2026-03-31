@echo off
REM Virtual Closet - Windows App Builder
REM Run this script on Windows with JDK 21+ installed

setlocal enabledelayedexpansion

set APP_NAME=Virtual Closet
set VERSION=1.0.0
set MAIN_CLASS=com.virtualcloset.app.VirtualClosetApp
set BUNDLE_JAR=virtual-closet-1.0.0-SNAPSHOT-windows-bundle.jar

echo ========================================
echo  Virtual Closet - Windows Build
echo ========================================
echo.

echo [1/4] Checking Java version...
java -version
if errorlevel 1 (
    echo ERROR: Java not found. Please install JDK 21+
    pause
    exit /b 1
)

echo.
echo [2/4] Building fat JAR with dependencies...
call mvn clean package -Pwindows-bundle -DskipTests
if errorlevel 1 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)

echo.
echo [3/4] Preparing input folder...
if exist target\jpackage-input rmdir /s /q target\jpackage-input
if exist target\app rmdir /s /q target\app
mkdir target\jpackage-input
copy target\%BUNDLE_JAR% target\jpackage-input\app.jar

echo.
echo [4/4] Creating Windows executable...

REM Check if icon exists
set ICON_ARG=
if exist icon.ico (
    echo Found icon.ico
    set ICON_ARG=--icon icon.ico
) else (
    echo No icon.ico found - using default icon
)

jpackage ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --app-version "%VERSION%" ^
    --input target\jpackage-input ^
    --main-jar app.jar ^
    --main-class %MAIN_CLASS% ^
    --dest target\app ^
    --java-options "-Xmx2g" ^
    --win-console ^
    %ICON_ARG%

if errorlevel 1 (
    echo ERROR: jpackage failed
    pause
    exit /b 1
)

echo.
echo ========================================
echo  BUILD SUCCESSFUL!
echo ========================================
echo.
echo Your app is ready at:
echo    target\app\%APP_NAME%\%APP_NAME%.exe
echo.
echo You can also create an installer with:
echo    jpackage --type msi ...
echo.
pause
