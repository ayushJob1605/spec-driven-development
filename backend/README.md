# Backend (Support Ticket API)

Spring Boot 3.4.3 / Java 21 / Gradle.

## Requirements

- JDK 21

## Run tests

```bash
cd backend
./gradlew test
```

## Run application (T01 scaffold)

```bash
cd backend
./gradlew bootRun
```

Local overrides: copy `application-local.yml.example` to `src/main/resources/application-local.yml`
(or repo-root `application-local.yml` patterns already gitignored). Do not commit secrets.

Profiles `dev` and `test` are stubs until T02/T05 wire PostgreSQL/H2/Flyway.
