# Implementation Plan / Tasks

**Phase:** Implementation (in progress)  
**Status:** Build tool and layout approved — **Gradle**, `backend/` + `frontend/`  
**Basis:** Approved `spec/*` and `.cursor/rules/` (`00-sdd-workflow`, `10-java-springboot`, `20-testing`, `30-api-standards`, `40-documentation`)  
**Constraint:** No new product requirements.

**Repo layout:** `backend/` (Spring Boot / Gradle), `frontend/` (Next.js, later tasks). See `decisions.md` ADR-009.

---

## Task dependency overview

```text
T01 → T02 → T03 → T04 → T05
                ↘         ↘
                 T06 → T07 → T08 → T09 → T10 → T11 → T12
                                                      ↓
                         T13 → T14 → T15 → T16 → T17 (final verification)
```

---

## T01 — Backend project scaffold

| | |
|---|---|
| **Task ID** | T01 |
| **Objective** | Create Spring Boot (Java 21) backend skeleton with `/api/v1` readiness, test profile hook, and no committed secrets. |
| **Spec references** | `architecture.md` §4–5, §8, §10; `requirements.md` NFR-001, NFR-003, NFR-004; `decisions.md` ADR-002, ADR-004; `.cursor/rules/10-java-springboot.mdc` |
| **Files/components affected** | `backend/` Gradle build; main application class; `application.yml` / profile stubs; `application-local.yml.example` |
| **Dependencies** | None |
| **Tests required** | Context-loads smoke test |
| **Acceptance criteria** | App starts with a non-secret config pattern; Java 21; no auth filters required; secrets only via env/local ignored files already covered by `.gitignore`. |
| **Status** | DONE |

---

## T02 — Database schema (Flyway) + persistence model

| | |
|---|---|
| **Task ID** | T02 |
| **Objective** | Add Flyway migrations and JPA (or equivalent) entities/repositories for Ticket and Comment per data model. |
| **Spec references** | `data-model.md` §1–4, §7; `architecture.md` ADR-002 path; `requirements.md` REQ-010, NFR-001 |
| **Files/components affected** | `db/migration/V1__...sql` (or backend resources equivalent); `Ticket`/`Comment` entities; repositories; `application-dev` (PostgreSQL) and `application-test` (H2) datasource settings |
| **Dependencies** | T01 |
| **Tests required** | Integration: Flyway migrates successfully on H2 test profile |
| **Acceptance criteria** | Tables/columns/indexes match `data-model.md`; UUID PKs; nullable assignee; FK `comment.ticket_id`; enums for priority/status; no secrets in migration/config committed files. |
| **Status** | DONE |

---

## T03 — Domain enums + state machine module

| | |
|---|---|
| **Task ID** | T03 |
| **Objective** | Implement centralized transition checker for the exact five allowed edges; initial status OPEN constant. |
| **Spec references** | `state-machine.md` §1–6; `requirements.md` BR-001–BR-004; `architecture.md` §5 (state machine module); `test-strategy.md` §3 |
| **Files/components affected** | `TicketStatus`, `Priority` enums; `TicketStateMachine` (or equivalent) in domain/service layer |
| **Dependencies** | T01 (can proceed in parallel with T02 if enums don’t need DB) |
| **Tests required** | **Unit:** all 5 valid transitions allowed; representative invalid set (terminal, skip-ahead, same-state, reopen) rejected |
| **Acceptance criteria** | Only the exact transition matrix is legal; no side effects beyond boolean/allow decision at this layer. |
| **Status** | DONE |

---

## T04 — Error model + global exception handling + JSON unknown properties

| | |
|---|---|
| **Task ID** | T04 |
| **Objective** | Implement standard error JSON and map domain/API failures to 400/404/409/422 with specified codes. |
| **Spec references** | `api-contract.md` §3; `requirements.md` NFR-006, BR-013; `architecture.md` §5–6; `.cursor/rules/30-api-standards.mdc` |
| **Files/components affected** | `ErrorResponse`, `FieldErrorItem`; `@ControllerAdvice` / exception handler; Jackson FAIL_ON_UNKNOWN_PROPERTIES (or equivalent); domain exceptions (`NotFound`, `InvalidTransition`, `Validation`) |
| **Dependencies** | T01 |
| **Tests required** | API/unit tests that unknown property → 400 `MALFORMED_REQUEST` with required error fields |
| **Acceptance criteria** | Error body always has `timestamp`, `status`, `code`, `message`, `path`, `fieldErrors` (`field` + `message`); `fieldErrors` may be `[]`; no stack traces in responses. |
| **Status** | DONE |

