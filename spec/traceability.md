# Requirements Traceability

| Requirement | Specification | Implementation | Test | Status |
|---|---|---|---|---|
| REQ-001 Create ticket | api-contract.md (POST /tickets); data-model.md; state-machine.md (initial OPEN) | `TicketController`/`TicketService`; `frontend/.../tickets/new` | `TicketApiIT` create 201 OPEN | IMPLEMENTED |
| REQ-002 List tickets | api-contract.md (GET /tickets pagination) | `TicketController.list`; `frontend` list page | `TicketApiIT` pagination metadata | IMPLEMENTED |
| REQ-003 View ticket details | api-contract.md (GET /tickets/{id}); ui-flow.md | `getById`; `frontend/tickets/[id]` | `TicketApiIT` 404 + detail comments | IMPLEMENTED |
| REQ-004 Update ticket fields | api-contract.md (PATCH); edit rules | `TicketService.update`; detail edit form | `TicketApiIT` patch + terminal 422 | IMPLEMENTED |
| REQ-005 Change assignee | api-contract.md (assignee null) | PATCH assignee null; UI empty→null | `TicketApiIT` unassign | IMPLEMENTED |
| REQ-006 Add comment | api-contract.md comments; ui-flow | `addComment`; detail comment form | `TicketApiIT` comment + terminal reject | IMPLEMENTED |
| REQ-007 Search tickets | api-contract.md `q` | repository search; list UI search | `TicketApiIT` q+status | IMPLEMENTED |
| REQ-008 Filter by status | api-contract.md `status` | list filter; UI status select | `TicketApiIT` combined filter | IMPLEMENTED |
| REQ-009 Status transitions | state-machine.md; POST status | `TicketStateMachine` + status API; UI buttons | unit + `TicketApiIT` transitions/409 | IMPLEMENTED |
| REQ-010 Persist data | architecture; Flyway | V1 migration; JPA; PG/H2 profiles | `FlywayMigrationIntegrationTest` (H2); PG restart manual | IMPLEMENTED (PG restart manual) |
| REQ-011 Validate input | error model | `GlobalExceptionHandler` + service validation | API 400/422/409 cases | IMPLEMENTED |
| REQ-012 UI errors and terminal UX | ui-flow.md | ErrorBanner; hide edit/comment/status when terminal | Manual E2E checklist | IMPLEMENTED (manual verify) |
| NFR error/CORS/tech | architecture; ADR-008/009 | CorsConfig; Gradle backend; Next.js frontend | CORS config; error shape tests | IMPLEMENTED |
