---
name: lld
description: Low-Level Design generator. Creates class diagrams, sequence diagrams, API contracts, database schemas, package structures, and interface definitions. Use after HLD and DDD are complete.
argument-hint: [project-name]
user-invocable: true
---

# Low-Level Design (LLD) Generator

You are a Senior Software Designer. Generate implementation-ready low-level design documents that developers can directly code from.

## Current SDLC State
!`cat .sdlc/state.json 2>/dev/null | python3 -c "import sys,json; s=json.load(sys.stdin); print(f'Project: {s[\"project\"]}  |  Phase: {s[\"currentPhase\"]}')" 2>/dev/null || echo "Project: Not initialized"`

## Context — HLD
!`cat docs/architecture/hld/system-architecture.md 2>/dev/null | head -80 || echo "HLD not found. Run /hld first."`

## Context — DDD Artifacts
!`ls docs/ddd/ 2>/dev/null || echo "No DDD artifacts yet. Consider running /ddd-architect first."`

## Context — PRD
!`cat docs/prd/prd.md 2>/dev/null | head -50 || echo "PRD not found."`

## Arguments
- `$1` = Project name

## Instructions

### Step 1: Read All Context
Read these files completely:
- `docs/architecture/hld/system-architecture.md` — System architecture
- `docs/ddd/` — All DDD artifacts (if exist)
- `docs/prd/prd.md` — Requirements
- `docs/prd/roadmap.md` — Roadmap (if exists)

### Step 2: Generate LLD Documents

#### 2a. Create `docs/architecture/lld/class-design.md`

```markdown
# [Project Name] — Class Design

## Package Structure

### Backend (Spring Boot)
```
com.example.[project]/
├── [bounded-context-1]/
│   ├── domain/
│   │   ├── model/
│   │   │   ├── [AggregateRoot].java          # Aggregate root entity
│   │   │   ├── [Entity].java                 # Child entities
│   │   │   └── [ValueObject].java            # Value objects
│   │   ├── event/
│   │   │   └── [DomainEvent].java            # Domain events
│   │   ├── repository/
│   │   │   └── [Aggregate]Repository.java    # Repository interface
│   │   ├── service/
│   │   │   └── [Domain]Service.java          # Domain service
│   │   └── exception/
│   │       └── [Domain]Exception.java        # Domain exceptions
│   ├── application/
│   │   ├── command/
│   │   │   ├── Create[Entity]Command.java    # Write DTOs
│   │   │   └── Update[Entity]Command.java
│   │   ├── query/
│   │   │   └── [Entity]Query.java            # Read DTOs
│   │   ├── service/
│   │   │   └── [Entity]ApplicationService.java
│   │   └── mapper/
│   │       └── [Entity]Mapper.java           # MapStruct mapper
│   ├── infrastructure/
│   │   ├── persistence/
│   │   │   ├── [Entity]JpaEntity.java        # JPA entity
│   │   │   ├── [Entity]JpaRepository.java    # Spring Data repo
│   │   │   └── [Entity]RepositoryImpl.java   # Port implementation
│   │   └── messaging/
│   │       ├── [Event]Publisher.java          # Kafka producer
│   │       └── [Event]Consumer.java          # Kafka consumer
│   └── api/
│       ├── controller/
│       │   └── [Entity]Controller.java       # REST controller
│       ├── dto/
│       │   ├── [Entity]Request.java          # API request
│       │   └── [Entity]Response.java         # API response
│       └── advice/
│           └── [Context]ExceptionHandler.java
```

### Frontend (Angular)
```
src/app/features/[feature]/
├── components/
│   ├── [feature]-list/
│   │   ├── [feature]-list.component.ts
│   │   ├── [feature]-list.component.html
│   │   └── [feature]-list.component.spec.ts
│   ├── [feature]-detail/
│   ├── [feature]-form/
│   └── [feature]-card/
├── services/
│   └── [feature].service.ts
├── models/
│   └── [feature].model.ts
├── store/
│   ├── [feature].actions.ts
│   ├── [feature].reducer.ts
│   ├── [feature].selectors.ts
│   ├── [feature].effects.ts
│   └── [feature].state.ts
└── [feature].routes.ts
```

## Class Diagrams

For each bounded context, generate class diagrams showing:

### [Bounded Context Name] — Domain Model
```
┌──────────────────────────────────┐
│       [AggregateRoot]            │ ◄── AGGREGATE ROOT
├──────────────────────────────────┤
│ - id: UUID                       │
│ - [field]: [Type]                │
│ - [field]: [Type]                │
│ - status: [StatusEnum]           │
│ - createdAt: Instant             │
│ - updatedAt: Instant             │
├──────────────────────────────────┤
│ + create([params]): Self         │
│ + update([params]): void         │
│ + [businessMethod](): [Return]   │
│ + validate(): void               │
└──────────┬───────────────────────┘
           │ 1..*
           ▼
┌──────────────────────────────────┐
│       [ChildEntity]              │
├──────────────────────────────────┤
│ - id: UUID                       │
│ - [field]: [Type]                │
├──────────────────────────────────┤
│ + [methods]                      │
└──────────────────────────────────┘

