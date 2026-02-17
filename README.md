# Code Operator Management System

A comprehensive code operator management system with a complete lifecycle for managing operators, operator packages, version control, execution, and marketplace functionality.

## Project Status

### ✅ Completed (Phase 1: Foundation)

#### Backend Implementation

**Module Structure:**
- ✅ `operator-api` - API layer with Spring Boot application
- ✅ `operator-core` - Core domain models and repositories
- ✅ `operator-service` - Business logic services
- ✅ `operator-infrastructure` - Infrastructure services (storage, git, etc.)
- ✅ `operator-common` - Shared utilities and security components

**Domain Entities Implemented:**
1. ✅ User & UserPrincipal (authentication)
2. ✅ Operator & Category & Parameter (operator management)
3. ✅ OperatorPackage & PackageOperator (package management)
4. ✅ Version & PackageVersion (version control)
5. ✅ Task & TaskLog & TaskArtifact (execution)
6. ✅ MarketItem & Rating & Review (marketplace)
7. ✅ PublishDestination & PublishHistory (publishing)
8. ✅ OperatorPermission (permissions)
9. ✅ AuditLog (auditing)

**JPA Repositories Created:**
- ✅ UserRepository, CategoryRepository, OperatorRepository, ParameterRepository
- ✅ OperatorPackageRepository, PackageOperatorRepository
- ✅ VersionRepository, PackageVersionRepository
- ✅ MarketItemRepository, RatingRepository, ReviewRepository
- ✅ TaskRepository, TaskLogRepository, TaskArtifactRepository
- ✅ AuditLogRepository

**Security & Authentication:**
- ✅ JWT Token Provider
- ✅ UserPrincipal (Spring Security UserDetails)
- ✅ JWT Authentication Filter
- ✅ JWT Authentication Entry Point
- ✅ Spring Security Configuration
- ✅ Custom User Details Service
- ✅ Authentication Service (login, register, refresh token, change password)
- ✅ Global Exception Handler

**API Controllers:**
- ✅ AuthController (login, register, token refresh, user info, change password, logout)

**DTOs:**
- ✅ LoginRequest, RegisterRequest, AuthResponse
- ✅ ChangePasswordRequest, RefreshTokenRequest, UserInfo

**Configuration:**
- ✅ Application configuration (application.yml, application-dev.yml, application-prod.yml)
- ✅ Logback logging configuration
- ✅ OpenAPI/Swagger configuration
- ✅ Database and Redis configuration
- ✅ MinIO configuration

**Utilities:**
- ✅ ApiResponse (standard API response wrapper)
- ✅ PageResponse (pagination wrapper)
- ✅ Custom exceptions (ResourceNotFoundException, UnauthorizedException, BadRequestException)

### 🚧 In Progress

- ⏳ API Layer - Additional controllers (Operators, Packages, Execution, Market, etc.)

### 📋 Remaining Tasks

#### Backend Tasks

