# CodeCollab Backend Testing Guide

## Prerequisites
- Java 17+
- Maven 3.8+
- Docker & Docker Compose (optional, for full containerized setup)

---

## Running the Services

### Option 1: Run Individual Services (Development)

```bash
# Start auth-service
cd auth-service
mvn spring-boot:run

# In another terminal, start api-gateway
cd api-gateway
mvn spring-boot:run

# Start project-service
cd project-service
mvn spring-boot:run

# Start file-service
cd file-service
mvn spring-boot:run
```

### Option 2: Docker Compose (Full Stack)

```bash
docker-compose up --build
```

---

## API Testing with cURL

### Authentication Endpoints

#### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "securePassword123",
    "fullName": "John Doe"
  }'
```

**Response:**
```json
{
  "accessToken": "eyJhbGc...",
  "refreshToken": "eyJhbGc...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "user": {
    "userId": 1,
    "username": "johndoe",
    "email": "john@example.com",
    "fullName": "John Doe",
    "role": "DEVELOPER",
    "provider": "LOCAL",
    "isActive": true,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

#### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "securePassword123"
  }'
```

#### 3. Get Profile (Protected)

```bash
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

---

## Project Service Endpoints

All project endpoints are accessed through `http://localhost:8080/api/projects`

### Authentication Required Endpoints

#### 1. Create Project

```bash
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "MyProject",
    "description": "A test project",
    "language": "javascript",
    "visibility": "PUBLIC"
  }'
```

**Response:**
```json
{
  "projectId": 1,
  "ownerId": 1,
  "name": "MyProject",
  "description": "A test project",
  "language": "javascript",
  "visibility": "PUBLIC",
  "templateId": null,
  "archived": false,
  "createdAt": "2026-04-16T20:30:00",
  "updatedAt": "2026-04-16T20:30:00",
  "starCount": 0,
  "forkCount": 0
}
```

#### 2. Get Project by ID

```bash
curl -X GET http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 3. Update Project

```bash
curl -X PUT http://localhost:8080/api/projects/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "name": "UpdatedProject",
    "description": "Updated description",
    "language": "python",
    "visibility": "PRIVATE"
  }'
```

#### 4. Archive Project

```bash
curl -X PUT http://localhost:8080/api/projects/1/archive \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 5. Delete Project

```bash
curl -X DELETE http://localhost:8080/api/projects/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 6. Star/Unstar Project

```bash
# Toggle star
curl -X PUT http://localhost:8080/api/projects/1/star \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Response: { "starred": true }

# Check if starred
curl -X GET http://localhost:8080/api/projects/1/star \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Response: { "starred": true }
```

#### 7. Fork Project

```bash
curl -X POST http://localhost:8080/api/projects/fork/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 8. Get Dashboard Projects

```bash
curl -X GET http://localhost:8080/api/projects/dashboard \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Returns: User's projects + Public projects from others
```

#### 9. Get User's Projects

```bash
curl -X GET "http://localhost:8080/api/projects/owner?ownerId=1&page=0&size=20" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

#### 10. Get Projects User is Member Of

```bash
curl -X GET http://localhost:8080/api/projects/member \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Public Endpoints (No Auth Required)

#### 1. Get Public Projects

```bash
curl -X GET "http://localhost:8080/api/projects/public?page=0&size=20"
```

#### 2. Search Projects

```bash
curl -X GET "http://localhost:8080/api/projects/search?keyword=test&page=0&size=20"
```

#### 3. Filter by Language

```bash
curl -X GET "http://localhost:8080/api/projects/language?lang=javascript&page=0&size=20"
```

---

## Business Rules

### Ownership
- Only owner can create, update, delete, archive projects
- Only owner can change visibility

### Visibility
- PUBLIC: Visible to all users (even non-members)
- PRIVATE: Only owner + members can access

### Forking
- Creates new project with copied file structure
- New owner = current user
- Source project's forkCount incremented

### Starring
- Users can star/unstar projects
- Prevents duplicate stars
- Updates starCount in Project entity

---

## Service Ports

| Service | Port |
|---------|-----|
| API Gateway | 8080 |
| Auth Service | 8081 |
| Project Service | 8082 |
| File Service | 8083 |
| Collab Service | 8084 |
| Version Service | 8086 |
| Execution Service | 8085 |
| Comment Service | 8087 |
| Chat Service | 8089 |

---

## Database Configuration

### MySQL (Project & File Services - Default)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/codecollab_project
    username: root
    password: dhruvsh@97
```

### H2 (Auth Service - Development)
```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/codecollab-auth
  h2:
    console:
      enabled: true
```

---

## Troubleshooting

### Service Won't Start

1. Check port availability: `netstat -ano | findstr 8082`
2. Verify Java 17+: `java -version`
3. Check Maven: `mvn -version`

### Token Expiration

Access tokens expire after 24 hours. Use the refresh token endpoint to get a new access token.

### CORS Issues

All services have CORS configured for:
- http://localhost:5173
- http://localhost:3000
- http://127.0.0.1:5173

### Database Connection

Ensure MySQL is running with the correct credentials.
