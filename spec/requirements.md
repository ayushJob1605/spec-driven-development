# Requirements

## 1. Purpose

Build a Support Ticket Management System that lets users create, list, view,
update, comment on, search, and filter support tickets, with backend-enforced
status transitions, input validation, durable persistence, and clear UI errors.

## 2. Scope

### In scope (v1)

- Create ticket
- List tickets (paginated, newest-first by createdAt)
- View ticket details
- Update title, description, priority, and assignee (when allowed)
- Change status via dedicated API (state machine enforced)
- Add comments (when allowed)
- Search by keyword (title and description)
- Filter by status (combinable with search)
- Backend validation
- Meaningful UI error display
- PostgreSQL persistence for development/demo
- H2 for automated tests
- Flyway migrations
- State-machine integration tests
- No secrets committed
- Unauthenticated local/demo use
- Local development CORS allowing the Next.js frontend origin

### Out of scope (v1)

- Authentication and authorization
- Optimistic locking / concurrent-update conflict handling
- Notifications, attachments, SLA, analytics
- Comment edit/delete
- Ticket delete/merge
- Multi-tenancy
- Searching comment bodies

## 3. Actors

| Actor | Description |
|-------|-------------|
| Operator | Unauthenticated v1 user of the UI/API who manages tickets and comments |
| System | Enforces validation, state machine, persistence, and terminal-state rules |

## 4. Functional Requirements

### REQ-001 Create ticket

The system shall allow creation of a ticket with required title, required
description, required priority, and optional assignee. New tickets shall start
in status OPEN. Each ticket shall have a UUID identifier and createdAt/updatedAt
timestamps.

### REQ-002 List tickets

The system shall list tickets with pagination. Default page size is 20; maximum
page size is 100. Default sort is newest-first by createdAt. List responses shall
include pagination metadata: page, size, totalElements, totalPages.

### REQ-003 View ticket details

The system shall return full ticket details for a given ticket UUID, including
comments. CLOSED and CANCELLED tickets remain readable.

### REQ-004 Update ticket fields

While status is OPEN, IN_PROGRESS, or RESOLVED, the system shall allow updates
to title, description, priority, and assignee. Status shall not be changed
through this field-update capability. CLOSED and CANCELLED tickets shall reject
field updates.

### REQ-005 Change assignee

Assignee is an optional string (max 100 characters). Unassigning shall be
performed by setting assignee to null when field updates are allowed.

### REQ-006 Add comment

The system shall allow adding a comment with required content (max 2000
characters) and optional author (max 100 characters), except when the ticket is
CLOSED or CANCELLED. Each comment shall have createdAt and updatedAt timestamps.

### REQ-007 Search tickets

The system shall support case-insensitive partial matching of a keyword against
ticket title and description.

### REQ-008 Filter by status

The system shall support filtering the ticket list by status. Search and status
filter may be combined on the same list request.

### REQ-009 Status transitions

The system shall change ticket status only through a dedicated status-change API
under base path /api/v1, and only according to the approved state machine.

### REQ-010 Persist data

Ticket and comment data shall persist in PostgreSQL for development/demo and
shall survive application restart. Automated tests may use H2. Schema changes
shall be managed with Flyway.

### REQ-011 Validate input

The backend shall validate requests and reject invalid input and illegal
transitions using the approved error model.

### REQ-012 UI errors and terminal UX

The UI shall display meaningful errors derived from the backend JSON error
responses. For CLOSED and CANCELLED tickets, the UI shall hide edit and comment
actions. The backend remains the authority and shall still enforce terminal-state
restrictions.

## 5. Non-Functional Requirements

### NFR-001 Technology

- Backend: Java 21, Spring Boot, REST API
- Frontend: Next.js
- API base path: /api/v1
- DB: PostgreSQL (development/demo), H2 (tests)
- Migrations: Flyway

### NFR-002 Durability

Development/demo data stored in PostgreSQL must survive application restart.

### NFR-003 Secrets

No secrets shall be committed to the repository.

### NFR-004 Auth

No authentication or authorization in v1.

### NFR-005 CORS

Local development CORS shall allow the Next.js frontend origin
(`http://localhost:3000`).

### NFR-006 Error contract

API errors shall use a consistent JSON error format with fields:
timestamp, status, code, message, path, fieldErrors[].

