---
name: build-with-agent-team
description: Build a sprint using Claude Code Agent Teams with tmux split panes. Coordinates backend, frontend, QA, and review agents in parallel using DDD contracts and frozen specifications. Use when you want multiple agents collaborating on sprint execution.
argument-hint: [sprint-plan-or-story] [num-agents]
---

# Build with Agent Team — DDD SDLC Edition

You are coordinating a sprint build using Claude Code Agent Teams. Read the sprint plan or story, determine the right team structure, define contracts from frozen specs, spawn teammates, and orchestrate the build with DDD governance.

## Arguments

- **Sprint plan or story**: `$ARGUMENTS` - Path to sprint plan, story ID (US-XXX), or description of what to build
- **Team size**: Optional number at end - Number of agents (auto-determined if not specified)

## Prerequisites

Before using this skill:
1. **Design phase complete** — HLD, LLD, DDD artifacts exist in `docs/`
2. **Contracts frozen** — `docs/tech-specs/openapi.yaml` and `docs/tech-specs/schema.sql` exist
3. **Sprint planned** — Stories assigned to current sprint in `docs/sprints/`
4. **Agent teams enabled** — `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1` in environment

If prerequisites are not met, guide the user to complete them first.

---

## Step 1: Read the Plan & Context

Read the sprint plan or story. Also read the frozen contracts and DDD artifacts:

**Required reads:**
- Sprint plan: `docs/sprints/sprint-N-plan.md` (or story from `docs/prd/backlog.md`)
- Frozen API contract: `docs/tech-specs/openapi.yaml`
- Frozen DB schema: `docs/tech-specs/schema.sql`
- Context map: `docs/ddd/CONTEXT_MAP.md`
- Aggregate designs: `docs/ddd/aggregate-designs/`
- Domain events: `docs/ddd/domain-events.md`
- SDLC state: `.sdlc/state.json`

Understand:
- What stories are in this sprint?
- Which bounded contexts are affected?
- Which aggregates need implementation?
- What are the frozen API endpoints for these stories?
- What are the frozen DB tables for these stories?
- Are there cross-context interactions requiring sagas?

---

## Step 2: Determine Team Structure

If team size is specified, use that number of agents.

If NOT specified, analyze the sprint scope:

| Sprint Scope | Team Size | Agents |
|-------------|-----------|--------|
| Single context, backend only | 2 | Backend + QA |
| Single context, full-stack | 3 | Backend + Frontend + QA |
| Multi-context, full-stack | 4 | Backend + Frontend + QA + Validator |
| Complex sprint with security | 5 | Backend + Frontend + QA + Security + Validator |

### Agent Role Definitions

For each agent, define using our `.claude/agents/` definitions:

| Agent | Definition | Model | Owns | Does NOT Touch |
|-------|-----------|-------|------|----------------|
| **Backend** | `backend-agent.md` | Sonnet | `backend/src/` | `frontend/`, `docs/`, contracts |
| **Frontend** | `frontend-agent.md` | Sonnet | `frontend/src/app/features/` | `backend/`, `docs/`, contracts |
| **QA** | `qa-agent.md` | Sonnet | `**/test/`, `*.spec.ts`, `e2e/` | Production source code |
| **Security** | `security-agent.md` | Opus | Read-only analysis | ALL (read-only) |
| **Validator** | `validator-agent.md` | Opus | Read-only analysis | ALL (read-only) |

---

## Step 3: Set Up Agent Team

Enable tmux split panes so each agent is visible:

```
teammateMode: "tmux"
```

Verify tmux is available. If not, fall back to in-process mode.

---

## Step 4: Define Contracts from Frozen Specs

**Critical difference from generic agent teams:** Our contracts are ALREADY DEFINED and FROZEN by the Architect Agent. You do NOT author contracts — you EXTRACT them from the frozen specs.

### Extract from openapi.yaml

For each story in the sprint, extract the relevant endpoints:
- Exact URLs (including path parameters)
- HTTP methods
- Request body schemas (exact JSON shapes)
- Response body schemas (exact JSON shapes)
- Status codes (success + error cases)
- Authentication requirements

### Extract from schema.sql

For each story, extract the relevant tables:
- Table definitions (columns, types, constraints)
- Foreign key relationships
- Indexes
- Migration version numbers

### Extract from DDD Artifacts

For each bounded context in the sprint:
- Aggregate root + entities + value objects
- Invariants to enforce
- Domain events to emit
- Repository interface signatures

### Map the Contract Chain (DDD Edition)

```
schema.sql ──► Backend Domain Layer (Aggregate, Repository)
                    │
openapi.yaml ──► Backend API Layer (Controller, DTOs)
                    │
openapi.yaml ──► Frontend Infrastructure Layer (API Service)
                    │
                    ▼
              Frontend Facade → Store → Components
```

