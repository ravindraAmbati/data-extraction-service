@echo off
setlocal
set PROJECT_DIR=%~dp0..\..
if "%PID_FILE%"=="" set PID_FILE=%PROJECT_DIR%\data-extraction-service.pid
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0stop.ps1" -PidFile "%PID_FILE%"
