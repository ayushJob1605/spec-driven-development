# Graph Report - spec-java-project  (2026-09-20)

## Corpus Check
- 68 files · ~16,483 words
- Verdict: corpus is large enough that graph structure adds value.
- Unclassified: 16 file(s) not represented in the graph (top: .mdc 7, (none) 3, .example 1)

## Summary
- 756 nodes · 1170 edges · 32 communities (28 shown, 4 thin omitted)
- Extraction: 96% EXTRACTED · 4% INFERRED · 0% AMBIGUOUS · INFERRED: 42 edges (avg confidence: 0.84)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- =========================
- TicketService.java
- Architecture
- Ticket
- Test Strategy
- YYYY-MM-DD — Title
- Output
- Output
- Output
- Data Model
- UI Flow
- State Machine
- Documentation Skill
- Decision Log
- [id]/page.tsx
- API Contract
- .update
- 6. Business Rules
- Implementation Plan / Tasks
- org.junit.jupiter.api.Test
- Backend (Support Ticket API)
- SupportTicketApplication.java
- gradlew
- FieldErrorItem
- package.json
- CorsConfig.java
- ErrorCodes
- compilerOptions
- Backend (Support Ticket API)
- Support Ticket Frontend

## God Nodes (most connected - your core abstractions)
1. `=========================` - 122 edges
2. `Ticket` - 37 edges
3. `Comment` - 28 edges
4. `TicketStatus` - 24 edges
5. `Implementation Plan / Tasks` - 22 edges
6. `TicketResponse` - 19 edges
7. `UpdateTicketRequest` - 19 edges
8. `TicketApiIT` - 19 edges
9. `Priority` - 18 edges
10. `TicketService` - 18 edges

## Surprising Connections (you probably didn't know these)
- `T04 — Error model + global exception handling + JSON unknown properties` --references--> `ErrorResponse`  [INFERRED]
  spec/plan.md → backend/src/main/java/com/supportticket/api/ErrorResponse.java
- `T07 — Get ticket by ID API` --references--> `TicketResponse`  [INFERRED]
  spec/plan.md → backend/src/main/java/com/supportticket/api/dto/TicketResponse.java
- `Requirements Traceability` --references--> `GlobalExceptionHandler`  [INFERRED]
  spec/traceability.md → backend/src/main/java/com/supportticket/api/GlobalExceptionHandler.java
- `Requirements Traceability` --references--> `FlywayMigrationIntegrationTest`  [INFERRED]
  spec/traceability.md → backend/src/test/java/com/supportticket/FlywayMigrationIntegrationTest.java
- `Requirements Traceability` --references--> `TicketApiIT`  [INFERRED]
  spec/traceability.md → backend/src/test/java/com/supportticket/api/TicketApiIT.java

## Import Cycles
- None detected.

## Communities (32 total, 4 thin omitted)

### Community 0 - "========================="
Cohesion: 0.02
Nodes (120): =========================, =========================, =========================, =========================, =========================, =========================, =========================, ========================= (+112 more)

### Community 1 - "TicketService.java"
Cohesion: 0.07
Nodes (36): CommentResponse, CreateCommentRequest, CreateTicketRequest, PageResponse, StatusChangeRequest, TicketResponse, TicketController, TicketMapper (+28 more)

### Community 2 - "Architecture"
Cohesion: 0.14
Nodes (13): 10. Deployment Considerations, 11. Architectural Decisions, 12. Open Questions, 1. Context, 2. Architectural Goals, 3. System Context, 4. Components, 5. Component Responsibilities (+5 more)

### Community 3 - "Ticket"
Cohesion: 0.05
Nodes (19): arraylist, Comment, Ticket, CommentRepository, cascadetype, column, enumerated, enumtype (+11 more)

### Community 4 - "Test Strategy"
Cohesion: 0.17
Nodes (11): 10. CI Verification, 1. Testing Goals, 2. Test Pyramid, 3. Unit Testing, 4. Integration Testing, 5. API Testing, 6. End-to-End Testing, 7. Security Testing (+3 more)

### Community 5 - "YYYY-MM-DD — Title"
Cohesion: 0.18
Nodes (10): AI Mistakes Identified, AI Result, Decision, Entries, Follow-up, Human Review, Objective, Prompt (+2 more)

### Community 6 - "Output"
Cohesion: 0.20
Nodes (9): AI Mistakes, AI Mistakes Identified, Compare, Output, Recommended Fixes, Review, Review Code, Specification Deviations (+1 more)

### Community 7 - "Output"
Cohesion: 0.20
Nodes (9): AI Mistakes, Assumptions, Findings, Output, Questions, Review, Review Specification, Specification Changes (+1 more)

### Community 8 - "Output"
Cohesion: 0.22
Nodes (8): Generate Tests, Generated Tests, Output, Process, Remaining Gaps, Requirement Coverage, Test Quality, Test Scenarios

### Community 9 - "Data Model"
Cohesion: 0.18
Nodes (10): 1. Entities, 2. Relationships, 3. Constraints, 4. Indexes, 5. Lifecycle, 6. Data Integrity Rules, 7. Migration Considerations, Comment (+2 more)

### Community 10 - "UI Flow"
Cohesion: 0.13
Nodes (14): 1. User Journeys, 2. Screens, 3. Navigation, 4. User Actions, 5. Validation, 6. Error States, 7. Loading States, J1 Create ticket (+6 more)

### Community 11 - "State Machine"
Cohesion: 0.25
Nodes (7): 1. States, 2. Events, 3. Valid Transitions, 4. Invalid Transitions, 5. Transition Rules, 6. Side Effects, State Machine

