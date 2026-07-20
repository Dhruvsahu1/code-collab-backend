# Auth Service Folder Structure Explained

The `auth-service` microservice follows a standard Spring Boot and MVC (Model-View-Controller) architectural pattern. By separating code into specific folders (packages), the application remains highly organized, maintainable, and scalable.

Here is a detailed breakdown of every folder inside `src/main/java/com/codesync/auth` and the root `src` directory, using files from your project as examples:

---

## 1. `config`
**Purpose:** Stores configuration classes that set up beans, third-party libraries, or global settings when the application starts.
**What's inside:**
- `SecurityConfig.java`: Configures Spring Security rules (e.g., which endpoints require authentication and which are public).
- `GlobalExceptionHandler.java`: A centralized place to catch errors thrown anywhere in the app and format them into clean HTTP JSON responses for the frontend.

## 2. `controller`
**Purpose:** The entry point for HTTP requests. Controllers listen for web traffic (like `GET` or `POST` requests), read the incoming data, and pass it to the `service` layer.
**What's inside:**
- `AuthController.java`: Contains endpoints like `/api/auth/login` and `/api/auth/register`.
- `OAuthController.java`: Handles third-party login flows (e.g., Google or GitHub login).
*Note: Controllers should never contain complex business logic; they are just traffic directors.*

## 3. `dto` (Data Transfer Object)
**Purpose:** Defines the exact shape of JSON data sent to or received from the frontend. DTOs are temporary containers used strictly for network communication.
**What's inside:**
- `LoginRequest.java`: Contains only the `email` and `password` fields that a user submits when logging in.
- `AuthResponse.java`: Contains the JWT token and basic user info sent back upon successful login.
*Note: We use DTOs instead of Database Entities to ensure we don't accidentally leak sensitive database fields (like a password hash) to the frontend.*

## 4. `entity` (or `model`)
**Purpose:** Contains Java classes that map directly to your PostgreSQL database tables. 
**What's inside:**
- `User.java`: Represents the `users` table. Fields in this class (like `userId`, `username`, `passwordHash`) directly correspond to database columns. It uses `@Entity` and `@Table` annotations.

## 5. `enums`
**Purpose:** Stores Enumerations, which are predefined lists of constant values. This ensures data consistency and prevents typos.
**What's inside:**
- `Role.java`: Might contain `DEVELOPER` and `ADMIN`.
- `AuthProvider.java`: Might contain `LOCAL`, `GOOGLE`, `GITHUB`.

## 6. `exception`
**Purpose:** Houses custom exception classes tailored to the application's specific business rules.
**What's inside:**
- `UserNotFoundException.java`
- `InvalidCredentialsException.java`
- `TokenExpiredException.java`
*When the service layer encounters a problem, it throws one of these exceptions, which the `GlobalExceptionHandler` (in `config`) catches and translates into a `404` or `401` HTTP error.*

## 7. `repository`
**Purpose:** The Data Access Layer. Interfaces here extend Spring Data JPA (`JpaRepository`), providing out-of-the-box methods to interact with the database without writing raw SQL.
**What's inside:**
- `UserRepository.java`: Allows the service layer to call methods like `userRepository.findByEmail(email)` to fetch a user from the database.

## 8. `security`
**Purpose:** Contains specialized security logic, primarily related to JSON Web Tokens (JWT) and user authentication context.
**What's inside:**
- `JwtUtil.java`: Functions to generate, sign, and validate JWT tokens.
- `JwtAuthenticationFilter.java`: A filter that intercepts every incoming request to check if a valid JWT token is present in the headers.
- `UserDetailsServiceImpl.java`: Loads user data from the database during the Spring Security login process.

## 9. `service`
**Purpose:** The "Brain" of the application. This layer holds the core business logic. It receives commands from the `controller`, enforces rules, processes data, and talks to the `repository`.
**What's inside:**
- `AuthService.java` (Interface) and `AuthServiceImpl.java` (Implementation).
- *Example Flow:* The controller tells the service to "register a user". The service will first check the repository if the email already exists. If it does, it throws an exception. If not, it hashes the password, creates a new `User` entity, and saves it via the repository.

---

## 10. `resources` (Under `src/main/resources`)
**Purpose:** Stores non-Java configuration files and static assets.
**What's inside:**
- `application.yml` (and `application-prod.yml`): The main configuration file. It holds environment variables like the database connection URL, server port (`8081`), and the JWT secret key.

## 11. `test` (Under `src/test`)
**Purpose:** Contains all the automated code tests to ensure the application works correctly before deploying.
**What's inside:**
- Contains a mirrored package structure (e.g., `src/test/java/com/codesync/auth`) holding Unit Tests (testing a single class in isolation) and Integration Tests (testing how the whole system works together). Example: `AuthServiceApplicationTests.java`.
