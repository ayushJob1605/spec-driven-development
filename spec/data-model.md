# Data Model

## 1. Entities

### Ticket

| Field | DB column | Type | Nullable | Notes |
|-------|-----------|------|----------|-------|
| id | id | UUID | No | Primary key |
| title | title | VARCHAR(200) | No | Required; non-blank after trim |
| description | description | VARCHAR(5000) | No | Required; non-blank after trim |
| priority | priority | ENUM | No | LOW, MEDIUM, HIGH, CRITICAL |
| status | status | ENUM | No | OPEN, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED |
| assignee | assignee | VARCHAR(100) | Yes | null means unassigned |
| createdAt | created_at | TIMESTAMPTZ | No | Set on create |
| updatedAt | updated_at | TIMESTAMPTZ | No | Set on create and every mutation |

### Comment

| Field | DB column | Type | Nullable | Notes |
|-------|-----------|------|----------|-------|
| id | id | UUID | No | Primary key |
| ticketId | ticket_id | UUID | No | FK → ticket.id |
| content | content | VARCHAR(2000) | No | Required; non-blank after trim |
| author | author | VARCHAR(100) | Yes | Optional |
| createdAt | created_at | TIMESTAMPTZ | No | Set on create |
| updatedAt | updated_at | TIMESTAMPTZ | No | Set on create (and update if ever allowed) |

## 2. Relationships

- One Ticket has many Comments (`ticket_id` foreign key).
- Ticket delete is out of scope for v1 product flows; FK should restrict or
  cascade consistently in migrations (recommend ON DELETE CASCADE for
  orphan prevention if hard delete is ever used internally).

## 3. Constraints

- `title`, `description`, `priority`, `status` NOT NULL on ticket
- `content`, `ticket_id` NOT NULL on comment
- Length limits as in section 1
- Enum columns accept only declared values
- Assignee unassign = SQL NULL (API JSON `null`)

## 4. Indexes

| Index | Purpose |
|-------|---------|
| ticket(created_at DESC) | Newest-first listing |
| ticket(status) | Status filter |
| comment(ticket_id) | Load comments for detail |

Search may use database-appropriate case-insensitive partial match
(e.g. ILIKE / LOWER LIKE). Additional full-text indexes are optional and
not required for v1.

## 5. Lifecycle

1. **Create ticket** — status OPEN; set createdAt and updatedAt.
2. **Update fields** — allowed only when status is OPEN, IN_PROGRESS, or
   RESOLVED; refresh updatedAt; status unchanged.
3. **Transition status** — only via dedicated API per state-machine.md;
   refresh updatedAt.
4. **Add comment** — allowed only when ticket is not CLOSED or CANCELLED;
   set comment timestamps; refresh ticket updatedAt.
5. **Terminal** — CLOSED/CANCELLED are read-only for fields, status, and
   comments.

## 6. Data Integrity Rules

- Whitespace-only required strings are invalid (enforced in application
  validation → 422).
- Invalid enum values → 422.
- Illegal status transitions → 409.
- Missing ticket → 404.
- Unknown JSON properties on requests → 400 (API layer).

## 7. Migration Considerations

- All schema changes via Flyway.
- PostgreSQL for development/demo.
- H2 for automated tests; migrations must remain compatible with the test
  profile strategy described in architecture.md.