---

## T05 — CORS + environment profiles wiring

| | |
|---|---|
| **Task ID** | T05 |
| **Objective** | Enable local CORS for `http://localhost:3000`; wire PostgreSQL (dev/demo) vs H2 (test) without committing secrets. |
| **Spec references** | `api-contract.md` §8; `architecture.md` §8, §10; `decisions.md` ADR-002, ADR-008; `requirements.md` NFR-005 |
| **Files/components affected** | CORS config; Spring profiles; sample `application-local.yml.example` (no real secrets) if needed |
| **Dependencies** | T01, T02 |
| **Tests required** | None mandatory beyond existing context load; optional CORS config assertion |
| **Acceptance criteria** | Test profile uses H2 + Flyway; dev/demo documents PostgreSQL via env/local ignored config; CORS origin exactly `http://localhost:3000`. |
| **Status** | DONE |

---

## T06 — Create ticket API

| | |
|---|---|
| **Task ID** | T06 |
| **Objective** | `POST /api/v1/tickets` creates ticket with validation; status always OPEN. |
| **Spec references** | `api-contract.md` POST create; `requirements.md` REQ-001, BR-007–BR-009; `data-model.md` lifecycle §5.1; `.cursor/rules/10-java-springboot.mdc` (DTOs, service layer) |
| **Files/components affected** | Controller, DTOs, service, repository usage |
| **Dependencies** | T02, T03, T04 |
| **Tests required** | API: 201 + UUID + status OPEN; 422 blank/whitespace/oversized/invalid priority; 400 unknown fields; assignee optional |
| **Acceptance criteria** | Matches contract request/response; client cannot set status on create; timestamps set. |
| **Status** | DONE |

---

## T07 — Get ticket by ID API

| | |
|---|---|
| **Task ID** | T07 |
| **Objective** | `GET /api/v1/tickets/{id}` returns detail including comments; 404 if missing. |
| **Spec references** | `api-contract.md` GET by id; `requirements.md` REQ-003; `data-model.md` relationships |
| **Files/components affected** | Controller, service, mapper; comment collection loading |
| **Dependencies** | T06 (needs create path or test fixtures) |
| **Tests required** | API: 200 with comments array; 404 `NOT_FOUND` error shape |
| **Acceptance criteria** | CLOSED/CANCELLED tickets remain readable; response shape matches `TicketResponse`. |
| **Status** | DONE |

---

## T08 — List / search / filter / pagination API

| | |
|---|---|
| **Task ID** | T08 |
| **Objective** | `GET /api/v1/tickets` with `page`, `size`, `status`, `q`; newest-first by `createdAt`. |
| **Spec references** | `api-contract.md` §4 list, §5–6; `requirements.md` REQ-002, REQ-007, REQ-008, BR-011–BR-012; `decisions.md` ADR-006 |
| **Files/components affected** | List query in repository/service; page response DTO |
| **Dependencies** | T06 |
| **Tests required** | API/integration: default page=0 size=20; size>100 → 422; invalid status → 422; `q` case-insensitive partial on title/description; `q`+`status` combined; sort `createdAt` DESC; metadata `page`,`size`,`totalElements`,`totalPages` |
| **Acceptance criteria** | Behavior matches contract exactly; no undocumented sort modes. |
| **Status** | DONE |

---

## T09 — Update ticket fields API (PATCH)

| | |
|---|---|
| **Task ID** | T09 |
| **Objective** | `PATCH /api/v1/tickets/{id}` updates title/description/priority/assignee when non-terminal; reject status property; support assignee `null`. |
| **Spec references** | `api-contract.md` PATCH; `requirements.md` REQ-004, REQ-005, BR-005, BR-009; `state-machine.md` §5.3–5.4; `data-model.md` §5.2 |
| **Files/components affected** | PATCH DTO (no status), service editability checks |
| **Dependencies** | T07 |
| **Tests required** | API: successful patch on OPEN/IN_PROGRESS/RESOLVED; unassign `null`; terminal → 422; `status` in body → 400; at least one field required; length/blank validation 422; 404 missing |
| **Acceptance criteria** | Status unchanged by PATCH; `updatedAt` refreshed; DTOs not entities exposed. |
| **Status** | DONE |