### Community 12 - "Documentation Skill"
Cohesion: 0.40
Nodes (5): Documentation Skill, Process, Purpose, Rules, When to Use

### Community 13 - "Decision Log"
Cohesion: 0.17
Nodes (11): ADR-001 Next.js frontend, ADR-002 PostgreSQL for dev/demo, H2 for tests, Flyway migrations, ADR-003 Dedicated status transition API, ADR-004 No authentication or authorization in v1, ADR-005 Error HTTP mapping and JSON shape, ADR-006 Identifiers, pagination, and list semantics, ADR-007 No optimistic locking in v1, ADR-008 Local CORS origin (+3 more)

### Community 14 - "[id]/page.tsx"
Cohesion: 0.13
Nodes (36): TicketListPage(), TicketDetailPage(), onAddComment(), onSave(), onTransition(), CreateTicketPage(), onSubmit(), ErrorBanner() (+28 more)

### Community 15 - "API Contract"
Cohesion: 0.06
Nodes (34): 1. API Conventions, 2. Authentication, 3. Error Model, 4. Endpoints, 5. Pagination, 6. Filtering, 7. Versioning, 8. Compatibility (+26 more)

### Community 16 - ".update"
Cohesion: 0.12
Nodes (8): UpdateTicketRequest, Priority, CRITICAL, HIGH, LOW, MEDIUM, com.fasterxml.jackson.annotation.JsonSetter, org.springframework.web.bind.annotation.PatchMapping

### Community 17 - "6. Business Rules"
Cohesion: 0.04
Nodes (46): 10. Acceptance Criteria, 1. Purpose, 2. Scope, 3. Actors, 4. Functional Requirements, 5. Non-Functional Requirements, 6. Business Rules, 7. Constraints (+38 more)

### Community 18 - "Implementation Plan / Tasks"
Cohesion: 0.09
Nodes (22): Coverage map, Implementation order (reviewable increments), Implementation Plan / Tasks, Specification gaps that could block implementation, T01 — Backend project scaffold, T02 — Database schema (Flyway) + persistence model, T03 — Domain enums + state machine module, T04 — Error model + global exception handling + JSON unknown properties (+14 more)

### Community 19 - "org.junit.jupiter.api.Test"
Cohesion: 0.05
Nodes (42): assertthat, autowired, TicketStateMachine, EchoRequest, EchoResponse, MalformedRequestErrorIT, TicketApiIT, TicketStateMachineTest (+34 more)

### Community 20 - "Backend (Support Ticket API)"
Cohesion: 0.40
Nodes (4): Backend (Support Ticket API), Requirements, Run application (T01 scaffold), Run tests

### Community 21 - "SupportTicketApplication.java"
Cohesion: 0.50
Nodes (3): SupportTicketApplication, org.springframework.boot.autoconfigure.SpringBootApplication, springapplication

### Community 22 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 25 - "FieldErrorItem"
Cohesion: 0.15
Nodes (17): ErrorResponse, FieldErrorItem, GlobalExceptionHandler, InvalidTransitionException, ResourceNotFoundException, ValidationException, collectors, invalidformatexception (+9 more)

### Community 26 - "package.json"
Cohesion: 0.05
Nodes (39): compat, __dirname, eslintConfig, __filename, nextConfig, dependencies, next, react (+31 more)

### Community 27 - "CorsConfig.java"
Cohesion: 0.24
Nodes (9): CorsConfig, JacksonConfig, corsconfiguration, deserializationfeature, org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer, org.springframework.context.annotation.Bean, org.springframework.context.annotation.Configuration, org.springframework.web.filter.CorsFilter (+1 more)

### Community 29 - "compilerOptions"
Cohesion: 0.11
Nodes (18): compilerOptions, allowJs, esModuleInterop, incremental, isolatedModules, jsx, lib, module (+10 more)

### Community 30 - "Backend (Support Ticket API)"
Cohesion: 0.29
Nodes (6): Backend (Support Ticket API), Frontend (Next.js), Notes, Run, Setup, Test

## Knowledge Gaps
- **356 isolated node(s):** `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`, `OPEN` (+351 more)
  These have ≤1 connection - possible missing edges or undocumented components. (Counts symbols only; 447 node(s) total have ≤1 connection when file, concept and rationale nodes are included.)
- **4 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `=========================` connect `=========================` to `Documentation Skill`?**
  _High betweenness centrality (0.027) - this node is a cross-community bridge._
- **Why does `TicketResponse` connect `TicketService.java` to `.update`, `Implementation Plan / Tasks`, `org.junit.jupiter.api.Test`?**
  _High betweenness centrality (0.026) - this node is a cross-community bridge._
- **Why does `Ticket` connect `Ticket` to `.update`, `TicketService.java`?**
  _High betweenness centrality (0.025) - this node is a cross-community bridge._
- **What connects `LOW`, `MEDIUM`, `HIGH` to the rest of the system?**
  _356 weakly-connected nodes found - possible documentation gaps or missing edges._
- **Should `=========================` be split into smaller, more focused modules?**
  _Cohesion score 0.016666666666666666 - nodes in this community are weakly interconnected._
- **Should `TicketService.java` be split into smaller, more focused modules?**
  _Cohesion score 0.0650103519668737 - nodes in this community are weakly interconnected._
- **Should `Architecture` be split into smaller, more focused modules?**
  _Cohesion score 0.14285714285714285 - nodes in this community are weakly interconnected._