HTTP status usage:

- 400 for malformed requests (including unknown JSON fields)
- 404 for missing tickets
- 409 for invalid state transitions
- 422 for validation errors (including blank/whitespace-only required values
  and invalid enum values)

### NFR-007 Identifiers and timestamps

- Ticket ID: UUID
- Ticket and comment entities include createdAt and updatedAt

## 6. Business Rules

### BR-001 Initial status

Every newly created ticket has status OPEN.

### BR-002 Allowed transitions (exact set)

- OPEN → IN_PROGRESS
- IN_PROGRESS → RESOLVED
- RESOLVED → CLOSED
- OPEN → CANCELLED
- IN_PROGRESS → CANCELLED

Any other transition is invalid and shall be rejected with 409.

### BR-003 Terminal states

CLOSED and CANCELLED are terminal. No further status transitions are allowed.
Field updates and new comments are not allowed.

### BR-004 Status change channel

Status may change only via the dedicated status API, never via field update.

### BR-005 Field editability

- Editable when status is OPEN, IN_PROGRESS, or RESOLVED: title, description,
  priority, assignee
- Not editable when status is CLOSED or CANCELLED
- Status is never edited via field update

### BR-006 Comment eligibility

Comments may not be added when status is CLOSED or CANCELLED.

### BR-007 Priority values

Priority is required and must be one of: LOW, MEDIUM, HIGH, CRITICAL.
Invalid enum values return 422.

### BR-008 Required ticket fields and lengths

- Title: required, max 200 characters
- Description: required, max 5000 characters
- Blank or whitespace-only title/description are rejected with 422

### BR-009 Assignee

Assignee is optional, max 100 characters. Unassign using null.

### BR-010 Comment fields

- Content: required, max 2000 characters; blank/whitespace-only rejected with 422
- Author: optional, max 100 characters

### BR-011 Search semantics

Search is case-insensitive partial match on title and description only.

### BR-012 List behavior

- Pagination default size 20, maximum size 100
- Metadata: page, size, totalElements, totalPages
- Default sort: createdAt descending (newest-first)

### BR-013 Unknown JSON fields

Requests containing unknown JSON fields return 400.

### BR-014 Terminal UI behavior

UI hides edit/comment actions for terminal tickets; backend still enforces
restrictions.

## 7. Constraints

- Spec-Driven Development lifecycle is mandatory.
- Implementation must remain traceable to approved specifications.
- Frontend for v1 is Next.js.
- Database migrations must be managed with Flyway.
- Optimistic locking is out of scope for v1.

## 8. Assumptions

- Unauthenticated access is acceptable for v1 local/demo use.
- Read access to CLOSED and CANCELLED tickets (list/detail) remains allowed.
- Local Next.js CORS origin is `http://localhost:3000`.

## 9. Open Questions

None blocking for v1 requirements. Specification defaults for API query
parameter names, error `code` values, and `fieldErrors[]` item shape are
documented in `api-contract.md`.

## 10. Acceptance Criteria

- Ticket can be created from the UI with required fields; status is OPEN; ID is UUID.
- Tickets can be listed with pagination (default 20, max 100), newest-first by createdAt,
  including page, size, totalElements, totalPages.
- Ticket details can be viewed, including comments and timestamps.
- Title, description, priority, and assignee can be updated when status is
  OPEN, IN_PROGRESS, or RESOLVED.
- Assignee can be unassigned via null when edits are allowed.
- Field updates and comment creation are rejected for CLOSED and CANCELLED.
- UI hides edit/comment actions for CLOSED and CANCELLED tickets.
- Comments can be added with required content when the ticket is not terminal.
- Keyword search matches title/description case-insensitively and partially.
- Status filter works and can be combined with search.
- All five allowed transitions succeed via the dedicated status API under /api/v1.
- All disallowed transitions are rejected by the backend with 409.
- Malformed requests / unknown fields return 400; missing tickets 404;
  validation failures (including whitespace-only required fields and invalid
  enums) return 422.
- Error bodies include timestamp, status, code, message, path, fieldErrors[].
- UI shows meaningful errors from that JSON error format.
- Data survives application restart on PostgreSQL (dev/demo).
- State-machine integration tests pass (H2 allowed for tests).
- No secrets are committed.
