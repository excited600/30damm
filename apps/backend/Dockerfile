FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace

# Copy Gradle wrapper and build files
COPY apps/backend/gradlew apps/backend/gradlew
COPY apps/backend/gradle apps/backend/gradle
COPY apps/backend/build.gradle.kts apps/backend/build.gradle.kts
COPY apps/backend/settings.gradle.kts apps/backend/settings.gradle.kts

# Copy OpenAPI spec (needed for code generation)
COPY packages/api-spec/openapi packages/api-spec/openapi

# Copy source code
COPY apps/backend/src apps/backend/src

# Install Node.js (needed for OpenAPI bundling with npx)
RUN apt-get update && apt-get install -y nodejs npm && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace/apps/backend

# Build the application
RUN chmod +x gradlew && ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /workspace/apps/backend/build/libs/*.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
