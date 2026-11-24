# API Contract

## 1. API Conventions

- Base path: `/api/v1`
- Content-Type: `application/json`
- Path ticket identifiers: UUID
- Timestamps: ISO-8601 UTC strings (e.g. `2026-09-20T08:05:00Z`)
- Unknown JSON properties in request bodies → **400**
- Enum values are uppercase strings as defined in requirements

### Common resource shapes

#### TicketResponse

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "title": "Cannot login",
  "description": "User reports 401 on login",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "alex@example.com",
  "createdAt": "2026-09-20T08:00:00Z",
  "updatedAt": "2026-09-20T08:00:00Z",
  "comments": []
}
```

`comments` is included on detail responses. List responses may omit `comments`
or return an empty array; detail must include the full comment list
(newest or oldest order is implementation-defined; recommend oldest-first
for conversation readability).

#### CommentResponse

```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "ticketId": "550e8400-e29b-41d4-a716-446655440000",
  "content": "Investigating",
  "author": "alex",
  "createdAt": "2026-09-20T08:10:00Z",
  "updatedAt": "2026-09-20T08:10:00Z"
}
```

## 2. Authentication

None in v1. Endpoints are accessible without credentials.

## 3. Error Model

All error responses use:

```json
{
  "timestamp": "2026-09-20T08:15:00Z",
  "status": 422,
  "code": "VALIDATION_ERROR",
  "message": "Request validation failed",
  "path": "/api/v1/tickets",
  "fieldErrors": [
    {
      "field": "title",
      "message": "must not be blank"
    }
  ]
}
```

### fieldErrors[] item shape

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| field | string | Yes | JSON property or query parameter name |
| message | string | Yes | Human-readable validation message |

`fieldErrors` may be an empty array when not applicable (e.g. 404, 409).

### Standard codes

| HTTP | code | When |
|------|------|------|
| 400 | MALFORMED_REQUEST | Malformed JSON, unknown properties, unreadable body |
| 404 | NOT_FOUND | Ticket UUID not found |
| 409 | INVALID_TRANSITION | Illegal status transition |
| 422 | VALIDATION_ERROR | Field validation, blank/whitespace-only required values, invalid enums, size limits, edits/comments on terminal tickets |

## 4. Endpoints

### POST /api/v1/tickets

Create a ticket. Status is always OPEN (client must not send status).

#### Request

```json
{
  "title": "Cannot login",
  "description": "User reports 401 on login",
  "priority": "HIGH",
  "assignee": "alex@example.com"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| title | Yes | 1–200 chars after trim; blank/whitespace → 422 |
| description | Yes | 1–5000 chars after trim |
| priority | Yes | LOW \| MEDIUM \| HIGH \| CRITICAL |
| assignee | No | null or omit if unassigned; max 100 chars |

#### Response

- **201 Created** — TicketResponse (comments empty)

#### Errors

- 400, 422

---

### GET /api/v1/tickets

List tickets with pagination, optional search, and optional status filter.

#### Query parameters

| Param | Default | Rules |
|-------|---------|-------|
| page | 0 | Zero-based page index; ≥ 0 |
| size | 20 | 1–100; values above 100 → 422 |
| status | (none) | If present: valid TicketStatus enum; invalid → 422 |
| q | (none) | If present: case-insensitive partial match on title OR description |

Sort: `createdAt` descending (newest-first). Fixed for v1.

#### Response

- **200 OK**

```json
{
  "content": [ /* TicketResponse items */ ],
  "page": 0,
  "size": 20,
  "totalElements": 42,
  "totalPages": 3
}
```

#### Errors

- 400 (malformed query where applicable), 422

---

### GET /api/v1/tickets/{id}

#### Response

- **200 OK** — TicketResponse including comments
- **404** — ticket not found

---

### PATCH /api/v1/tickets/{id}

Update mutable fields. Must not accept `status`.

#### Request

All fields optional; at least one must be present.

```json
{
  "title": "Updated title",
  "description": "Updated description",
  "priority": "CRITICAL",
  "assignee": null
}
```

| Field | Rules |
|-------|-------|
| title | If present: same as create |
| description | If present: same as create |
| priority | If present: valid enum |
| assignee | If present: string ≤ 100 or **null** to unassign |

#### Response

- **200 OK** — TicketResponse
- **404** — not found
- **422** — validation failure OR ticket is CLOSED/CANCELLED
- **400** — unknown properties (including `status` if treated as unknown, or reject `status` explicitly as malformed/validation — prefer **400** if `status` is an unknown/forbidden property on this endpoint, or **422** with field error; v1 decision: sending `status` on PATCH → **400** MALFORMED_REQUEST)

#### Errors

- 400, 404, 422

---

### POST /api/v1/tickets/{id}/status

Dedicated status transition API.

#### Request

```json
{
  "status": "IN_PROGRESS"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| status | Yes | Target TicketStatus |

#### Response

- **200 OK** — TicketResponse with updated status
- **404** — not found
- **409** — transition not allowed by state machine
- **422** — missing/invalid status enum
- **400** — unknown properties / malformed body

---

### POST /api/v1/tickets/{id}/comments

Add a comment.

#### Request

```json
{
  "content": "Investigating root cause",
  "author": "alex"
}
```

| Field | Required | Rules |
|-------|----------|-------|
| content | Yes | 1–2000 chars after trim |
| author | No | max 100 chars |

#### Response

- **201 Created** — CommentResponse
- **404** — ticket not found
- **422** — validation OR ticket is CLOSED/CANCELLED
- **400** — malformed / unknown properties

## 5. Pagination

- `page`: zero-based, default `0`
- `size`: default `20`, maximum `100`
- Response metadata: `page`, `size`, `totalElements`, `totalPages`
- Default sort: newest-first by `createdAt`

## 6. Filtering

- `status`: exact match on ticket status
- `q`: case-insensitive partial match against title and description
- Both may be combined on `GET /api/v1/tickets`

## 7. Versioning

API is versioned via the `/api/v1` path prefix.

## 8. Compatibility

- Local development CORS allows origin `http://localhost:3000`
- No authentication headers required in v1
- Clients must tolerate additional response fields added in future minor
  revisions within v1 only if documented; v1.0 response fields above are
  required