### Cross-Cutting Concerns Assignment

| Concern | Owner | Coordinates With |
|---------|-------|-----------------|
| Aggregate invariant enforcement | Backend | Validator |
| DTO ↔ Domain mapping | Backend | Frontend (response shapes) |
| Error response format | Backend | Frontend (error handling) |
| Domain event publishing | Backend | Frontend (if real-time updates) |
| Route guards & auth | Frontend | Backend (JWT validation) |
| Form validation rules | Frontend | Backend (Bean Validation parity) |
| Test coverage thresholds | QA | Backend + Frontend |

### Contract Quality Checklist (DDD Edition)

Before including contracts in agent prompts, verify:
- [ ] Are API URLs extracted exactly from openapi.yaml (including path params)?
- [ ] Are request/response shapes the exact JSON from openapi.yaml schemas?
- [ ] Are aggregate invariants listed from aggregate design docs?
- [ ] Are domain events specified with exact payload shapes?
- [ ] Are error responses specified per openapi.yaml (400, 404, 422 bodies)?
- [ ] Are DB table definitions exact from schema.sql?
- [ ] Is the bounded context clearly identified from CONTEXT_MAP.md?

---

## Step 5: Spawn All Agents in Parallel

Enter **Delegate Mode** (Shift+Tab). You coordinate — you do NOT implement.

### Backend Agent Spawn Prompt

```
You are the Backend Developer agent for Sprint N.

## Your Ownership
- You own: `backend/src/main/java/` and `backend/src/test/java/`
- Do NOT touch: `frontend/`, `docs/`, `docs/tech-specs/openapi.yaml`, `docs/tech-specs/schema.sql`

## What You're Building
[Stories from sprint plan relevant to backend]

## Bounded Context
[Context name from CONTEXT_MAP.md]

## Frozen Contracts

### Database Schema (from schema.sql)
[Exact table definitions for this context]

### API Contract (from openapi.yaml)
[Exact endpoints, request/response shapes for this context's stories]

### Aggregate Design (from aggregate-designs/)
[Aggregate root, entities, value objects, invariants, domain events]

## DDD Layer Discipline
- domain/model/ — Entities, Value Objects, Aggregates
- domain/event/ — Domain Events (past tense naming)
- domain/repository/ — Repository interfaces (ports)
- application/service/ — Use case orchestration (thin, @Transactional here)
- infrastructure/persistence/ — JPA entities, Repository implementations
- api/controller/ — REST controllers (no business logic)
- api/dto/ — Request/Response DTOs (never expose domain entities)

## STOP Rules
- ✋ HALT if you need cross-context repository access
- ✋ HALT if you need to modify openapi.yaml or schema.sql
- ✋ HALT if an invariant seems wrong — message the lead
- ✋ HALT if you need to share entities across contexts

## Before Reporting Done
1. `mvn compile` — zero errors
2. `mvn test` — all tests pass
3. Verify: no business logic in controllers
4. Verify: DTOs used for API responses (no entity leakage)
5. Verify: invariants enforced in aggregate
Do NOT report done until all validations pass.
```

### Frontend Agent Spawn Prompt

```
You are the Frontend Developer agent for Sprint N.

## Your Ownership
- You own: `frontend/src/app/features/[feature-name]/`
- Do NOT touch: `backend/`, `docs/`, contracts, other feature modules

## What You're Building
[Stories from sprint plan relevant to frontend]

## Frozen Contract (from openapi.yaml)
[Exact endpoints this feature calls, request/response shapes]

## Feature Module Structure
frontend/src/app/features/[feature-name]/
├── pages/              # Smart/container components
├── components/         # Presentational (dumb) components
├── application/        # Facade (orchestration layer)
│   └── [feature].facade.ts
├── infrastructure/     # API service (HTTP calls only)
│   └── [feature]-api.service.ts
├── state/              # NgRx store
├── models/             # DTOs + ViewModels
└── [feature-name].routes.ts

## Layer Discipline
- Components: Presentation only, @Input/@Output, OnPush
- Facade: Orchestration, calls API service, updates store
- API Service: HTTP calls only, maps to openapi.yaml exactly
- Models: Separate DTO from ViewModel, map in facade

## STOP Rules
- ✋ HALT if you need cross-feature imports
- ✋ HALT if you need to modify openapi.yaml
- ✋ HALT if you need API calls in components (use facade)
- ✋ HALT if you need global mutable state

## Before Reporting Done
1. `ng build` — zero errors
2. `ng test` — all tests pass
3. Verify: no API calls in components
4. Verify: DTOs mapped to ViewModels in facade
5. Verify: lazy loading configured in routes
Do NOT report done until all validations pass.
```

### QA Agent Spawn Prompt

