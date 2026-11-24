# Architecture

## 1. Context

Support Ticket Management System (v1): Next.js UI and Spring Boot REST API
persisting tickets and comments in PostgreSQL, with Flyway migrations and
H2 for automated tests.

## 2. Architectural Goals

- Enforce business rules and state machine on the backend
- Keep API contract stable under `/api/v1`
- Durable demo/dev data in PostgreSQL
- Simple, testable layering without unnecessary frameworks
- No secrets in source control

## 3. System Context

```text
[Operator browser]
       │
       ▼
[Next.js frontend :3000]
       │  HTTP JSON /api/v1
       ▼
[Spring Boot backend]
       │
       ▼
[PostgreSQL]   (dev/demo)
[H2]           (tests only)
```

## 4. Components

| Component | Technology |
|-----------|------------|
| Web UI | Next.js |
| API | Spring Boot (Java 21), REST |
| Domain/services | Spring services (validation + state machine) |
| Persistence | Spring Data / JPA (or equivalent Spring persistence) |
| Migrations | Flyway |
| Database | PostgreSQL (dev/demo), H2 (tests) |

## 5. Component Responsibilities

| Component | Responsibility |
|-----------|----------------|
| Next.js UI | Screens, navigation, call API, display errors, hide terminal actions |
| Controllers | HTTP mapping, request/response DTOs under `/api/v1` |
| Services | Validation, edit/comment eligibility, state transitions |
| State machine module | Centralize allowed transition checks |
| Repositories | Persist/load Ticket and Comment |
| Flyway | Schema versioning |
| Exception handler | Map failures to the standard error JSON |

## 6. Data Flow

1. UI sends JSON to `/api/v1/...`
2. Controller deserializes (fail unknown properties → 400)
3. Service validates and applies rules / transitions
4. Repository reads/writes PostgreSQL (or H2 in tests)
5. Response DTO returned; errors mapped to error model

## 7. External Integrations

None in v1.

## 8. Security

- No authentication/authorization in v1
- CORS: allow `http://localhost:3000` for local development
- Secrets (DB passwords, etc.) via environment or local ignored config —
  never committed
- Input validation on all write endpoints

## 9. Observability

Minimal v1: application logs for request failures and unexpected errors.
No requirement for metrics/tracing platforms in v1.

## 10. Deployment Considerations

- Local demo: PostgreSQL + Spring Boot + Next.js
- Tests: Spring Boot test slice/integration with H2 and Flyway
- Restart durability applies to PostgreSQL-backed runs

## 11. Architectural Decisions

See `decisions.md` for the decision log. Summary:

- Next.js frontend
- Dedicated status transition endpoint
- PostgreSQL + Flyway; H2 for tests
- No auth; no optimistic locking in v1
- Standard error JSON and HTTP mapping

## 12. Open Questions

None blocking for v1 architecture. Package naming and exact Spring module
layout are left to the implementation plan.
