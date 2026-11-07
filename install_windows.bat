@echo off
REM ==============================================================================
REM  College Certificate Management System - Windows Installer & Setup Script
REM ==============================================================================
title College Certificate Management System Setup
color 0b

echo.
echo ==============================================================================
echo       COLLEGE CERTIFICATE MANAGEMENT SYSTEM - WINDOWS SETUP
echo ==============================================================================
echo.

set SCRIPT_DIR=%~dp0
set TARGET_EXE=%SCRIPT_DIR%CollegeCertificateManagementSystem.exe
if not exist "%TARGET_EXE%" set TARGET_EXE=%SCRIPT_DIR%target\CollegeCertificateManagementSystem.exe

set TARGET_JAR=%SCRIPT_DIR%CollegeCertGen-Pro.jar
if not exist "%TARGET_JAR%" set TARGET_JAR=%SCRIPT_DIR%target\CollegeCertGen-Pro.jar

set ICON_FILE=%SCRIPT_DIR%app_icon.ico
if not exist "%ICON_FILE%" set ICON_FILE=%SCRIPT_DIR%src\main\resources\images\app_icon.ico

echo [1/3] Checking Java Runtime Environment (JRE 17+)...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Java is not detected in your PATH.
    echo Please download and install free OpenJDK 17 or 21 from:
    echo https://adoptium.net/
    echo.
) else (
    echo [OK] Java runtime found.
)

echo [2/3] Verifying Application Binaries...
if exist "%TARGET_EXE%" (
    echo [OK] Native Windows Executable found: %TARGET_EXE%
) else (
    echo [!] Native executable not in root, checking JAR...
)

echo [3/3] Creating Desktop Shortcut...
powershell -NoProfile -ExecutionPolicy Bypass -Command "$ws = New-Object -ComObject WScript.Shell; $desk = [Environment]::GetFolderPath('Desktop'); $lnk = $ws.CreateShortcut((Join-Path $desk 'College Certificate Management System.lnk')); $lnk.TargetPath = '%TARGET_EXE%'; $lnk.WorkingDirectory = '%SCRIPT_DIR%'; if (Test-Path '%ICON_FILE%') { $lnk.IconLocation = '%ICON_FILE%'; }; $lnk.Description = 'College Certificate Management System'; $lnk.Save(); Write-Host '[OK] Desktop shortcut created successfully!'"

echo.
echo ==============================================================================
echo  INSTALLATION COMPLETE!
echo  You can now start the application from your Desktop shortcut or run:
echo  CollegeCertificateManagementSystem.exe
echo ==============================================================================
echo.
pause

