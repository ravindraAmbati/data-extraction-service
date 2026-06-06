@echo off
setlocal
set APP_NAME=data-extraction-service
set PROJECT_DIR=%~dp0..\..
if "%APP_JAR%"=="" set APP_JAR=%PROJECT_DIR%\target\%APP_NAME%.jar
if "%APP_PROFILE%"=="" set APP_PROFILE=local
if "%PID_FILE%"=="" set PID_FILE=%PROJECT_DIR%\%APP_NAME%.pid

if not exist "%APP_JAR%" (
  echo JAR not found: %APP_JAR%
  echo Run: mvn package
  exit /b 1
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0start.ps1" -AppJar "%APP_JAR%" -Profile "%APP_PROFILE%" -PidFile "%PID_FILE%"
