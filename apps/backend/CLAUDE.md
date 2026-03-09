# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot 4.0 application written in Kotlin for managing social gatherings. The project uses PostgreSQL for persistence, Redis for distributed locking, and follows a layered architecture pattern.

**Package naming**: The base package is `beyondeyesight._0` (note the underscore) - the original package name '30damm' was invalid so it was changed to '_0'.

## Development Commands

### Build and Run
```bash
# Build the project (includes OpenAPI generation)
./gradlew build

# Build without running tests
./gradlew build -x test

# Run the application
./gradlew bootRun

# Run tests
./gradlew test

# Run a specific test
./gradlew test --tests "ClassName.testMethodName"

# Clean build artifacts
./gradlew clean
```

### OpenAPI Code Generation
The project uses OpenAPI Generator to generate API interfaces and models from a shared specification:

```bash
# Bundle OpenAPI spec from source YAML files
./gradlew bundleOpenApi

# Generate API code (automatically runs bundleOpenApi first)
./gradlew openApiGenerate
```

**Important**: Generated code is placed in `build/generated/src/main/kotlin` with packages:
- API interfaces: `beyondeyesight.api`
- DTOs/Models: `beyondeyesight.model`

Controllers implement these generated interfaces (e.g., `GatheringsApiImpl` implements `GatheringsApi`).

### Database Setup
The application requires PostgreSQL and Redis running locally:
- **Development PostgreSQL**: `localhost:5432/30damm` (username: `root`, password: `beyondeyesight`)
- **Test PostgreSQL**: `localhost:15432/testdb` (username: `test`, password: `test`)
- **Redis**: `localhost:6379`

Database schema is managed via Flyway migrations in `src/main/resources/db/migration/` with naming convention `V1.XX__description.sql`.

### Docker Database Management
```bash
# Development database
./gradlew postgresUp        # Start PostgreSQL container
./gradlew postgresDown      # Stop PostgreSQL container
./gradlew postgresRemove    # Remove PostgreSQL container
./gradlew postgresStatus    # Check PostgreSQL status

# Test database
./gradlew testPostgresUp    # Start test PostgreSQL container
./gradlew testPostgresDown  # Stop test PostgreSQL container
./gradlew testPostgresStatus # Check test PostgreSQL status
```

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

**SeriesEntity**: Represents recurring gathering series that share common properties (capacity, fees, category, etc.). Individual gatherings can be scheduled based on series templates.

**SeriesScheduleEntity**: Defines schedules for series using `ScheduleType` (WEEKLY or DATE). Includes validation logic in `@PostLoad` to ensure data integrity based on schedule type. Uses a sequence-based ID (`seq`) instead of UUID.

**ParticipantEntity**: Represents user participation in gatherings with various states and approval workflows.

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
- Controllers should implement generated OpenAPI interfaces (e.g., `GatheringsApiImpl : GatheringsApi`)
- Entity validation logic can be placed in `init` blocks or `@PostLoad` methods for data integrity checks

## Testing

Integration tests extend `EndToEndTestBase` which provides:
- `WebTestClient` for HTTP request testing
- `JdbcTemplate` for database access
- Automatic database cleanup after each test (truncates all tables except `flyway_schema_history`)
- `ObjectMapper` configured for Jackson serialization

Tests run against a random port (`@SpringBootTest(webEnvironment = RANDOM_PORT)`) and use the test database configuration.