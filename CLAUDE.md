# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **Code Operator Management System** - a full-stack platform for managing code operators (reusable code components) with complete lifecycle management including creation, packaging, versioning, execution, and marketplace functionality.

## Development Commands

### Quick Start

```bash
# Start everything (macOS only - launches both frontend and backend)
./start-all.sh

# Start only backend (requires Docker services running)
./start-backend.sh

# Start only frontend
./start-frontend.sh

# Start Docker services (PostgreSQL, Redis, MinIO)
docker-compose up -d
```

### Backend Commands

```bash
# Build all modules
mvn clean install

# Run backend (from operator-api directory)
cd operator-api
mvn spring-boot:run

# Or run JAR directly
java -jar operator-api/target/operator-api-1.0.0-SNAPSHOT.jar

# Build without tests
mvn clean install -DskipTests
```

### Frontend Commands

```bash
cd operator-manager-web

# Install dependencies
npm install

# Development server with hot reload
npm run dev

# Build for production
npm run build

# Linting
npm run lint
npm run lint:fix
```

### Test Commands

```bash
cd tests

# Run all test suites
./99-run-all.sh

# Run individual test suites
./01-prepare-data.sh      # Prepare test data
./02-auth-test.sh         # Authentication tests
./03-operator-crud-test.sh # Operator CRUD tests
```

## Architecture

### Backend Module Structure

The backend follows **Clean Architecture** with 5 Maven modules:

```
operator-manager/
├── operator-api/              # API Layer - REST controllers, handlers, security config
├── operator-core/             # Domain Layer - JPA entities and repositories
├── operator-service/          # Business Logic Layer - Service implementations
├── operator-infrastructure/   # Infrastructure Layer - MinIO, Git, Docker, Redis
└── operator-common/           # Shared Utilities - JWT, exceptions, API wrappers
```

**Data Flow:**
```
Frontend (React) → REST API → Controllers → Services → Repositories → PostgreSQL
                                         ↓
                                    Redis (Cache + Queue)
                                         ↓
                                    MinIO (File Storage)
```

### Key Domain Entities

All entities are in `operator-core/src/main/java/com/operator/core/`:

- **Security**: User, UserPrincipal
- **Operators**: Operator, Category, Parameter
- **Packages**: OperatorPackage, PackageOperator
- **Versions**: Version, PackageVersion
- **Execution**: Task, TaskLog, TaskArtifact
- **Marketplace**: MarketItem, Rating, Review
- **Publishing**: PublishDestination, PublishHistory
- **Audit**: AuditLog

### API Controllers (All Complete)

All 8 controllers in `operator-api/src/main/java/com/operator/api/controller/`:

1. **AuthController** - `/api/v1/auth/*` (login, register, token refresh, change password)
2. **OperatorController** - `/api/v1/operators/*` (CRUD, search, parameters, status)
3. **PackageController** - `/api/v1/packages/*` (CRUD, operator management, reordering)
4. **CategoryController** - `/api/v1/categories/*` (tree management, operators by category)
5. **ExecutionController** - `/api/v1/execution/*` (tasks, logs, cancellation, statistics)
6. **VersionController** - `/api/v1/versions/*` (version management, release, comparison)
7. **MarketController** - `/api/v1/market/*` (search, ratings, reviews, publish/unpublish)
8. **UserController** - `/api/v1/users/*` (profile management, role/status updates)

### Infrastructure Services

All infrastructure components are in `operator-infrastructure/src/main/java/com/operator/infrastructure/`:

- **MinIO Storage Service** (`storage/MinioStorageService.java`) - File upload/download with presigned URLs
- **Redis Cache Service** (`RedisCacheService.java`) - String, Hash, List, Set, ZSet operations with TTL
- **Git Integration Service** (`git/GitIntegrationService.java`) - Repo operations, commits, tags, changelog
- **Task Scheduler** (`scheduler/TaskScheduler.java`) - Priority polling, timeout detection
- **Redis Queue Service** (`scheduler/RedisQueueService.java`) - Priority queue using Sorted Set
- **Task Executor Service** (`scheduler/TaskExecutorService.java`) - Execution orchestration and cleanup
- **WebSocket Handler** (`scheduler/TaskLogWebSocketHandler.java`) - Real-time logs and progress
- **Publish Services** (`publisher/`) - REST and file-based publishing

### Frontend Structure

```
operator-manager-web/src/
├── components/      # Reusable UI components
├── pages/          # Route pages
├── api/            # API client functions
├── stores/         # Zustand state stores
├── hooks/          # Custom React hooks
├── utils/          # Utility functions
├── types/          # TypeScript type definitions
└── App.tsx         # Root component
```

## Configuration

### Backend Configuration

Main config: `operator-api/src/main/resources/application.yml`

Environment-specific configs:
- `application-dev.yml` - Development
- `application-prod.yml` - Production

Key configuration sections:
- `spring.datasource` - PostgreSQL connection
- `spring.data.redis` - Redis connection
- `minio` - MinIO storage settings
- `jwt` - JWT token configuration

### Database

Initialize database:
```bash
createdb operator_manager_dev
psql -d operator_manager_dev -f db/migration/V1__init_schema.sql
```

Default admin credentials:
- Username: `admin`
- Password: `admin123`

## Service URLs

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5432 |
| Redis | localhost:6379 |
| MinIO Console | http://localhost:9001 |

## Technology Stack

**Backend:**
- Spring Boot 3.2.x
- Java 21 LTS
- PostgreSQL 15+
- Redis 7.x
- MinIO (S3-compatible storage)
- JWT + Spring Security
- Groovy 4.x (for dynamic operators)
- JGit (Git integration)
- SpringDoc OpenAPI

**Frontend:**
- React 18 + TypeScript 5.x
- Vite 5.x (build tool)
- Ant Design 5.x (UI library)
- Zustand (state management)
- React Router 6.x
- Axios (HTTP client)
- Monaco Editor (code editor)
- ECharts (charts)

## Important Notes

### JWT Authentication

All API endpoints except `/api/v1/auth/*` require a JWT token in the `Authorization` header:
```
Authorization: Bearer <jwt_token>
```

### Adding a New Feature

1. **Backend** - Follow the established pattern:
   - Create entity in `operator-core` (domain package)
   - Create repository interface in `operator-core`
   - Create DTOs in `operator-api/src/main/java/com/operator/api/controller/dto/`
   - Create service in `operator-service`
   - Create controller in `operator-api`

2. **Frontend** - Organize by feature:
   - Create API client function in `api/`
   - Create Zustand store if state needed
   - Create page components in `pages/`
   - Add route to `App.tsx`

### Testing

The test framework uses shell scripts with curl for API testing:
- Test utilities are in `tests/utils/` (logger.sh, assertions.sh)
- API base URL: `http://localhost:8080/api/v1`
- Test scripts must be executable: `chmod +x <script>.sh`

### Error Handling

All API responses use `ApiResponse<T>` wrapper with:
- `success`: boolean
- `message`: string
- `data`: T | null
- `error`: string | null

Global exception handler is in `operator-api/src/main/java/com/operator/api/handler/GlobalExceptionHandler.java`
