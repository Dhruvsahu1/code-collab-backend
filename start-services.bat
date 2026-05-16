@echo off
REM CodeCollab Backend Startup Script for Windows
REM Prerequisites: Maven, MySQL (running on localhost:3306), Redis (running on localhost:6379)

echo ========================================
echo CodeCollab Backend Startup
echo ========================================
echo.

REM Check if MySQL is running (optional - services will create databases)
echo Checking MySQL connection...
mysql -u root -pdhruvsh@97 -e "SELECT 1" >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] MySQL is running
) else (
    echo [WARN] MySQL not accessible - services may fail to start
    echo Please ensure MySQL is running with root / dhruvsh@97
)

REM Check if Redis is running (for collab-service)
echo Checking Redis connection...
redis-cli ping >nul 2>&1
if %ERRORLEVEL% equ 0 (
    echo [OK] Redis is running
) else (
    echo [WARN] Redis not running - collab-service may fail
    echo Please start Redis on localhost:6379
)

echo.
echo Starting services...
echo.

REM Start each service in a new window
echo [1/10] Starting API Gateway (8080)...
start "CodeSync-API-Gateway" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\api-gateway && mvn spring-boot:run"

timeout /t 15

echo [2/10] Starting Auth Service (8081)...
start "CodeSync-Auth" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\auth-service && mvn spring-boot:run"

timeout /t 10

echo [3/10] Starting Project Service (8082)...
start "CodeSync-Project" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\project-service && mvn spring-boot:run"

timeout /t 10

echo [4/10] Starting File Service (8083)...
start "CodeSync-File" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\file-service && mvn spring-boot:run"

timeout /t 10

echo [5/10] Starting Collab Service (8084)...
start "CodeSync-Collab" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\collab-service && mvn spring-boot:run"

timeout /t 10

echo [6/10] Starting Execution Service (8085)...
start "CodeSync-Execution" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\execution-service && mvn spring-boot:run"

timeout /t 10

echo [7/10] Starting Version Service (8086)...
start "CodeSync-Version" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\version-service && mvn spring-boot:run"

timeout /t 10

echo [8/10] Starting Comment Service (8087)...
start "CodeSync-Comment" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\comment-service && mvn spring-boot:run"

timeout /t 10

echo [9/10] Starting Notification Service (8091)...
start "CodeSync-Notification" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\notification-service && mvn spring-boot:run"

timeout /t 10

echo [10/10] Starting Chat Service (8089)...
start "CodeSync-Chat" cmd /k "cd /d C:\Users\dhruv\OneDrive\Desktop\Code-collab-backend\chat-service && mvn spring-boot:run"

echo.
echo ========================================
echo Services started! Check each window.
echo API Gateway: http://localhost:8080
echo Frontend:    cd frontend ^&^& npm run dev
echo ========================================