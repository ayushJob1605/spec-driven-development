# Test Strategy

## 1. Testing Goals

- Prove API behavior matches `api-contract.md` and `requirements.md`
- Prove state machine allows only the five valid transitions and rejects others
- Prove validation and terminal-state rules
- Prove persistence behavior suitable for demo (PostgreSQL) and CI (H2)
- Keep tests fast, deterministic, and free of secrets

## 2. Test Pyramid

| Layer | Focus | Volume |
|-------|-------|--------|
| Unit | State-machine matrix, pure validation helpers | High |
| Integration / API | Controllers + services + DB (H2) | Medium |
| Manual / light E2E | Critical UI journeys against local stack | Low |

## 3. Unit Testing

- All valid transitions succeed in isolation
- Representative invalid transitions fail (including terminal and skip-ahead)
- Priority/status enum parsing behavior as applicable
- Blank/whitespace rejection rules for required strings

## 4. Integration Testing

- Flyway migrations apply on H2 test profile
- Create/list/get/patch/status/comment flows against real Spring context
- Search (`q`) and status filter combination
- Pagination defaults, max size enforcement, newest-first order
- Restart durability for demo is verified manually or with PostgreSQL-based
  test when available; not required on H2 in-memory if data is non-durable by design

## 5. API Testing

Assert HTTP status and error body shape (`timestamp`, `status`, `code`,
`message`, `path`, `fieldErrors`):

| Scenario | Expected |
|----------|----------|
| Unknown JSON property | 400 MALFORMED_REQUEST |
| Missing ticket UUID | 404 NOT_FOUND |
| Illegal transition | 409 INVALID_TRANSITION |
| Blank title / invalid enum / oversize field | 422 VALIDATION_ERROR |
| PATCH/comment on CLOSED or CANCELLED | 422 VALIDATION_ERROR |
| Valid create | 201; status OPEN |
| Valid transition set | 200 for each allowed edge |

## 6. End-to-End Testing

Optional for v1 automation. Manual checklist:

- Create from UI → list → detail
- Edit fields; unassign with null
- Search + status filter
- Legal transitions; UI hides illegal/terminal actions
- Error messages visible for 422/409
- Terminal ticket read-only UX

## 7. Security Testing

- Confirm no secrets in git (ignore rules for `.env`, local config, keys)
- No auth tests required beyond “endpoints work without credentials” for v1

## 8. Performance Testing

Out of scope for v1 beyond basic pagination max size of 100.

## 9. Test Data

- Build tickets in each status as needed for transition tests
- Prefer unique titles/keywords per test to avoid cross-talk
- No production credentials in fixtures

## 10. CI Verification

- Run unit + API/integration tests on H2 in CI
- Fail build on test failure
- State-machine integration tests are mandatory for merge readiness
