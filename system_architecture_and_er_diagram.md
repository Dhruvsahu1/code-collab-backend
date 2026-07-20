# System Architecture & ER Diagram

This document outlines the architecture and data model for the **Code Collab Backend**, a microservices-based platform designed for real-time collaborative coding.

## 1. High-Level Architecture Diagram

The system employs a microservices architecture, where a central API Gateway routes traffic from clients to the appropriate backend services. All services currently connect to a shared PostgreSQL database, operating within a Docker network (`codecollab-network`).

```mermaid
graph TD
    Client([Client Apps / Frontend])
    Gateway[API Gateway :8080]
    DB[(PostgreSQL Database :5432)]

    Client -->|HTTP/WebSocket Requests| Gateway

    subgraph Microservices Layer
        Auth[auth-service :8081]
        Project[project-service :8082]
        File[file-service :8083]
        Collab[collab-service :8084]
        Exec[execution-service :8085]
        Version[version-service :8086]
        Comment[comment-service :8087]
        Chat[chat-service :8088]
        Notify[notification-service :8089]
    end

    Gateway --> Auth
    Gateway --> Project
    Gateway --> File
    Gateway --> Collab
    Gateway --> Exec
    Gateway --> Version
    Gateway --> Comment
    Gateway --> Chat
    Gateway --> Notify

    Auth -.-> DB
    Project -.-> DB
    File -.-> DB
    Collab -.-> DB
    Exec -.-> DB
    Version -.-> DB
    Comment -.-> DB
    Chat -.-> DB
    Notify -.-> DB
    
    classDef service fill:#f9f,stroke:#333,stroke-width:2px;
    class Auth,Project,File,Collab,Exec,Version,Comment,Chat,Notify service;
    classDef gateway fill:#bbf,stroke:#333,stroke-width:2px;
    class Gateway gateway;
```

> [!NOTE]
> All services run in isolated Docker containers but share the same virtual network (`codecollab-network`). Service discovery and load balancing are handled internally or through the API gateway.

---

## 2. API Gateway Connection Explained

The **API Gateway** acts as the single entry point into the system for all client requests. 

### Core Responsibilities:
1. **Request Routing**: It takes incoming requests (e.g., `GET /api/projects`) and routes them to the exact downstream service (e.g., `project-service:8082`).
2. **Security & Authentication (Edge Security)**: It often intercepts requests to validate JWT tokens before forwarding them to underlying services.
3. **Cross-Origin Resource Sharing (CORS)**: The gateway handles CORS headers for frontend web applications.
4. **WebSocket Proxying**: For real-time features like chat and collaborative coding (handled by `chat-service` and `collab-service`), the gateway proxies WebSocket connections.

### Connection with Microservices:
In the `docker-compose.yml`, the `api-gateway` explicitly `depends_on` all other microservices. It connects to them via their Docker container names (e.g., `http://auth-service:8081`). Because they are all on the `codecollab-network` bridge network, the Gateway resolves these service names via Docker's internal DNS.

---

## 3. Entity-Relationship (ER) Diagram

Below is the database schema spanning across the different microservices. Note that in a strict microservices environment, these tables are logically (and sometimes physically) separated by bounded context, even if they share the same PostgreSQL instance.