**API Controllers (Task #4):**
- ⏳ OperatorController - CRUD operations for operators
- ⏳ OperatorPackageController - Manage operator packages
- ⏳ CategoryController - Category management
- ⏳ ExecutionController - Task execution management
- ⏳ VersionController - Version management
- ⏳ MarketController - Marketplace operations
- ⏳ PublishController - Publishing operations
- ⏳ UserController - User profile management

**Application Services (Task #5):**
- ⏳ OperatorService - Operator business logic
- ⏳ PackageService - Package business logic
- ⏳ CategoryService - Category business logic
- ⏳ TaskService - Task management
- ⏳ VersionService - Version management
- ⏳ MarketService - Marketplace business logic
- ⏳ PublishService - Publishing business logic

**Infrastructure Layer (Task #6):**
- ⏳ MinIO Storage Service - File storage
- ⏳ Redis Cache Configuration - Caching layer
- ⏳ Git Integration Service - JGit integration
- ⏳ Docker Sandbox Executor - Containerized execution
- ⏳ Java Executor - Java code execution
- ⏳ Groovy Executor - Groovy script execution
- ⏳ Task Scheduler - Redis-based task queue

#### Frontend Tasks

**Foundation (Tasks #8-9):**
- ⏳ Initialize React + TypeScript + Vite project
- ⏳ Configure Ant Design UI library
- ⏳ Set up React Router
- ⏳ Set up Zustand state management
- ⏳ Create layout components (Header, Sidebar, Content)
- ⏳ Implement authentication pages (login, register)
- ⏳ API request utilities with Axios

**UI Pages (Tasks #10-13):**
- ⏳ Dashboard/Statistics pages
- ⏳ Operator management pages (list, detail, create, edit, code editor)
- ⏳ Operator package management pages
- ⏳ Execution/task management pages (with real-time logs)
- ⏳ Version management pages
- ⏳ Marketplace pages (search, detail, ratings, reviews)
- ⏳ User settings pages

#### Deployment (Task #14)

- ⏳ Dockerfile for Spring Boot app
- ⏳ Docker Compose configuration (PostgreSQL, Redis, MinIO)
- ⏳ Nginx reverse proxy configuration
- ⏳ Database migration scripts

## Technology Stack

### Backend
- **Framework**: Spring Boot 3.2.x
- **Language**: Java 21 LTS
- **Database**: PostgreSQL 15+
- **Cache**: Redis 7.x
- **File Storage**: MinIO (S3-compatible)
- **Authentication**: JWT + Spring Security
- **Scripting**: Groovy 4.x (for dynamic operators)
- **API Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven 3.9.x

### Frontend (Planned)
- **Framework**: React 18 + TypeScript 5.x
- **Build Tool**: Vite 5.x
- **UI Library**: Ant Design 5.x
- **State Management**: Zustand
- **Routing**: React Router 6.x
- **HTTP Client**: Axios
- **Code Editor**: Monaco Editor
- **Charts**: ECharts

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                     Frontend (React)                     │
│  Ant Design + Zustand + React Router + Monaco Editor    │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓ REST API
┌─────────────────────────────────────────────────────────┐
│                   API Layer (Spring Boot)                │
│  Controllers + Security + Exception Handlers + OpenAPI  │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓
┌─────────────────────────────────────────────────────────┐
│                 Service Layer                            │
│  Business Logic + Transaction Management + DTO Mapping  │
└─────────────────────────────────────────────────────────┘
                            │
                            ↓
┌────────────────┬────────────────────┬───────────────────┐
│  Core Layer    │  Infrastructure    │  Common Layer     │
│  (Entities +   │  (Storage, Git,    │  (Security +      │
│   Repositories)│   Executors)       │   Utils)          │
└────────────────┴────────────────────┴───────────────────┘
                            │
                            ↓
┌────────────────┬────────────────────┬───────────────────┐
│  PostgreSQL    │  Redis             │  MinIO            │
│  (Primary DB)  │  (Cache + Queue)   │  (File Storage)   │
└────────────────┴────────────────────┴───────────────────┘
```

## Quick Start

### Prerequisites
- JDK 21+
- Maven 3.9+
- PostgreSQL 15+
- Redis 7.x
- MinIO (optional, for file storage)

### Database Setup

Create the database:
```sql
CREATE DATABASE operator_manager;
CREATE USER operator_user WITH PASSWORD 'operator_pass';
GRANT ALL PRIVILEGES ON DATABASE operator_manager TO operator_user;
```

### Configuration

Edit `operator-api/src/main/resources/application-dev.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/operator_manager_dev
    username: dev_user
    password: dev_pass
  data:
    redis:
      host: localhost
      port: 6379

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
```

### Build & Run

```bash
# Build project
mvn clean install

# Run application
cd operator-api
mvn spring-boot:run

# Or run JAR
java -jar operator-api/target/operator-api-1.0.0-SNAPSHOT.jar
```

### Access

- **API Base URL**: http://localhost:8080/api
- **Swagger UI**: http://localhost:8080/api/swagger-ui.html
- **Health Check**: http://localhost:8080/api/actuator/health

## API Documentation

### Authentication Endpoints

**POST /api/v1/auth/login**
```json
{
  "username": "admin",
  "password": "password123"
}
```

**POST /api/v1/auth/register**
```json
{
  "username": "newuser",
  "email": "user@example.com",
  "password": "password123",
  "fullName": "New User"
}
```

**POST /api/v1/auth/refresh**
```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**GET /api/v1/auth/me**
- Returns current authenticated user info

**POST /api/v1/auth/change-password**
```json
{
  "oldPassword": "oldpass",
  "newPassword": "newpass"
}
```

## Project Structure

```
operator-manager/
├── operator-api/              # API layer (Spring Boot)
│   └── src/main/java/com/operator/api/
│       ├── controller/        # REST controllers
│       ├── config/           # Configuration classes
│       └── handler/          # Exception handlers
├── operator-core/             # Core domain layer
│   └── src/main/java/com/operator/core/
│       ├── operator/         # Operator entities & repos
│       ├── package/          # Package entities & repos
│       ├── version/          # Version entities & repos
│       ├── market/           # Marketplace entities & repos
│       ├── execution/        # Task/Execution entities & repos
│       ├── publish/          # Publishing entities & repos
│       ├── security/         # User entity & repo
│       └── audit/            # Audit log entity & repo
├── operator-service/          # Business logic layer
│   └── src/main/java/com/operator/service/
│       ├── operator/
│       ├── package/
│       ├── version/
│       ├── market/
│       ├── execution/
│       ├── publish/
│       └── security/         # AuthService
├── operator-infrastructure/   # Infrastructure layer
│   └── src/main/java/com/operator/infrastructure/
│       ├── storage/          # MinIO storage service
│       ├── git/              # Git integration
│       ├── sandbox/          # Docker sandbox
│       └── scheduler/        # Task scheduler
├── operator-common/           # Common utilities
│   └── src/main/java/com/operator/common/
│       ├── security/         # JWT, UserPrincipal, Filters
│       ├── exception/        # Custom exceptions
│       └── utils/            # ApiResponse, etc.
└── pom.xml                    # Parent POM
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_URL` | Database URL | jdbc:postgresql://localhost:5432/operator_manager |
| `DB_USERNAME` | Database username | operator_user |
| `DB_PASSWORD` | Database password | - |
| `REDIS_HOST` | Redis host | localhost |
| `REDIS_PORT` | Redis port | 6379 |
| `JWT_SECRET` | JWT secret key | - |
| `JWT_EXPIRATION` | JWT token expiration (ms) | 86400000 |
| `MINIO_ENDPOINT` | MinIO endpoint | http://localhost:9000 |
| `MINIO_ACCESS_KEY` | MinIO access key | minioadmin |
| `MINIO_SECRET_KEY` | MinIO secret key | minioadmin |

## Development Guide

### Adding a New Entity

1. Create entity class in `operator-core/src/main/java/com/operator/core/{domain}/`
2. Create repository interface in `operator-core/src/main/java/com/operator/core/{domain}/repository/`
3. Create DTO classes in `operator-api/src/main/java/com/operator/api/controller/dto/`
4. Create service in `operator-service/src/main/java/com/operator/service/`
5. Create controller in `operator-api/src/main/java/com/operator/api/controller/`

### Adding a New API Endpoint

1. Define DTOs (request/response)
2. Implement business logic in service layer
3. Create controller method with proper annotations
4. Add OpenAPI documentation
5. Write unit tests

## License

Apache License 2.0

## Team

Operator Manager Development Team

## Version

1.0.0-SNAPSHOT
