@echo off
echo ========================================
echo CodeSync - Start Collab Service
echo ========================================
echo.
echo Make sure MySQL is running first!
echo.
echo [1] Starting API Gateway on port 8080...
cd api-gateway
start "API-Gateway" cmd /k mvn spring-boot:run
cd ..

timeout /nobreak /t 10

echo [2] Starting Auth Service on port 8081...
cd auth-service
start "Auth-Service" cmd /k mvn spring-boot:run
cd ..

timeout /nobreak /t 10

echo [3] Starting Collab Service on port 8084...
cd collab-service
start "Collab-Service" cmd /k mvn spring-boot:run
cd ..

timeout /nobreak /t 10

echo [4] Starting Frontend on port 5173...
cd frontend
start "Frontend" cmd /k npm run dev
cd ..

echo.
echo ========================================
echo All services starting...
echo.
echo After startup, open: http://localhost:5173
echo.
echo Check console windows for any errors!
echo ========================================
pause