---

## T10 — Status transition API + state-machine integration tests

| | |
|---|---|
| **Task ID** | T10 |
| **Objective** | `POST /api/v1/tickets/{id}/status` applies only legal transitions; illegal → 409. |
| **Spec references** | `api-contract.md` POST status; `state-machine.md` full; `requirements.md` REQ-009, BR-002–BR-004; `test-strategy.md` §4–5, §10; `.cursor/rules/20-testing.mdc` |
| **Files/components affected** | Status endpoint; service calling state machine; persistence of new status + `updatedAt` |
| **Dependencies** | T03, T07, T09 (helpful for fixture setup) |
| **Tests required** | **Mandatory integration/API:** each of 5 valid transitions → 200; representative invalid (incl. CLOSED/CANCELLED/reopen/skip/same-state) → 409 `INVALID_TRANSITION`; invalid/missing enum → 422; unknown props → 400; 404 missing ticket |
| **Acceptance criteria** | Only dedicated endpoint changes status; side effects limited to status + `updatedAt`. |
| **Status** | DONE |

---

## T11 — Add comment API

| | |
|---|---|
| **Task ID** | T11 |
| **Objective** | `POST /api/v1/tickets/{id}/comments` with content/author rules; forbidden on terminal tickets. |
| **Spec references** | `api-contract.md` POST comments; `requirements.md` REQ-006, BR-006, BR-010; `data-model.md` §5.4 |
| **Files/components affected** | Comment controller/service/repo; ticket `updatedAt` refresh |
| **Dependencies** | T07 |
| **Tests required** | API: 201 on non-terminal; blank content 422; author optional max 100; terminal 422; 404 missing ticket; 400 unknown props; detail GET shows new comment |
| **Acceptance criteria** | Matches contract; ticket `updatedAt` updated on comment add. |
| **Status** | DONE |

---

## T12 — Backend validation & error-contract integration suite (hardening)

| | |
|---|---|
| **Task ID** | T12 |
| **Objective** | Consolidate remaining API/validation cases from test strategy into a coherent suite; ensure CI-ready H2 tests. |
| **Spec references** | `test-strategy.md` §5, §7, §10; `requirements.md` REQ-011, AC list; `api-contract.md` §3 |
| **Files/components affected** | Test classes only (plus tiny fixes if gaps found) |
| **Dependencies** | T06–T11 |
| **Tests required** | Full matrix coverage for error shape on 400/404/409/422; oversize fields; whitespace-only; invalid enums; no-auth access works |
| **Acceptance criteria** | `./gradlew test` passes on H2; state-machine integration tests included; no secrets in fixtures. |
| **Status** | DONE |

---

## T13 — Next.js frontend scaffold + API client + error mapping

| | |
|---|---|
| **Task ID** | T13 |
| **Objective** | Create Next.js app with typed API client for `/api/v1` and shared error display helpers. |
| **Spec references** | `architecture.md` §3–4; `decisions.md` ADR-001; `ui-flow.md` §5–6; `api-contract.md` §1, §3, §8; `requirements.md` REQ-012, NFR-001 |
| **Files/components affected** | `frontend/` Next.js project; API base URL via public env (non-secret); error mapper for `message` + `fieldErrors` |
| **Dependencies** | T04+ (needs stable error contract); ideally T12 backend available for manual check |
| **Tests required** | None automated required by spec (manual later); optional unit test for error mapper |
| **Acceptance criteria** | Client can call backend origin; maps 400/404/409/422 per `ui-flow.md`; no secrets committed. |
| **Status** | DONE |

---

## T14 — Ticket list UI (search, filter, pagination)

| | |
|---|---|
| **Task ID** | T14 |
| **Objective** | Implement Ticket List screen: keyword search, status filter, pagination, navigate to detail. |
| **Spec references** | `ui-flow.md` J2, screens, §7; `requirements.md` REQ-002, REQ-007, REQ-008, REQ-012 |
| **Files/components affected** | List page/components |
| **Dependencies** | T13, T08 |
| **Tests required** | Manual per `test-strategy.md` §6 (list/search/filter/page) |
| **Acceptance criteria** | Calls GET list with `q`/`status`/`page`/`size`; shows loading and empty states; newest-first as returned by API; errors shown meaningfully. |
| **Status** | DONE |

