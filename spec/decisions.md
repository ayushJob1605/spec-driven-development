# Architectural Decisions

## Decision Log

### ADR-001 Next.js frontend

- **Context:** Requirements allow React/Next.js or equivalent; need a single v1 choice.
- **Decision:** Use Next.js for the frontend.
- **Alternatives:** Plain React SPA, other frameworks.
- **Consequences:** UI lives in a Next.js app; local origin `http://localhost:3000` for CORS.

### ADR-002 PostgreSQL for dev/demo, H2 for tests, Flyway migrations

- **Context:** Data must survive restart for demo; tests need a lightweight DB.
- **Decision:** PostgreSQL for development/demo; H2 for automated tests; Flyway for all schema migrations.
- **Alternatives:** H2 everywhere; Liquibase; auto-ddl only.
- **Consequences:** Migrations must stay compatible with both engines used by profiles; secrets for Postgres stay out of git.

### ADR-003 Dedicated status transition API

- **Context:** Status follows a strict state machine; field updates must not change status.
- **Decision:** `POST /api/v1/tickets/{id}/status` is the only way to change status. PATCH rejects/forbids status.
- **Alternatives:** Include status on PATCH with server-side checks.
- **Consequences:** Clearer API semantics; UI uses a separate status action.

### ADR-004 No authentication or authorization in v1

- **Context:** Security model unspecified beyond “no secrets committed.”
- **Decision:** Ship v1 without AuthN/AuthZ; document as local/demo only.
- **Alternatives:** Basic auth, JWT, OAuth.
- **Consequences:** Open API on the network where deployed; acceptable only for v1 local/demo scope.

### ADR-005 Error HTTP mapping and JSON shape

- **Context:** UI needs consistent, meaningful errors.
- **Decision:** Error body fields `timestamp`, `status`, `code`, `message`, `path`, `fieldErrors[]` where each item has `field` and `message`. HTTP: 400 malformed/unknown fields; 404 missing ticket; 409 invalid transition; 422 validation (including blank required fields, invalid enums, terminal edit/comment).
- **Alternatives:** RFC 7807 only; 400 for all client errors.
- **Consequences:** Backend exception handling and UI error mapping align on one contract.

### ADR-006 Identifiers, pagination, and list semantics

- **Context:** List/search/filter behavior must be unambiguous.
- **Decision:** Ticket IDs are UUIDs; page is 0-based default 0; size default 20 max 100; metadata `page`, `size`, `totalElements`, `totalPages`; sort newest-first by `createdAt`; query params `q` and `status`.
- **Alternatives:** Numeric IDs; 1-based pages; cursor pagination.
- **Consequences:** API and UI share one pagination model.

### ADR-007 No optimistic locking in v1

- **Context:** Concurrent edits possible but not required for v1.
- **Decision:** Out of scope; last write wins.
- **Alternatives:** Version column / ETags.
- **Consequences:** Simpler model; possible silent overwrites under concurrency.

### ADR-008 Local CORS origin

- **Context:** Next.js and Spring Boot run on different origins locally.
- **Decision:** Allow `http://localhost:3000`.
- **Alternatives:** Proxy-only via Next.js rewrites (may still be used in addition).
- **Consequences:** Backend CORS config is explicit for local demo.

### ADR-009 Gradle build tool and backend layout

- **Context:** Architecture left build tool and folder layout open; plan listed Maven vs Gradle as blocking for T01.
- **Decision:** Use Gradle (Spring Boot 3.4.x, Java 21 toolchain) under `backend/`. Frontend will live under `frontend/` (later tasks).
- **Alternatives:** Maven; root-level single-module layout.
- **Consequences:** Use `./gradlew` in `backend/`; CI and docs should target Gradle, not Maven.
