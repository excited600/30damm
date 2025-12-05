# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0 application written in Kotlin for managing social gatherings. The project uses PostgreSQL for persistence, Redis for distributed locking, and follows a layered architecture pattern.

**Package naming**: The base package is `beyondeyesight._0` (note the underscore) - the original package name '3040' was invalid so it was changed to '_0'.

## Development Commands

### Build and Run
```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test
./gradlew test --tests "ClassName.testMethodName"

# Clean build artifacts
./gradlew clean
```

### Database Setup
The application requires PostgreSQL and Redis running locally:
- PostgreSQL: `localhost:5432/3040` (username: `root`, password: `beyondeyesight`)
- Redis: `localhost:6379`

Database schema is managed via Flyway migrations in `src/main/resources/db/migration/`.

## Architecture

### Layered Architecture
The codebase follows a strict layered architecture with clear separation of concerns:

```
ui (controllers)
    ↓
application (application services - transaction boundaries)
    ↓
domain (domain services, entities, repositories)
    ↓
infra (repository adapters, external services)
```

**Key principles:**
- **UI Layer** (`ui/`): REST controllers that handle HTTP requests/responses. Contains request/response DTOs as inner classes.
- **Application Layer** (`application/`): Application services that define transaction boundaries using `@Transactional`. These orchestrate domain services and use mapper functions to convert entities to DTOs.
- **Domain Layer** (`domain/`): Contains business logic, entities, and repository interfaces. Domain entities have factory methods (e.g., `UserEntity.signUp()`, `GatheringEntity.open()`).
- **Infrastructure Layer** (`infra/`): JPA repository adapters that implement domain repository interfaces, and external service implementations like `RedisLockService`.

### Key Design Patterns

**Repository Pattern**: Domain defines repository interfaces (`UserRepository`, `GatheringRepository`), while infrastructure provides JPA implementations (`UserJpaRepositoryAdapter`, `GatheringJpaRepositoryAdapter`).

**Mapper Functions**: Application services accept mapper functions as parameters to convert domain entities to DTOs, keeping the domain layer free of presentation concerns:
```kotlin
userApplicationService.signUp(..., mapper = { userEntity -> SignUpResponse.from(userEntity) })
```

**Factory Methods**: Entities use companion object factory methods for creation (e.g., `UserEntity.signUp()`, `GatheringEntity.open()`), ensuring valid entity construction.

**Distributed Locking**: `LockService` interface in domain layer is implemented by `RedisLockService` for handling concurrent gathering registrations. Supports both immediate locking (`tryLock`) and retry-based locking (`lockWithRetry`).

### Domain Models

**UserEntity**: Represents users with fields like email, nickname, age, gender, introduction, password, phoneNumber, hearts, and provider (THIRTY_FORTY or KAKAO).

**GatheringEntity**: Represents social gatherings with complex rules around capacity, gender ratios, age restrictions, application types (FIRST_IN, APPROVAL), and status (OPEN, CLOSED, CANCELLED).

**BaseEntity**: Parent entity providing UUID-based identity and audit fields (createdAt, updatedAt).

### Database Management

- **Flyway**: Migrations are in `src/main/resources/db/migration/` following the naming convention `V1.XX__description.sql`
- **JPA**: Hibernate DDL is disabled (`ddl-auto: none`) - all schema changes must be done via Flyway migrations
- SQL logging is enabled (`show-sql: true`)

### Configuration

- **Security**: Currently permissive (all endpoints permit all), configured in `SecurityConfig.kt`. BCrypt password encoder is configured.
- **Redis**: Used for distributed locking via `RedisLockService` with Lua scripts for atomic unlock operations.

## Code Conventions

- Use Kotlin data classes for DTOs where appropriate
- Place request/response DTOs as inner classes within controllers
- Domain entities should have factory methods in companion objects
- Use `@Transactional` only at the application service layer
- Repository adapters in `infra/` should be thin wrappers around Spring Data JPA repositories