```mermaid
erDiagram
    USERS {
        Long userId PK
        String username UK
        String email UK
        String passwordHash
        String fullName
        String role "DEVELOPER, ADMIN"
        String avatarUrl
        Boolean isActive
    }

    PROJECTS {
        Long projectId PK
        Long ownerId FK
        String name
        String description
        String language
        String visibility
        Boolean isArchived
        Int starCount
        Int forkCount
    }

    PROJECT_MEMBERS {
        Long id PK
        Long projectId FK
        Long userId FK
        String role "OWNER, COLLABORATOR"
        DateTime joinedAt
    }
    
    PROJECT_STARS {
        Long id PK
        Long projectId FK
        Long userId FK
    }

    CODE_FILES {
        Long fileId PK
        Long projectId FK
        String name
        String path
        String language
        Text content
        Long createdById FK
        Boolean isFolder
        Boolean isDeleted
    }

    SNAPSHOTS {
        Long snapshotId PK
        Long projectId FK
        Long fileId FK
        Long authorId FK
        String message
        Text content
        String hash
        Long parentSnapshotId FK
        String branch
        String tag
    }

    COLLAB_SESSIONS {
        Long id PK
        String sessionId UK
        Long projectId FK
        Long fileId FK
        Long ownerId FK
        Text code
        String status "ACTIVE, ENDED"
        String sessionPassword
    }

    PARTICIPANTS {
        Long id PK
        Long sessionId FK
        Long userId FK
        String socketId
        String role
    }

    COMMENTS {
        Long commentId PK
        Long projectId FK
        Long fileId FK
        Long authorId FK
        Text content
        Int lineNumber
        Long parentCommentId FK
        Boolean resolved
    }

    EXECUTION_JOBS {
        Long id PK
        String jobId
        Long userId FK
        Long fileId FK
        String language
        Text code
        String status
        Text stdout
        Text stderr
    }

    CHAT_MESSAGES {
        Long messageId PK
        Long projectId FK
        Long senderId FK
        Text content
        DateTime timestamp
    }

    NOTIFICATIONS {
        Long notificationId PK
        Long userId FK
        String message
        Boolean isRead
        DateTime createdAt
    }

    %% Relationships
    USERS ||--o{ PROJECTS : "owns (ownerId)"
    USERS ||--o{ PROJECT_MEMBERS : "is member of (userId)"
    PROJECTS ||--o{ PROJECT_MEMBERS : "has members"
    USERS ||--o{ PROJECT_STARS : "stars (userId)"
    PROJECTS ||--o{ PROJECT_STARS : "is starred by"
    
    PROJECTS ||--o{ CODE_FILES : "contains"
    USERS ||--o{ CODE_FILES : "creates (createdById)"
    
    CODE_FILES ||--o{ SNAPSHOTS : "has versions"
    PROJECTS ||--o{ SNAPSHOTS : "has commits"
    USERS ||--o{ SNAPSHOTS : "authors (authorId)"
    
    CODE_FILES ||--o{ COLLAB_SESSIONS : "is edited in"
    PROJECTS ||--o{ COLLAB_SESSIONS : "hosts session"
    COLLAB_SESSIONS ||--o{ PARTICIPANTS : "has active"
    USERS ||--o{ PARTICIPANTS : "joins as (userId)"
    
    CODE_FILES ||--o{ COMMENTS : "has reviews"
    USERS ||--o{ COMMENTS : "writes (authorId)"
    
    CODE_FILES ||--o{ EXECUTION_JOBS : "is run by"
    USERS ||--o{ EXECUTION_JOBS : "triggers (userId)"
    
    PROJECTS ||--o{ CHAT_MESSAGES : "has messages"
    USERS ||--o{ CHAT_MESSAGES : "sends (senderId)"
    
    USERS ||--o{ NOTIFICATIONS : "receives"
```

---

## 4. Microservices Breakdown

1. **`auth-service`**: Handles user registration, login, JWT generation, and profile management. Stores data in the `users` table.
2. **`project-service`**: Manages the creation, metadata, visibility, and membership of projects. Handles `projects`, `project_members`, and `project_stars` tables.
3. **`file-service`**: Acts as the virtual filesystem for the project. Manages directories, files, and their initial contents in the `code_files` table.
4. **`collab-service`**: The real-time heart of the application. It utilizes WebSockets/Operational Transformation (or CRDTs) to allow multiple users to edit the same file simultaneously. Manages `collab_sessions` and `participants`.
5. **`version-service`**: Operates similarly to a simplified Git backend. It takes snapshots of code files, managing commit history, branching, and tagging in the `snapshots` table.
6. **`execution-service`**: Responsible for taking code strings, compiling them (if necessary), and executing them securely (often inside isolated sandboxes/containers). Logs output in `execution_jobs`.
7. **`comment-service`**: Handles line-specific code review comments, threads, and resolutions. Manages the `comments` table.
8. **`chat-service`**: Provides project-level or session-level real-time text chat using WebSockets. Stores history in `chat_messages`.
9. **`notification-service`**: Centralizes alerts across the system (e.g., "User X invited you to a project", "Build failed"). Stored in `notifications`.
