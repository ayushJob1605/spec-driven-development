# State Machine

## 1. States

| State | Terminal | Description |
|-------|----------|-------------|
| OPEN | No | Initial status of every new ticket |
| IN_PROGRESS | No | Work has started |
| RESOLVED | No | Work completed; awaiting close |
| CLOSED | Yes | Successfully completed and closed |
| CANCELLED | Yes | Abandoned; no further work |

Initial state on create: **OPEN**.

## 2. Events

Status changes occur only via the dedicated status API
(`POST /api/v1/tickets/{id}/status`).

| Event | Target status |
|-------|---------------|
| TRANSITION_TO_IN_PROGRESS | IN_PROGRESS |
| TRANSITION_TO_RESOLVED | RESOLVED |
| TRANSITION_TO_CLOSED | CLOSED |
| TRANSITION_TO_CANCELLED | CANCELLED |

## 3. Valid Transitions

Exact allowed set (no others):

| From | To | Event |
|------|----|-------|
| OPEN | IN_PROGRESS | TRANSITION_TO_IN_PROGRESS |
| IN_PROGRESS | RESOLVED | TRANSITION_TO_RESOLVED |
| RESOLVED | CLOSED | TRANSITION_TO_CLOSED |
| OPEN | CANCELLED | TRANSITION_TO_CANCELLED |
| IN_PROGRESS | CANCELLED | TRANSITION_TO_CANCELLED |

```text
OPEN ──► IN_PROGRESS ──► RESOLVED ──► CLOSED
 │            │
 └──► CANCELLED ◄─────────┘
```

## 4. Invalid Transitions

Any transition not listed in section 3 is invalid and must be rejected with
HTTP **409**.

Examples (non-exhaustive):

- CLOSED → OPEN
- RESOLVED → OPEN
- CANCELLED → OPEN
- OPEN → RESOLVED
- OPEN → CLOSED
- IN_PROGRESS → CLOSED
- IN_PROGRESS → OPEN
- RESOLVED → IN_PROGRESS
- RESOLVED → CANCELLED
- CLOSED → any status
- CANCELLED → any status
- Same-state requests (e.g. OPEN → OPEN)

## 5. Transition Rules

1. Backend is the authority for transition legality.
2. UI may hide illegal options but must not be relied on for enforcement.
3. Field-update APIs must not accept or apply status changes.
4. Terminal tickets (CLOSED, CANCELLED) reject:
   - status transitions
   - field updates
   - new comments
5. Successful transition updates `status` and `updatedAt` only (v1).

## 6. Side Effects

On successful transition:

- Set ticket `status` to the target value
- Refresh ticket `updatedAt`

No other side effects in v1 (no auto-comments, notifications, or assignee changes).
