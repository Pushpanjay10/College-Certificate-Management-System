@echo off
REM ========================================================
REM  College Certificate Management System - Windows Launcher
REM ========================================================
title College Certificate Management System
cd /d "%~dp0"

REM 1. Check bundled private JRE
if exist "%~dp0jre\bin\javaw.exe" (
    start "" "%~dp0jre\bin\javaw.exe" -jar "%~dp0CollegeCertGen-Pro.jar"
    exit /b 0
)

REM 2. Check native Windows Executable
if exist "%~dp0CollegeCertificateManagementSystem.exe" (
    start "" "%~dp0CollegeCertificateManagementSystem.exe"
    exit /b 0
)

REM 3. Fallback to system Java
where javaw >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    start "" javaw -jar "%~dp0CollegeCertGen-Pro.jar"
    exit /b 0
)

where java >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    start "" java -jar "%~dp0CollegeCertGen-Pro.jar"
    exit /b 0
)

echo [ERROR] Java Runtime not detected.
echo Please reinstall using CollegeCertificateManagementSystem-Installer.exe
echo or install Java 17+ from: https://adoptium.net/
pause
