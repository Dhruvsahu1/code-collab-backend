
# ⚡ CodeSync
 
### **Code Together. Build Faster. Ship Smarter.**
 
A production-grade, **microservices-based online code collaboration platform** enabling developers to write, execute, review, and version-control code in real time — directly from the browser.
 
[Live Demo](#) · [API Docs](#-api-documentation) · [Report a Bug](#) · [Request Feature](#)
 
</div>
---
 
## 📖 Table of Contents
 
- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture](#️-architecture)
- [Tech Stack](#️-tech-stack)
- [Microservices](#-microservices)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Gateway Routing](#-api-gateway-routing)
- [User Roles & Capabilities](#-user-roles--capabilities)
- [System Workflow](#-system-workflow)
- [Non-Functional Requirements](#-non-functional-requirements)
- [Class Diagram Summary](#-class-diagram-summary)
- [Future Roadmap](#-future-roadmap)
- [Contributing](#-contributing)
- [License](#-license)
- [Author](#-author)
---
 
## 🌐 Overview
 
**CodeSync** is a full-stack online code collaboration platform inspired by [Replit](https://replit.com) and [GitHub Codespaces](https://github.com/features/codespaces). It provides a powerful, browser-based environment where development teams can collaborate in real time, execute code securely in sandboxed containers, manage project versions, and conduct code reviews — all without leaving the browser.
 
The platform is designed around a **microservices architecture**, with each domain isolated into its own independently deployable service, backed by its own PostgreSQL database, and communicating via REST, WebSocket (STOMP), and RabbitMQ.
 
---
 
## ✨ Key Features
 
### 🔐 Authentication & Identity
- JWT-based authentication with 24-hour token expiry
- OAuth2 login via **GitHub** and **Google**
- Role-based access control: `GUEST`, `DEVELOPER`, `ADMIN`
- Secure bcrypt password hashing
### 📁 Project & File Management
- Multi-file project system with nested folder trees
- Create, rename, move, delete files and folders
- Fork public projects into personal copies
- Star/bookmark projects for quick access
- Project visibility control: `PUBLIC` / `PRIVATE`
### 🧑‍💻 Live Code Editor
- **Monaco Editor** (VS Code engine) embedded in the browser
- Full syntax highlighting, auto-indent, and bracket matching
- In-project code search across all files
- Powered by React.js with Tailwind CSS
### 🤝 Real-Time Collaboration
- WebSocket (STOMP) powered live co-editing sessions
- Operational Transformation (OT) / CRDT for edit consistency
- Live cursor presence — colour-coded per participant
- Password-protected sessions with participant limits
- Auto-session cleanup after 30 minutes of inactivity
### ⚙️ Sandboxed Code Execution
- Isolated Docker containers per execution job
- Supports **13+ languages**: Python, Java, JavaScript (Node.js), C, C++, Go, Rust, Ruby, TypeScript, PHP, Kotlin, Swift, R
- Async job queue via **RabbitMQ**: `QUEUED → RUNNING → COMPLETED / FAILED / TIMED_OUT / CANCELLED`
- Real-time `stdout` streaming over WebSocket
- Resource limits: **10s CPU**, **256MB RAM**, no network, no persistent writes outside `/tmp`
### 🕓 Version Control
- Git-inspired snapshot system with SHA-256 integrity hashes
- Branch management with a default `main` branch
- Line-by-line diff view using the **Myers diff algorithm**
- Non-destructive restore (creates a new snapshot rather than rewriting history)
- Semantic version tagging (e.g., `v1.0.0`)
### 💬 Code Review
- Inline comments anchored to specific file lines
- Threaded replies via `parentCommentId`
- Resolve / Unresolve workflow for tracking review progress
- `@mention` support triggers targeted notifications
### 🔔 Notifications
- In-app real-time unread badge pushed via WebSocket
- Email notifications for direct session invites and `@mentions`
- Admin broadcast notifications to all or targeted users
- Event types: `SESSION_INVITE`, `COMMENT`, `MENTION`, `SNAPSHOT`, `FORK`
---
 
## 🏗️ Architecture
 
```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React.js)                       │
│          Monaco Editor · Tailwind CSS · SockJS              │
└─────────────────────────┬───────────────────────────────────┘
                          │ HTTP / WebSocket
┌─────────────────────────▼───────────────────────────────────┐
│             API Gateway  (Spring Cloud Gateway :8080)        │
│                    JWT Validation · Routing                  │
└──┬──────┬──────┬──────┬──────┬──────┬──────┬──────┬────────┘
   │      │      │      │      │      │      │      │
 Auth  Project  File  Collab  Exec  Version Comment Notif
 :8081  :8082  :8083  :8084  :8085  :8086  :8087   :8088
 
 Each service owns its own PostgreSQL schema.
 Redis handles WebSocket state, pub/sub, and caching.
 RabbitMQ handles async code execution job dispatch.
```
 
**Communication Patterns**
 
| Pattern | Used For |
|---|---|
| REST (HTTP/JSON) | Synchronous inter-service calls via `RestTemplate` |
| WebSocket (STOMP) | Real-time collaboration, cursor sync, execution streaming |
| RabbitMQ | Async code execution job queue |
| Redis Pub/Sub | Multi-instance WebSocket event broadcast |
 
---
 
## 🛠️ Tech Stack
 
### Backend
| Technology | Purpose |
|---|---|
| Java Spring Boot | Core microservices framework |
| Spring Security + JWT | Authentication & authorisation |
| Spring OAuth2 | GitHub / Google login |
| Spring WebSocket (STOMP) | Real-time collaboration |
| Spring Data JPA | ORM & database access |
| Spring Cloud Gateway | API Gateway & routing |
 
### Frontend
| Technology | Purpose |
|---|---|
| React.js | SPA frontend |
| Tailwind CSS | Utility-first styling |
| Monaco Editor | In-browser VS Code editor engine |
| SockJS + STOMP.js | WebSocket client |
 
### Infrastructure & Data
| Technology | Purpose |
|---|---|
| PostgreSQL | Primary relational store (per-service DB) |
| Redis | Session state, pub/sub, caching |
| RabbitMQ | Async execution job queue |
| Docker & Docker Compose | Containerisation & local orchestration |
| Kubernetes | Production autoscaling (HPA-ready) |
 
### Execution & Tooling
| Technology | Purpose |
|---|---|
| Docker Engine API | Ephemeral sandbox container management |
| `java-diff-utils` (Myers) | Line-by-line snapshot diff computation |
| Swagger / OpenAPI 3.0 | REST API documentation |
| GitHub Actions | CI/CD pipeline |
| AWS SES / SendGrid | Transactional email |
| AWS S3 | Large file attachment storage |
 
---
 
## 🔌 Microservices
 
| Service | Port | Base Package | Responsibility |
|---|---|---|---|
| **API Gateway** | `8080` | — | Route all requests, JWT validation |
| **Auth-Service** | `8081` | `com.codesync.auth` | Registration, login, JWT, OAuth2, profile |
| **Project-Service** | `8082` | `com.codesync.project` | Project CRUD, visibility, fork, star, members |
| **File-Service** | `8083` | `com.codesync.file` | File/folder CRUD, content, file tree, search |
| **Collab-Service** | `8084` | `com.codesync.collab` | Live sessions, participant management, cursors |
| **Execution-Service** | `8085` | `com.codesync.execution` | Sandboxed code runs, job queue, language registry |
| **Version-Service** | `8086` | `com.codesync.version` | Snapshots, diff, branches, restore, tags |
| **Comment-Service** | `8087` | `com.codesync.comment` | Inline comments, threading, resolve workflow |
| **Notification-Service** | `8091` | `com.codesync.notification` | In-app & email alerts, bulk dispatch |
| **Frontend (React)** | `3000` | `com.codesync.web` | UI, MVC controllers, WebSocket handlers |
 
---
 
## 📁 Project Structure
 
```
codesync/
│
├── api-gateway/                    # Spring Cloud Gateway
│   └── src/main/resources/
│       └── application.yml         # Route configuration
│
├── auth-service/                   # JWT, OAuth2, user management
│   └── src/main/java/com/codesync/auth/
│       ├── entity/User.java
│       ├── repository/UserRepository.java
│       ├── service/AuthService.java
│       ├── service/impl/AuthServiceImpl.java
│       └── resource/AuthResource.java
│
├── project-service/                # Project CRUD, fork, star
├── file-service/                   # File tree, content management
├── collab-service/                 # WebSocket sessions, cursors
├── execution-service/              # Docker sandbox, job queue
├── version-service/                # Snapshots, diff, branches
├── comment-service/                # Inline code review
├── notification-service/           # In-app & email alerts
│
├── frontend/                       # React SPA
│   └── src/
│       ├── components/
│       │   ├── Editor/             # Monaco Editor wrapper
│       │   ├── FileTree/           # Project file explorer
│       │   └── Collaboration/      # Session & cursor UI
│       └── pages/
│
├── docker-compose.yml              # Full local environment
└── README.md
```
 
---
 
## 🚀 Getting Started
 
### Prerequisites
 
- [Docker](https://docs.docker.com/get-docker/) & [Docker Compose](https://docs.docker.com/compose/) v2+
- [Java 17+](https://adoptium.net/) (for local development)
- [Node.js 18+](https://nodejs.org/) (for frontend development)
### 1. Clone the Repository
 
```bash
git clone https://github.com/your-username/codesync.git
cd codesync
```
 
### 2. Configure Environment Variables
 
```bash
cp .env.example .env
# Edit .env with your database credentials, JWT secret, OAuth2 keys, etc.
```
 
Key environment variables:
 
 
All services, databases (PostgreSQL), Redis, and RabbitMQ will start automatically.
 
### 4. Access the Application
 
| Service | URL |
|---|---|
| 🌐 Frontend | http://localhost:3000 |
| 🔗 API Gateway | http://localhost:8080 |
| 📚 Swagger UI | http://localhost:8080/swagger-ui.html |
| 🐰 RabbitMQ Dashboard | http://localhost:15672 |
 
### Local Development (Without Docker)
 
```bash
# Start infrastructure only
docker-compose up postgres redis rabbitmq
 
# Run a service locally (example: auth-service)
cd auth-service
./mvnw spring-boot:run
 
# Run the frontend
cd frontend
npm install
npm start
```
 
---
 
## 🔗 API Gateway Routing
 
All client requests pass through the API Gateway on port `8080`:
 
```
/api/auth/**           →  Auth-Service        :8081
/api/projects/**       →  Project-Service     :8082
/api/files/**          →  File-Service        :8083
/api/sessions/**       →  Collab-Service      :8084
/api/executions/**     →  Execution-Service   :8085
/api/versions/**       →  Version-Service     :8086
/api/comments/**       →  Comment-Service     :8087
/api/notifications/**  →  Notification-Service :8091
```
 
WebSocket endpoints are available at:
 
```
ws://localhost:8080/ws/collab       →  Live collaboration (STOMP)
ws://localhost:8080/ws/execution    →  Real-time stdout streaming
ws://localhost:8080/ws/notifications → Unread badge push
```
 
---
 
## 👥 User Roles & Capabilities
 
### 🧑 Guest (Unauthenticated)
- Browse and search public projects by language, name, or owner
- View public project file trees in read-only mode
- View project descriptions, star counts, fork counts, and contributor lists
- Access registration and login pages
### 💻 Developer (Registered User)
- Full project CRUD, multi-file management
- Browser-based live code editor with syntax highlighting
- Start / join real-time collaboration sessions with cursor presence
- Execute code in sandboxed Docker containers with custom stdin
- Create version snapshots, diff any two versions, restore previous states
- Create branches and tag releases (e.g., `v1.0.0`)
- Fork public projects, star bookmarks
- Add inline code review comments with threaded replies
- Receive in-app and email notifications
### 👑 Project Owner
- All Developer capabilities
- Kick participants from active sessions
- Manage project members (invite, remove)
- End collaboration sessions
### 🛡️ Admin
- Suspend, reactivate, or permanently delete user accounts
- Moderate and delete any project
- Terminate any active collaboration session or execution job
- Access platform-wide analytics and audit logs
- Manage supported programming languages and sandbox images
- Broadcast platform notifications
---
 
## 🔄 System Workflow
 
```
1. User authenticates via Auth-Service (JWT or OAuth2)
        ↓
2. Creates a project via Project-Service
        ↓
3. Manages files via File-Service (create/edit/rename/move)
        ↓
4. Opens a collaboration session via Collab-Service
   → WebSocket channel established
   → OT/CRDT synchronises concurrent edits
   → Cursor positions broadcast in real time
        ↓
5. Executes code via Execution-Service
   → Job queued in RabbitMQ
   → Ephemeral Docker container spawned
   → stdout streamed back over WebSocket
        ↓
6. Saves a snapshot via Version-Service
   → SHA-256 hash computed
   → Parent chain updated
        ↓
7. Adds code review comments via Comment-Service
   → @mentions trigger Notification-Service
        ↓
8. Notifications dispatched via Notification-Service
   → In-app badge updated via WebSocket push
   → Email sent for direct mentions and session invites
```
 
---
 
## ⚡ Non-Functional Requirements
 
| Category | Requirement |
|---|---|
| **Performance** | Editor load < 1 second; WebSocket round-trip latency < 50ms |
| **Collaboration Sync** | All cursor & edit deltas broadcast within 100ms to all participants |
| **Sandbox Isolation** | Ephemeral Docker container; no network; max 10s CPU; max 256MB RAM |
| **Security** | bcrypt passwords; JWT expiry 24h; HTTPS enforced; sandboxed execution |
| **Scalability** | Independently scalable services; Redis Pub/Sub for multi-instance WS |
| **Availability** | 99.9% SLA; health-check endpoints per service; graceful disconnect handling |
| **Concurrency** | OT / CRDT guarantees convergent consistency under concurrent writes |
| **Version Integrity** | SHA-256 hash per snapshot; integrity verified on restore |
| **Auto Cleanup** | Idle sessions terminated after 30 min; sandbox containers destroyed within 5s of job completion |
| **Audit Trail** | All member changes, session events, and admin actions logged with actor + timestamp |
 
---
 
## 📐 Class Diagram Summary
 
Each microservice follows a **5-layer architecture**:
 
```
Entity / POJO  →  Repository Interface  →  Service Interface
                                               ↓
                                         ServiceImpl  →  REST Resource
```
 
| Service | Core Entity | Key Operations |
|---|---|---|
| **Auth** | `User` | `register`, `login`, `validateToken`, `OAuth2`, `updateProfile` |
| **Project** | `Project` | `createProject`, `forkProject`, `starProject`, `searchProjects` |
| **File** | `CodeFile` | `createFile`, `updateFileContent`, `getFileTree`, `searchInProject` |
| **Collab** | `CollabSession`, `Participant` | `createSession`, `joinSession`, `updateCursor`, `broadcastChange` |
| **Execution** | `ExecutionJob` | `submitExecution`, `cancelExecution`, `getExecutionResult` |
| **Version** | `Snapshot` | `createSnapshot`, `diffSnapshots`, `restoreSnapshot`, `createBranch` |
| **Comment** | `Comment` | `addComment`, `getReplies`, `resolveComment`, `getByLine` |
| **Notification** | `Notification` | `send`, `sendBulk`, `markAllRead`, `getUnreadCount` |
 
---
 
## 🗺️ Future Roadmap
 
| Feature | Description |
|---|---|
| 🤖 **AI Code Suggestions** | Inline AI-powered completions and refactoring hints |
| 🔗 **GitHub Integration** | Push/pull directly from GitHub repositories |
| 🚀 **CI/CD Pipelines** | Trigger automated build and test workflows from within CodeSync |
| 🎙️ **Voice / Video Collaboration** | WebRTC-based audio and video for pair programming sessions |
| 📦 **Package Manager Support** | Install and manage project dependencies from within the editor |
| 🌍 **Internationalisation (i18n)** | Multi-language UI support |
 
---
 
## 🤝 Contributing
 
Contributions are welcome! Please follow these steps:
 
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m "feat: add your feature"`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request
Please ensure all services pass their unit and integration tests before submitting.
 
```bash
# Run tests for a service
cd auth-service
./mvnw test
```
 
---
 
## 📚 API Documentation
 
Swagger UI is available at:
 
```
http://localhost:8080/swagger-ui.html
```
 
All REST endpoints across all services are documented under the **OpenAPI 3.0** specification, versioned at `/api/v1/`.
 
---
 
## 📄 License
 
This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.
 
---
 
## 👨‍💻 Author
 
**Dhruv Sahu**
 
> *"Code Together. Build Faster. Ship Smarter."*
 
---
 
<div align="center">
⭐ If you found this project useful, consider giving it a star — it helps others discover it!
 
</div>
