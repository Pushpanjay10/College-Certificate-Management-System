@echo off
REM ========================================================
REM  College Certificate Management System - Windows Launcher
REM ========================================================
title College Certificate Management System

echo ========================================================
echo  Starting College Certificate Management System (Windows)
echo ========================================================

if exist "target\CollegeCertificateManagementSystem.exe" (
    echo Launching Native Windows Executable...
    start "" "target\CollegeCertificateManagementSystem.exe"
    exit /b 0
)

if exist "CollegeCertificateManagementSystem.exe" (
    echo Launching Native Windows Executable...
    start "" "CollegeCertificateManagementSystem.exe"
    exit /b 0
)

set JAR_PATH=target\CollegeCertGen-Pro.jar
if not exist "%JAR_PATH%" set JAR_PATH=CollegeCertGen-Pro.jar

if not exist "%JAR_PATH%" (
    echo Application binary not found. Attempting maven build...
    call mvn clean package -DskipTests
)

if not exist "%JAR_PATH%" (
    echo [ERROR] Application binary not found. Please install Java 17+ and Maven.
    pause
    exit /b 1
)

echo Launching Java Executable...
java -jar "%JAR_PATH%"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo An error occurred. Make sure Java 17 or higher is installed and on your PATH.
    echo Download link: https://adoptium.net/
    pause
)