---

## T15 — Create ticket UI

| | |
|---|---|
| **Task ID** | T15 |
| **Objective** | Implement Create Ticket form and success navigation to detail. |
| **Spec references** | `ui-flow.md` J1; `requirements.md` REQ-001, REQ-012; `api-contract.md` create fields |
| **Files/components affected** | Create page/form |
| **Dependencies** | T13, T06 |
| **Tests required** | Manual: create success + 422 field errors displayed |
| **Acceptance criteria** | Required title/description/priority; optional assignee; disables submit while in-flight; navigates to detail on 201. |
| **Status** | DONE |

---

## T16 — Ticket detail UI (view, edit, status, comments, terminal UX)

| | |
|---|---|
| **Task ID** | T16 |
| **Objective** | Implement detail: view; conditional edit/unassign; legal status actions only; comments; hide mutate actions when CLOSED/CANCELLED; 404 handling. |
| **Spec references** | `ui-flow.md` J3–J6, §4–7; `state-machine.md` valid targets; `requirements.md` REQ-003–REQ-006, REQ-009, REQ-012, BR-014 |
| **Files/components affected** | Detail page; edit form; status controls; comment form |
| **Dependencies** | T13, T07, T09, T10, T11 |
| **Tests required** | Manual E2E checklist from `test-strategy.md` §6 |
| **Acceptance criteria** | Terminal tickets read-only in UI; backend still authoritative; 409 message shown; fieldErrors mapped; loading/disabled submit behavior. |
| **Status** | DONE |

---

## T17 — Final verification

| | |
|---|---|
| **Task ID** | T17 |
| **Objective** | Verify full acceptance criteria against specs; update traceability; confirm no secrets; PostgreSQL restart durability check. |
| **Spec references** | `requirements.md` §10 Acceptance Criteria; `traceability.md`; `test-strategy.md` §6–7, §10; `requirements.md` REQ-010, NFR-003 |
| **Files/components affected** | `spec/traceability.md` (implementation/test columns); possibly README runbook |
| **Dependencies** | T12, T16 |
| **Tests required** | Re-run full backend test suite; complete manual UI checklist; manual PostgreSQL create → restart → data present |
| **Acceptance criteria** | All AC items checked; state-machine integration tests pass; `.env`/credentials not committed; traceability updated from TBD → implemented/tested where done. |
| **Status** | DONE (automated + docs; PostgreSQL restart remains a manual demo check) |

---

## Coverage map

| Area | Tasks |
|------|-------|
| Backend scaffold / layers | T01, T04, T05 |
| Database / Flyway / persistence | T02, T05, T17 |
| State machine | T03, T10, T12 |
| API endpoints | T06–T11 |
| Validation / errors | T04, T06–T12 |
| Frontend | T13–T16 |
| Error handling (UI+API) | T04, T12, T13, T16 |
| Integration tests | T02, T08, T10, T12 |
| Final verification | T17 |

---

## Implementation order (reviewable increments)

1. T01 → T02 → T03 → T04 → T05
2. T06 → T07 → T08 → T09 → T10 → T11 → T12
3. T13 → T14 → T15 → T16
4. T17

Implement **one task at a time** per SDD rules.

---

## Specification gaps that could block implementation

| Gap | Severity | Notes |
|-----|----------|-------|
| **Build tool** | **Resolved** | Gradle (ADR-009). |
| **Spring Boot version** | **Resolved for T01** | Spring Boot **3.4.3** (Java 21). |
| **Monorepo folder names** | **Resolved** | `backend/` + `frontend/` (ADR-009). |
| **Comment sort order on detail** | Non-blocking | Contract says implementation-defined; recommend oldest-first (already in api-contract). |
| **Exact error `message` string text** | Non-blocking | Shape/codes specified; wording free as long as meaningful. |
| **Negative `page` / `size` &lt; 1** | Non-blocking | Contract implies `page ≥ 0`, `size` 1–100 → treat as 422 validation (consistent with size&gt;100). |
| **H2 vs PostgreSQL enum/type SQL dialect** | Risk, not a missing requirement | Must be solved in T02 via compatible Flyway strategy; if blocked technically, stop and propose a spec/ADR tweak — do not invent product behavior. |

**No product-behavior gaps remain** for the approved ticket/state/API/UI rules. Build tool and layout gaps are closed (ADR-009).
