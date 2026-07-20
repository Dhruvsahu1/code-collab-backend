# Code Collab System Diagrams

## 1. Architecture Diagram
```mermaid
graph TD
    Client[Frontend Apps] -->|HTTP/WS| Gateway[API Gateway :8080]
    Gateway --> Auth[auth-service]
    Gateway --> Project[project-service]
    Gateway --> File[file-service]
    Gateway --> Collab[collab-service]
    Gateway --> Exec[execution-service]
    Gateway --> Version[version-service]
    Gateway --> Comment[comment-service]
    Gateway --> Chat[chat-service]
    Gateway --> Notify[notification-service]
    Auth & Project & File & Collab & Exec & Version & Comment & Chat & Notify --> DB[(PostgreSQL)]
```

## 2. Flowchart / Process Diagram (Project Creation)
```mermaid
flowchart TD
    A[User clicks 'New Project'] --> B[Frontend sends POST /projects]
    B --> C{Gateway Routes to project-service}
    C --> D[Controller intercepts]
    D --> E[Service validates & saves]
    E --> F[(Database saves Project)]
    F --> G[Service returns Project ID]
    G --> H[Frontend redirects to IDE]
```

## 3. Data Flow Diagram (DFD - Level 0)
```mermaid
flowchart LR
    User((User)) -- Code Edits --> System[Code Collab System]
    System -- Output/Results --> User
    System -- Reads/Writes --> DB[(Database)]
    System -- External API --> Compiler[Execution Sandbox]
```

## 4. Entity-Relationship Diagram (ERD)
```mermaid
erDiagram
    USER ||--o{ PROJECT : owns
    USER ||--o{ PROJECT_MEMBER : is
    PROJECT ||--o{ PROJECT_MEMBER : has
    PROJECT ||--o{ CODE_FILE : contains
    CODE_FILE ||--o{ SNAPSHOT : versions
    CODE_FILE ||--o{ COLLAB_SESSION : edited_in
    COLLAB_SESSION ||--o{ PARTICIPANT : hosts
```

## 5. Sequence Diagram (WebSocket Collaboration)
```mermaid
sequenceDiagram
    actor Dev1
    participant Gateway
    participant CollabService
    actor Dev2
    Dev1->>Gateway: Send Code Edit (WS)
    Gateway->>CollabService: Route Event
    CollabService->>CollabService: Apply Operational Transform
    CollabService->>Gateway: Broadcast Edit
    Gateway->>Dev2: Push Code Edit (WS)
```

## 6. Deployment Diagram
```mermaid
graph TD
    subgraph Docker Host
        subgraph CodeCollab-Network
            GW[api-gateway container]
            MS[9 Microservice containers]
            DB[(PostgreSQL container)]
        end
    end
    Internet((Internet)) -->|Port 8080| GW
    GW --> MS
    MS --> DB
```

## 7. Use Case Diagram
```mermaid
flowchart LR
    Dev([Developer]) --> Create[Create/Fork Project]
    Dev --> Edit[Live Edit Code]
    Dev --> Run[Execute Code]
    Dev --> Chat[Chat/Comment]
    Admin([Admin]) --> Manage[Manage Users/System]
```

## 8. Wireframes / UI Mockups (Textual)
**IDE Dashboard:**
```text
[ Navbar: Logo | Search | Profile Dropdown ]
------------------------------------------------
[ Sidebar ] | [ Main Editor Window            ]
- Files   |   1: function hello() {           
- main.js |   2:   console.log("hi");         
- utils.js|   3: }                            
          |-----------------------------------
[ Terminal]| [ Output Terminal / Chat Box    ]
```
