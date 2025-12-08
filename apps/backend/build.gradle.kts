plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.openapi.generator") version "7.12.0"
}

group = "beyondeyesight"
version = "0.0.1-SNAPSHOT"
description = "3040 project"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.postgresql:postgresql")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.2")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

    // Kotlin JDSL 코어
    implementation("com.linecorp.kotlin-jdsl:jpql-dsl:3.5.2")
    implementation("com.linecorp.kotlin-jdsl:jpql-render:3.5.2")

    // Spring Data JPA 연동
    implementation("com.linecorp.kotlin-jdsl:spring-data-jpa-support:3.5.2")

    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    inputSpec.set("${rootDir}/../../packages/api-spec/openapi/3040-api-spec.yml")
    outputDir.set("${buildDir}/generated")
    apiPackage.set("beyondeyesight.api")
    modelPackage.set("beyondeyesight.model")
    typeMappings.set(mapOf(
        "date-time" to "LocalDateTime",
        "DateTime" to "LocalDateTime",
        "time" to "LocalTime",
        "date" to "LocalDate"
    ))
    importMappings.set(mapOf(
        "LocalDateTime" to "java.time.LocalDateTime",
        "LocalTime" to "java.time.LocalTime",
        "LocalDate" to "java.time.LocalDate"
    ))
    configOptions.set(mapOf(
        "interfaceOnly" to "false",
        "serviceInterface" to "true",
        "useSpringBoot3" to "true",
        "useTags" to "true",
        "dateLibrary" to "java8",
        "serializationLibrary" to "jackson",
        "exceptionHandler" to "false",
        "skipDefaultInterface" to "true",
        "useOneOfInterfaces" to "true"
    ))
}

sourceSets {
    main {
        kotlin {
            srcDir("${buildDir}/generated/src/main/kotlin")
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.register<Exec>("postgresUp") {
    description = "PostgreSQL 컨테이너 시작"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.yml up -d postgres")
}

tasks.register<Exec>("postgresStop") {
    description = "PostgreSQL 컨테이너 종료"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.yml stop postgres")
}

tasks.register<Exec>("postgresRemove") {
    description = "PostgreSQL 컨테이너 삭제 및 볼륨 제거"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.yml down -v postgres")
}

tasks.register<Exec>("postgresStatus") {
    description = "PostgreSQL 컨테이너 상태 확인"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.yml ps postgres")
}

tasks.register<Exec>("testPostgresUp") {
    description = "테스트용 PostgreSQL 컨테이너 시작"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml up -d postgres-test")
}

tasks.register<Exec>("testPostgresStop") {
    description = "테스트용 PostgreSQL 컨테이너 종료"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml stop postgres-test")
}

tasks.register<Exec>("testPostgresRemove") {
    description = "테스트용 PostgreSQL 컨테이너 삭제 및 볼륨 제거"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml down -v postgres-test")
}

tasks.register<Exec>("testRedisUp") {
    description = "테스트용 Redis 컨테이너 시작"
    environment("PATH", "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin")
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml up -d redis-test")
}

tasks.register<Exec>("testRedisStop") {
    description = "테스트용 Redis 컨테이너 종료"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml stop redis-test")
}

tasks.register<Exec>("testRedisRemove") {
    description = "테스트용 Redis 컨테이너 삭제 및 볼륨 제거"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml down -v redis-test")
}

tasks.register<Exec>("testContainersUp") {
    description = "테스트용 컨테이너 전체 시작"
    environment("PATH", "/usr/local/bin:/usr/bin:/bin:/usr/sbin:/sbin")
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml up -d")
}

tasks.register<Exec>("testContainersStop") {
    description = "테스트용 컨테이너 전체 종료"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml stop")
}

tasks.register<Exec>("testContainersRemove") {
    description = "테스트용 컨테이너 전체 삭제 및 볼륨 제거"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml down -v")
}

tasks.register<Exec>("testContainersStatus") {
    description = "테스트용 컨테이너 상태 확인"
    commandLine("sh", "-c", "/usr/local/bin/docker compose -f docker-compose.test.yml ps")
}

tasks.register<Exec>("bundleOpenApi") {
    description = "OpenAPI 스펙 파일 병합"
    workingDir = file("${rootDir}/../../packages/api-spec/openapi")
    commandLine(
        "sh", "-c",
        "source ~/.nvm/nvm.sh && npx @redocly/cli bundle 3040-api-spec-overview.yml -o 3040-api-spec.yml"
    )
}


/*
* redoc 이 참조할 스펙 문서를 복사합니다.
* */
tasks.register<Copy>("copyOpenApiSpec") {
    dependsOn("openApiGenerate")
    from("${buildDir}/generated/src/main/resources/openapi.yaml")
    into("${projectDir}/src/main/resources/static/openapi")
    rename { "openapi.yaml" }
}

tasks.named("processResources") {
    dependsOn("copyOpenApiSpec")
}

tasks.compileKotlin {
    dependsOn(tasks.openApiGenerate)
}

tasks.openApiGenerate {
    dependsOn("bundleOpenApi")
}

/*
* 테스트 시 실패 로그를 자세히 남김으로써, api 스펙 문서에 정의된 api를 구현하지 않으면 실행되지 못하는 원인을 알 수 있도록 한다.
* ./gradlew build 를 하면 원하는대로 빌드 시 실패 이유를 알 수 있지만,
* ./gradlew build -x test 를 하면 테스트가 실행되지 않기 때문에 실패 이유를 알 수 없다. 빈 주입이 런타임에 일어나기 때문이다. 이것은 한계로 남겨둔다.
* */
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}