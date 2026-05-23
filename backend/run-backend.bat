@echo off
title HRMS Backend Local Server Launcher
echo ===================================================
echo Starting HRMS Backend (Java Spring Boot)
echo ===================================================
powershell -ExecutionPolicy Bypass -File "%~dp0run-backend.ps1"
if %errorlevel% neq 0 (
    echo.
    echo [ERROR] Backend execution failed. Check console outputs above.
)
pause
