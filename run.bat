@echo off
REM ========================================================
REM  College Certificate Management System - Windows Launcher
REM ========================================================
title College Certificate Management System
cd /d "%~dp0"

REM 1. Check bundled private JRE
if exist "jre\bin\javaw.exe" (
    start "" "jre\bin\javaw.exe" -jar "CollegeCertGen-Pro.jar"
    exit /b 0
)

REM 2. Check native Windows Executable
if exist "CollegeCertificateManagementSystem.exe" (
    start "" "CollegeCertificateManagementSystem.exe"
    exit /b 0
)

if exist "target\CollegeCertificateManagementSystem.exe" (
    start "" "target\CollegeCertificateManagementSystem.exe"
    exit /b 0
)

REM 3. Check system Java
set JAR_FILE=CollegeCertGen-Pro.jar
if not exist "%JAR_FILE%" set JAR_FILE=target\CollegeCertGen-Pro.jar

where javaw >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    start "" javaw -jar "%JAR_FILE%"
    exit /b 0
)

where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    start "" java -jar "%JAR_FILE%"
    exit /b 0
)

echo [ERROR] Java Runtime not detected.
echo Please reinstall using CollegeCertificateManagementSystem-Installer.exe
echo or install Java 17+ from: https://adoptium.net/
pause
