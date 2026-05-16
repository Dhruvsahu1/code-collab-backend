@echo off
REM CodeCollab Backend Startup Script for Windows
REM Run this script to start all backend services

echo Starting CodeCollab Backend Services...
echo.

REM Check if Maven is installed
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ERROR: Maven is not installed or not in PATH
    echo Please install Maven from https://maven.apache.org/
    exit /b 1
)

REM Function to start a service
:startService
set SERVICE_NAME=%~1
set SERVICE_DIR=%~2
set SERVICE_PORT=%~3

echo Starting %SERVICE_NAME% on port %SERVICE_PORT%...
cd %SERVICE_DIR%
start "codesync-%SERVICE_NAME%" cmd /k "mvn spring-boot:run"
goto :eof

REM Start services in order (auth first for dependencies)
call :startService "api-gateway" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\api-gateway" 8080
timeout /t 10

call :startService "auth-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\auth-service" 8081
timeout /t 5

call :startService "project-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\project-service" 8082
timeout /t 5

call :startService "file-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\file-service" 8083
timeout /t 5

call :startService "collab-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\collab-service" 8084
timeout /t 5

call :startService "execution-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\execution-service" 8085
timeout /t 5

call :startService "version-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\version-service" 8086
timeout /t 5

call :startService "comment-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\comment-service" 8087
timeout /t 5

call :startService "comment-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\comment-service" 8087
timeout /t 5

call :startService "chat-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\chat-service" 8089
timeout /t 5

call :startService "notification-service" "C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\notification-service" 8091

echo.
echo All services started!
echo.
echo Services running:
echo - API Gateway:      http://localhost:8080
echo - Auth Service:   http://localhost:8081
echo - Project Service: http://localhost:8082
echo - File Service:    http://localhost:8083
echo - Collab Service: http://localhost:8084
echo - Execution Svc:  http://localhost:8085
echo - Version Service: http://localhost:8086
echo - Comment Service: http://localhost:8087
echo - Chat Service:   http://localhost:8089
echo - Notification:   http://localhost:8091
echo.
echo Next, start the frontend:
echo   cd frontend
echo   npm run dev