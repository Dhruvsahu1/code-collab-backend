#!/bin/bash
# CodeCollab Backend Startup Script

echo "Starting CodeCollab Backend Services..."

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting API Gateway on port 8080...${NC}"
cd api-gateway
mvn spring-boot:run -q &
GATEWAY_PID=$!
cd ..

sleep 5

echo -e "${GREEN}Starting Auth Service on port 8081...${NC}"
cd auth-service
mvn spring-boot:run -q &
AUTH_PID=$!
cd ..

sleep 5

echo -e "${GREEN}Starting other services...${NC}"
# Start other services in background
for service in project-service file-service collab-service version-service execution-service comment-service chat-service; do
    if [ -d "$service" ]; then
        echo "Starting $service..."
        cd $service
        mvn spring-boot:run -q &
        cd ..
    fi
done

echo ""
echo -e "${YELLOW}All services started!${NC}"
echo ""
echo "Services:"
echo "  - API Gateway:    http://localhost:8080"
echo "  - Auth Service:   http://localhost:8081"
echo "  - H2 Console:     http://localhost:8081/h2-console"
echo ""
echo "Frontend: http://localhost:5173 (run 'cd frontend && npm run dev')"
echo ""
echo "Press Ctrl+C to stop all services"

# Wait for ctrl+c
wait