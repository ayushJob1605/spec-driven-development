# UI Flow

## 1. User Journeys

### J1 Create ticket

1. Operator opens Create Ticket.
2. Enters title, description, priority; optionally assignee.
3. Submits → `POST /api/v1/tickets`.
4. On success, navigates to ticket detail.
5. On error, shows meaningful messages from error JSON (fieldErrors when present).

### J2 Browse and find tickets

1. Operator opens Ticket List.
2. Optionally enters keyword (`q`) and/or status filter.
3. Paginates through results (default page size 20).
4. Selects a ticket → Ticket Detail.

### J3 Update ticket fields

1. On detail for OPEN / IN_PROGRESS / RESOLVED, operator edits title,
   description, priority, and/or assignee (null to unassign).
2. Submits → `PATCH /api/v1/tickets/{id}`.
3. UI refreshes displayed data or shows errors.

### J4 Change status

1. On detail, UI shows only legal next statuses for current status.
2. Operator selects a transition → `POST /api/v1/tickets/{id}/status`.
3. On 409, show meaningful invalid-transition message.
4. After success, refresh; if now terminal, hide edit/comment actions.

### J5 Add comment

1. On non-terminal detail, operator enters content and optional author.
2. Submits → `POST /api/v1/tickets/{id}/comments`.
3. Comment appears in the thread; errors shown if validation fails.

### J6 View terminal ticket

1. Operator opens CLOSED or CANCELLED ticket.
2. Detail is read-only: no edit form, no comment form, no status actions.
3. Existing fields and comments remain visible.

## 2. Screens

| Screen | Purpose |
|--------|---------|
| Ticket List | Search, status filter, paginated newest-first list |
| Create Ticket | Form for new ticket |
| Ticket Detail | View fields, comments, conditional edit/status/comment |

## 3. Navigation

- List ↔ Create
- List → Detail (by id)
- Detail → List
- Create success → Detail

## 4. User Actions

| Action | Visible when | API |
|--------|--------------|-----|
| Create ticket | Always | POST /tickets |
| Search / filter / page | List | GET /tickets |
| Edit fields | OPEN, IN_PROGRESS, RESOLVED | PATCH /tickets/{id} |
| Unassign | Same as edit | PATCH assignee null |
| Change status | Non-terminal; only legal targets | POST /tickets/{id}/status |
| Add comment | Non-terminal | POST /tickets/{id}/comments |

Terminal (CLOSED, CANCELLED): hide edit, status-change, and comment actions.

## 5. Validation

- Client may provide immediate UX hints (required fields, max lengths).
- Backend validation is authoritative.
- Map `fieldErrors[].field` + `message` onto form fields when present.

## 6. Error States

| HTTP | UI behavior |
|------|-------------|
| 400 | Show message; indicate request could not be processed |
| 404 | Show ticket-not-found; offer return to list |
| 409 | Show invalid transition message from `message` |
| 422 | Show summary `message` and per-field errors |
| Network/unreachable | Show generic connectivity error |

Always prefer backend `message` and `fieldErrors` over generic text when available.

## 7. Loading States

- List: loading indicator while fetching; empty state when no results.
- Detail: loading while fetching ticket; disable submit buttons while
  mutating.
- Avoid duplicate submissions (disable button during in-flight request).
