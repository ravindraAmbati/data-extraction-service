@echo off
setlocal
set PROJECT_DIR=%~dp0..\..
if "%APP_JAR%"=="" set APP_JAR=%PROJECT_DIR%\target\data-extraction-service.jar
if "%APP_PROFILE%"=="" set APP_PROFILE=local
if "%PID_FILE%"=="" set PID_FILE=%PROJECT_DIR%\data-extraction-service.pid
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0restart.ps1" -AppJar "%APP_JAR%" -Profile "%APP_PROFILE%" -PidFile "%PID_FILE%"
