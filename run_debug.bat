@echo off
title College Certificate Management System [Debug Mode]
cd /d "%~dp0"
echo ===================================================
echo  College Certificate Management System - Diagnostic
echo ===================================================
echo Current Directory: %CD%
echo.

if exist "%~dp0jre\bin\java.exe" (
    echo Using Bundled Private OpenJDK 17 Runtime...
    echo.
    "%~dp0jre\bin\java.exe" -jar "%~dp0CollegeCertGen-Pro.jar"
    goto finish
)

echo Bundled JRE not found, trying system Java...
java -jar "%~dp0CollegeCertGen-Pro.jar"

:finish
echo.
echo Application exited with code: %ERRORLEVEL%
echo If the application didn't open or closed unexpectedly, see the output above.
echo Diagnostic logs: %%USERPROFILE%%\.college_cert_gen\app_startup.log
echo.
pause

