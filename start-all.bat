@echo off
echo ========================================
echo CodeSync - Start All Services
echo ========================================

echo.
echo [1] Checking MySQL...
netstat -ano | findstr "3306" | findstr "LISTENING" >nul
if %errorlevel%==0 (
    echo    - MySQL: Running
) else (
    echo    - MySQL: NOT RUNNING - Start MySQL first!
)

echo.
echo [2] Checking running services...
netstat -ano | findstr "8080" | findstr "LISTENING" >nul
if %errorlevel%==0 (echo    - API Gateway (8080): Running) else (echo    - API Gateway (8080): NOT RUNNING)

netstat -ano | findstr "8081" | findstr "LISTENING" >nul
if %errorlevel%==0 (echo    - Auth Service (8081): Running) else (echo    - Auth Service (8081): NOT RUNNING)

echo.
echo ========================================
echo To start all services, run each in separate terminals:
echo ========================================
echo.
echo Terminal 1 - API Gateway:
echo   cd api-gateway ^&^& mvn spring-boot:run
echo.
echo Terminal 2 - Auth Service:
echo   cd auth-service ^&^& mvn spring-boot:run
echo.
echo Terminal 3 - Project Service:
echo   cd project-service ^&^& mvn spring-boot:run
echo.
echo Terminal 4 - File Service:
echo   cd file-service ^&^& mvn spring-boot:run
echo.
echo Terminal 5 - Collab Service:
echo   cd collab-service ^&^& mvn spring-boot:run
echo.
echo Terminal 6 - Execution Service:
echo   cd execution-service ^&^& mvn spring-boot:run
echo.
echo ========================================
echo Frontend (in another terminal):
echo   cd frontend ^&^& npm run dev
echo ========================================
echo.
echo Test API:
echo   curl http://localhost:8080/api/auth/login
echo.
pause