```
You are the QA & Test agent for Sprint N.

## Your Ownership
- You own: `backend/src/test/java/`, `frontend/src/app/**/*.spec.ts`, `e2e/`
- Do NOT touch: Production source code

## What You're Testing
[Stories from sprint plan — test requirements]

## Test Strategy
- Backend unit tests: JUnit5 + Mockito (mock repositories)
- Backend integration tests: @SpringBootTest + TestContainers
- Frontend component tests: Jest + Angular Testing Library
- Frontend facade tests: Mock API service
- E2E tests: Playwright (critical flows only)

## Coverage Thresholds
- Backend: >= 80% line coverage
- Frontend: >= 70% line coverage
- Critical paths: 100% branch coverage

## Contracts to Test Against
[openapi.yaml endpoints — verify HTTP status codes match]

## Before Reporting Done
1. All unit tests pass
2. Coverage thresholds met
3. Integration tests pass
4. E2E tests pass for critical flows
Do NOT report done until all validations pass.
```

---

## Step 6: Facilitate Collaboration

All agents work in parallel. Your job as lead:

### During Implementation

- **Relay messages** between agents when they flag contract issues
- **Mediate contract deviations** — if an agent needs to deviate, evaluate, update, notify all
- **Unblock** agents waiting on decisions
- **Track progress** through shared task list (Ctrl+T)
- **Enforce STOP rules** — if an agent hits a STOP rule, they message you

### Pre-Completion Contract Verification

Before any agent reports "done", verify contract alignment:
- "Backend: what exact curl commands test each endpoint?"
- "Frontend: what exact fetch URLs and request bodies are you using?"
- Compare and flag mismatches BEFORE integration testing

### Cross-Review (DDD Edition)

| Reviewer | Reviews | Looking For |
|----------|---------|-------------|
| Frontend | Backend API | Usability, response shape match, error handling |
| Backend | Frontend API calls | Correct endpoint usage, auth headers |
| QA | Both | Coverage gaps, untested edge cases |

---

## Step 7: Validation

### Agent-Level Validation (each agent validates own domain)

See "Before Reporting Done" in each spawn prompt above.

### Lead-Level Validation (End-to-End)

After ALL agents return, you validate the integrated system:

1. **Can the system start?**
   - Backend: `mvn spring-boot:run` (or `gradle bootRun`)
   - Frontend: `ng serve`
   - No startup errors

2. **Does the happy path work?**
   - Walk through each story's acceptance criteria
   - Frontend → Backend → Database flow works

3. **Do integrations connect?**
   - Frontend API calls reach backend endpoints
   - Backend queries return expected data
   - Domain events fire correctly

4. **DDD Governance Check**
   - No cross-context violations
   - No entity leakage
   - Invariants enforced
   - Contract versions unchanged

5. **Coverage Gate**
   - Backend >= 80%
   - Frontend >= 70%

If validation fails:
- Identify which agent's domain contains the issue
- Re-spawn that specific agent with the issue description
- Re-run validation after fix

---

## Collaboration Anti-Patterns (Prevent These)

| Anti-Pattern | Why It Fails | Prevention |
|---|---|---|
| Parallel spawn without contracts | Agents diverge on URLs, response shapes | Extract contracts from frozen specs FIRST |
| Fully sequential spawning | Defeats agent team parallelism | Spawn all with contracts simultaneously |
| "Tell agents to coordinate" | They won't reliably | Lead relays ALL cross-agent communication |
| Lead implements code | Bottleneck, defeats delegation | Stay in Delegate Mode |
| Agents modify frozen contracts | Breaks DDD discipline | STOP rules + lead enforcement |
| Agents touch each other's files | Merge conflicts | Clear ownership boundaries |

---

## Definition of Done

The sprint build is complete when:
1. ✅ All agents report done with validations passing
2. ✅ Contract verification shows no mismatches
3. ✅ Cross-review feedback addressed
4. ✅ Lead E2E validation passes
5. ✅ Coverage thresholds met (Backend >= 80%, Frontend >= 70%)
6. ✅ No DDD governance violations
7. ✅ All story acceptance criteria met

---

## Execute

Now read the sprint plan/story and begin:

1. Read plan + frozen contracts + DDD artifacts
2. Determine team size and agent roles
3. Extract contracts from frozen openapi.yaml + schema.sql + aggregate designs
4. Map cross-cutting concerns and assign ownership
5. Enter Delegate Mode (Shift+Tab)
6. Spawn all agents in parallel with contracts and validation checklists
7. Monitor, relay messages, mediate contract deviations
8. Run contract diff before integration
9. When all agents return, run E2E validation
10. If validation fails, re-spawn relevant agent with specific issue
11. Confirm build meets sprint/story acceptance criteria