┌──────────────────────────────────┐
│       [ValueObject]              │ ◄── VALUE OBJECT (immutable)
├──────────────────────────────────┤
│ - [field]: [Type]                │
│ - [field]: [Type]                │
├──────────────────────────────────┤
│ + of([params]): Self             │
│ + equals(other): boolean         │
└──────────────────────────────────┘
```

## Interface Definitions

### Repository Interfaces
```java
public interface [Entity]Repository {
    Optional<[Entity]> findById(UUID id);
    [Entity] save([Entity] entity);
    void delete(UUID id);
    Page<[Entity]> findAll(Pageable pageable);
    List<[Entity]> findBy[Criteria]([Type] criteria);
}
```

### Service Interfaces
```java
public interface [Entity]Service {
    [Entity]Response create(Create[Entity]Command command);
    [Entity]Response update(UUID id, Update[Entity]Command command);
    [Entity]Response findById(UUID id);
    PageResponse<[Entity]Response> findAll(int page, int size);
    void delete(UUID id);
}
```

## Design Patterns Applied

| Pattern | Where | Purpose |
|---------|-------|---------|
| Builder | Domain entities with 4+ fields | Clean object construction |
| Factory | Complex entity creation | Encapsulate creation logic |
| Repository | Data access | Abstract persistence |
| Strategy | [Business rule variations] | Pluggable algorithms |
| Observer | Domain events | Loose coupling between contexts |
| Specification | Complex queries | Composable query criteria |
```

#### 2b. Create `docs/architecture/lld/sequence-diagrams.md`

Generate sequence diagrams for key use cases:

```markdown
# [Project Name] — Sequence Diagrams

## Use Case: [Use Case Name]

```
Actor          Frontend        API Gateway      Service          Database        Kafka
  │               │                │               │                │              │
  │──[action]────▶│                │               │                │              │
  │               │──HTTP POST────▶│               │                │              │
  │               │                │──validate─────▶               │              │
  │               │                │               │──INSERT───────▶│              │
  │               │                │               │◄──OK───────────│              │
  │               │                │               │──publish──────────────────────▶│
  │               │                │◄──201─────────│                │              │
  │               │◄──response─────│               │                │              │
  │◄──display─────│                │               │                │              │
```

[Repeat for 3-5 key use cases: CRUD, authentication, business workflow, etc.]
```

#### 2c. Create `docs/architecture/lld/api-contracts.md`

```markdown
# [Project Name] — API Contracts

## API Overview
Base URL: `/api/v1`
Authentication: Bearer JWT

## Endpoints

### [Resource] API

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | /api/v1/[resources] | List all | Yes |
| GET | /api/v1/[resources]/{id} | Get by ID | Yes |
| POST | /api/v1/[resources] | Create | Yes |
| PUT | /api/v1/[resources]/{id} | Update | Yes |
| DELETE | /api/v1/[resources]/{id} | Delete | Admin |

### Request/Response Schemas

#### Create [Resource]
Request:
```json
{
  "field1": "string (required)",
  "field2": "number (required)",
  "field3": "string (optional)"
}
```

Response (201):
```json
{
  "id": "uuid",
  "field1": "string",
  "field2": "number",
  "createdAt": "ISO-8601"
}
```

### Error Responses
```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    { "field": "field1", "message": "must not be blank" }
  ],
  "timestamp": "ISO-8601",
  "traceId": "uuid"
}
```
```

### Step 3: Update SDLC State
Update `.sdlc/state.json`:
- Add all LLD artifacts to `phases.design.artifacts`
- Update `updatedAt`

### Step 4: Present Summary
```
Low-Level Design for [Project Name] has been generated.

Artifacts:
- docs/architecture/lld/class-design.md
- docs/architecture/lld/sequence-diagrams.md
- docs/architecture/lld/api-contracts.md

Classes designed: [N] entities, [N] services, [N] controllers
API endpoints: [N] endpoints across [N] resources
Sequence diagrams: [N] key use cases

Next Steps:
1. Review the LLD documents
2. Run /tech-specs [project] [stack] for DB schemas and OpenAPI specs
3. Run /sdlc next to advance to Development phase
```

---

## Agent Delegation

This skill delegates to the **Architect Agent** (`.claude/agents/architect-agent.md`).

### When using Agent Teams (recommended for parallel design):
The Architect Agent runs on **Opus** model. LLD can run in parallel with DDD (both depend on HLD):
- HLD must complete first
- `/lld` ║ `/ddd-architect` → Two Architect Agents in parallel

### When using Task tool:
```
Use the Task tool to spawn an architect-agent subagent:
- subagent_type: "general-purpose"
- model: "opus"
- Provide: project name, HLD content, DDD artifacts (if available)
- Agent produces: LLD documents in docs/architecture/lld/
```

### Dual Diagram Output:
Class diagrams and sequence diagrams generated in both ASCII and Mermaid format. Standalone Mermaid files saved to `docs/architecture/lld/diagrams/*.mmd`.

### Agent produces:
- `docs/architecture/lld/class-design.md` — Class diagrams, package structure
- `docs/architecture/lld/sequence-diagrams.md` — Sequence diagrams for key use cases
- `docs/architecture/lld/api-contracts.md` — API contract definitions
- `docs/architecture/lld/diagrams/*.mmd` — Mermaid diagram files

### Agent reads:
- `docs/architecture/hld/system-architecture.md`, `docs/ddd/`, `docs/prd/prd.md`
