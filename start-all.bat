@echo off
echo ========================================================
echo Starting Code Collab Backend Microservices and Frontend
echo ========================================================
echo.
echo Make sure you have PostgreSQL running locally if you are not using Docker!
echo Opening each service in a new terminal window...
echo.

echo Starting auth-service...
start "Auth Service" cmd /k "cd auth-service && title Auth Service && mvn spring-boot:run"

echo Starting project-service...
start "Project Service" cmd /k "cd project-service && title Project Service && mvn spring-boot:run"

echo Starting file-service...
start "File Service" cmd /k "cd file-service && title File Service && mvn spring-boot:run"

echo Starting collab-service...
start "Collab Service" cmd /k "cd collab-service && title Collab Service && mvn spring-boot:run"

echo Starting execution-service...
start "Execution Service" cmd /k "cd execution-service && title Execution Service && mvn spring-boot:run"

echo Starting version-service...
start "Version Service" cmd /k "cd version-service && title Version Service && mvn spring-boot:run"

echo Starting comment-service...
start "Comment Service" cmd /k "cd comment-service && title Comment Service && mvn spring-boot:run"

echo Starting chat-service...
start "Chat Service" cmd /k "cd chat-service && title Chat Service && mvn spring-boot:run"

echo Starting notification-service...
start "Notification Service" cmd /k "cd notification-service && title Notification Service && mvn spring-boot:run"

echo Starting api-gateway...
:: Waiting a few seconds for others to start before gateway
timeout /t 5 /nobreak > nul
start "API Gateway" cmd /k "cd api-gateway && title API Gateway && mvn spring-boot:run"

echo Starting frontend...
start "Frontend React App" cmd /k "cd frontend && title Frontend && npm run dev"

echo.
echo All services have been launched in separate windows!
echo You can close this window now.
pause
