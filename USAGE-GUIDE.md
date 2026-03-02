# AI-Native SDLC Factory — Usage Guide

## Table of Contents
1. [Quick Start](#quick-start)
2. [Architecture Overview](#architecture-overview)
3. [Multi-Agent Architecture](#multi-agent-architecture)
4. [Complete Skill Reference](#complete-skill-reference)
5. [Sample Project Walkthrough](#sample-project-walkthrough)
6. [Agent Teams Walkthrough](#agent-teams-walkthrough)
7. [Quality Gates](#quality-gates)
8. [Tips & Best Practices](#tips--best-practices)

---

## Quick Start

### Prerequisites
- Claude Code CLI installed and configured
- GitHub CLI (`gh`) installed and authenticated (`gh auth login`)
- Node.js 18+ and npm (for Angular)
- Java 17+ and Maven 3.9+ (for Spring Boot)
- Git configured with your credentials
- **tmux** (recommended for Agent Teams split-pane view): `brew install tmux`

### Create a New Project

```bash
# From the kit directory:
bash create-project.sh my-awesome-app ~/projects
cd ~/projects/my-awesome-app
```

This copies all kit files, initializes git, configures agent teams, and creates the initial commit.

### Launch SDLC Factory (Each Session)

```bash
# With Agent Teams (recommended — requires tmux):
bash scripts/start-sdlc.sh

# Jump to a specific phase:
bash scripts/start-sdlc.sh design
```

### Initialize the SDLC

Inside Claude Code:
```
/sdlc init my-awesome-app
```

Follow the guided workflow or invoke phases independently.

### Alternative: Basic Mode (without Agent Teams)
```bash
cd /path/to/my-project
claude
> /sdlc init my-awesome-app
```

---

## Architecture Overview

```
┌──────────────────────────────────────────────────────────────────────────┐
│                    AI-NATIVE SDLC FACTORY                                │
├──────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  /sdlc (Master Orchestrator)                                             │
│    │                                                                     │
│    ├─ Phase 1: IDEATION ──────── /ideate                                 │
│    ├─ Phase 2: REQUIREMENTS ──── /enterprise-prd                         │
│    ├─ Phase 3: PROJECT SETUP ─── /github-project-setup + /agile-backlog  │
│    │                              + /roadmap                             │
│    ├─ Phase 4: DESIGN ────────── /hld + /lld + /ddd-architect            │
│    │                              + /tech-specs                          │
│    ├─ Phase 5: DEVELOPMENT ───── /scrum-sprint + /develop                │
│    ├─ Phase 6: TESTING ──────── /test-suite + /tdd-helper                │
│    ├─ Phase 7: SECURITY ──────── /security-review + /compliance-checklist│
│    ├─ Phase 8: CODE REVIEW ───── /pr-review                              │
│    └─ Phase 9: RELEASE ──────── /release                                 │
│                                                                          │
│  State: .sdlc/state.json    Docs: docs/    Rules: .claude/rules/         │
└──────────────────────────────────────────────────────────────────────────┘
```

### Key Concepts

- **Hybrid Orchestration:** Use `/sdlc next` for guided flow, or invoke any skill independently
- **Quality Gates:** Each phase has a gate that must pass before the next phase begins
- **State Tracking:** `.sdlc/state.json` tracks progress, artifacts, and phase transitions
- **Artifact Generation:** All documents are saved to `docs/` in structured subdirectories
- **GitHub Integration:** Repos, issues, milestones, PRs are managed via `gh` CLI

---

## Multi-Agent Architecture

The SDLC Factory uses 11 specialized agents, each with its own persona, model assignment, and tool restrictions. Skills are the user-facing interface; agents are the workers.

### Agent Overview

```
┌────────────────────────────────────────────────────────────────────────┐
│                      AI-NATIVE SDLC FACTORY (v2)                       │
│                                                                        │
│  ┌─────────────── THINKERS (Opus) ──────────────────────────────┐     │
│  │  research-agent    product-agent    architect-agent           │     │
│  │  security-agent    review-agent     validator-agent           │     │
│  │  Deep analysis, reasoning, governance, audit                  │     │
│  └───────────────────────────────────────────────────────────────┘     │
│                                                                        │
│  ┌─────────────── BUILDERS (Sonnet) ────────────────────────────┐     │
│  │  backend-agent     frontend-agent   qa-agent                  │     │
│  │  Code generation, test writing, implementation                │     │
│  └───────────────────────────────────────────────────────────────┘     │
│                                                                        │
│  ┌─────────────── EXECUTORS (Haiku) ────────────────────────────┐     │
│  │  devops-agent      memory-agent                               │     │
│  │  Fast CLI operations, context compression                     │     │
│  └───────────────────────────────────────────────────────────────┘     │
└────────────────────────────────────────────────────────────────────────┘
```

### Agent Details

| Agent | Model | Role | Key Capabilities |
|-------|-------|------|-----------------|
| **research-agent** | Opus | Market research, interactive discovery | AskUserQuestion for 3-phase questionnaire, WebSearch |
| **product-agent** | Opus | PRD, epics, stories, roadmap | Capability-driven PRD, story slicing, AskUserQuestion |
| **architect-agent** | Opus | HLD, LLD, DDD, contracts | Dual ASCII+Mermaid diagrams, contract freeze, saga design |
| **backend-agent** | Sonnet | Spring Boot DDD implementation | Layer discipline, aggregate rules, STOP rules |
| **frontend-agent** | Sonnet | Angular 17+ feature modules | Facade pattern, NgRx, lazy loading, STOP rules |
| **qa-agent** | Sonnet | Test pyramid management | JUnit5+Mockito, Jest, Playwright, coverage reports |
| **security-agent** | Opus | OWASP Top 10 audit | Credential scan, input validation, auth review (read-only) |
| **devops-agent** | Haiku | Git, gh CLI, CI/CD, releases | PR creation, tagging, GitHub project setup |
| **review-agent** | Opus | PR diff analysis | Architecture compliance, code quality, merge verdict (read-only) |
| **validator-agent** | Opus | DDD governance, drift detection | Boundary checks, coupling score, DDD Health Score (read-only) |
| **memory-agent** | Haiku | Sprint summaries, context compression | Evolution tracking, progressive compression |

### How Skills Delegate to Agents

| Skill | Agent Spawned | Model |
|-------|--------------|-------|
| `/ideate` | research-agent | Opus |
| `/enterprise-prd` | product-agent | Opus |
| `/agile-backlog` | product-agent | Opus |
| `/roadmap` | product-agent | Opus |
| `/hld` | architect-agent | Opus |
| `/lld` | architect-agent | Opus |
| `/ddd-architect` | architect-agent | Opus |
| `/develop backend` | backend-agent | Sonnet |
| `/develop frontend` | frontend-agent | Sonnet |
| `/test-suite` | qa-agent | Sonnet |
| `/security-review` | security-agent | Opus |
| `/pr-review create` | devops-agent | Haiku |
| `/pr-review review` | review-agent + validator-agent | Opus |
| `/release tag` | devops-agent | Haiku |
| `/scrum-sprint retro` | memory-agent | Haiku |
| `/build-with-agent-team` | Multiple agents in parallel | Mixed |

### Agent Teams — Parallel Execution

Agent Teams provide split-pane tmux visibility where you can see and interact with all agents simultaneously.

**When to use Agent Teams vs Task tool:**

| Scenario | Mechanism | Why |
|----------|-----------|-----|
| Backend + Frontend (parallel dev) | Agent Teams | Need visibility + interaction |
| Security + Validator (parallel review) | Agent Teams | See both audit results live |
| DDD + LLD (parallel design) | Agent Teams | Both need HLD, write different files |
| Simple gate check | Task tool | Quick, focused, no interaction needed |
| Memory compression | Task tool | Simple write task |

**Setup:**
```bash
# One-time setup
bash scripts/setup-agents.sh

# Launch with Agent Teams
bash scripts/start-sdlc.sh
```

**What you see in tmux:**
```
┌──────────────────┬──────────────────┬──────────────────┐
│  LEAD (main)     │  BACKEND AGENT   │  FRONTEND AGENT  │
│                  │  (Sonnet)        │  (Sonnet)        │
│  Coordinating    │  Writing Java    │  Writing Angular  │
│  sprint US-001   │  DDD layers...   │  Feature module.. │
│                  │                  │                   │
│  Task List:      │  Done: Domain    │  Done: Component  │
│  [Ctrl+T]       │  WIP: Repository │  WIP: Facade      │
│  3/8 complete    │  Next: Controller│  Next: Store      │
└──────────────────┴──────────────────┴──────────────────┘
```

### Contract-First Development

The key to parallel agent execution is **frozen contracts**:

1. **Architect Agent** defines `openapi.yaml` and `schema.sql` during Design phase
2. Contracts are **FROZEN** before Development phase
3. Backend Agent implements server side of the contract
4. Frontend Agent implements client side of the contract
5. Both agents have **STOP rules** — they halt if they need to modify contracts
6. If a contract change is needed, it goes through the Architect Agent with a **version bump**

### Pre-Sprint Review Cycle (Sprint 2+)

Before each subsequent sprint:
```
Sprint N-1 ends
      │
      ▼
MEMORY AGENT: compress sprint N-1 → sprint-N-1-summary.md
      │
      ▼
ARCHITECT AGENT: reviews Sprint N stories + current artifacts
      │
      ├─ New contexts needed? → define + update CONTEXT_MAP.md
      ├─ New API endpoints? → version bump openapi.yaml
      ├─ Schema changes? → new Flyway migration
      ├─ Nothing new? → SKIP (contracts stay frozen)
      │
      ▼
VALIDATOR AGENT: pre-sprint health check (DDD Health Score)
      │
      ▼
Sprint N executes with RE-FROZEN contracts
```

---

## Complete Skill Reference

### Master Orchestrator

| Command | Description |
|---------|-------------|
| `/sdlc init [project]` | Initialize new SDLC project, create state and directory structure |
| `/sdlc status` | Dashboard showing all 9 phases with current status |
| `/sdlc next` | Advance to next phase (runs quality gate first) |
| `/sdlc gate [phase]` | Check quality gate for a specific phase |
| `/sdlc phase [name]` | Jump to a specific phase (with gate check) |
| `/sdlc history` | Show completion timeline with timestamps |

### Phase 1: Ideation

| Command | Description |
|---------|-------------|
| `/ideate [project] [domain]` | Generate product vision with market research |

**Outputs:** `docs/ideation/product-vision.md`

### Phase 2: Requirements

| Command | Description |
|---------|-------------|
| `/enterprise-prd [project] [domain]` | Generate comprehensive PRD |

**Outputs:** `docs/prd/prd.md`

### Phase 3: Project Setup

| Command | Description |
|---------|-------------|
| `/github-project-setup [repo] [owner]` | Create GitHub repo with templates, CI/CD, labels |
| `/agile-backlog [project] [sprints]` | Generate epics, user stories, sprint backlog |
| `/roadmap [project]` | Generate milestone roadmap with Gantt chart |

**Outputs:** GitHub repo, `docs/prd/roadmap.md`, `docs/prd/backlog.md`

### Phase 4: Design

| Command | Description |
|---------|-------------|
| `/hld [project]` | System architecture, component diagrams, tech decisions |
| `/lld [project]` | Class diagrams, sequence diagrams, API contracts |
| `/ddd-architect [domain]` | Bounded contexts, aggregates, domain events |
| `/tech-specs [project] [stack]` | DB schemas, OpenAPI specs, workflow definitions |

**Outputs:** `docs/architecture/hld/`, `docs/architecture/lld/`, `docs/ddd/`, `docs/tech-specs/`

### Phase 5: Development

| Command | Description |
|---------|-------------|
| `/scrum-sprint planning` | Plan a new sprint with story selection |
| `/scrum-sprint progress` | Show current sprint progress |
| `/scrum-sprint review` | Conduct sprint review |
| `/scrum-sprint retro` | Run sprint retrospective |
| `/develop backend [US-XXX]` | Implement backend for a user story |
| `/develop frontend [US-XXX]` | Implement frontend for a user story |
| `/develop api [US-XXX]` | Implement API layer for a user story |

**Outputs:** Source code, `docs/sprints/sprint-N-plan.md`

### Phase 6: Testing

| Command | Description |
|---------|-------------|
| `/test-suite unit` | Generate and run unit tests |
| `/test-suite integration` | Generate and run integration tests |
| `/test-suite e2e` | Generate and run E2E tests |
| `/test-suite coverage` | Check coverage against thresholds |
| `/test-suite all` | Run all tests and generate report |
| `/tdd-helper` | TDD guidance (Red-Green-Refactor) |

**Outputs:** Test files, `docs/testing/test-report.md`

### Phase 7: Security

| Command | Description |
|---------|-------------|
| `/security-review` | OWASP Top 10 security audit |
| `/compliance-checklist [industry]` | Industry-specific compliance check |

**Outputs:** `docs/security/security-review.md`, `docs/security/compliance-report.md`

### Phase 8: Code Review

| Command | Description |
|---------|-------------|
| `/pr-review create` | Create a well-structured PR |
| `/pr-review review [pr#]` | Automated code review with findings |
| `/pr-review list` | List open PRs |

**Outputs:** GitHub PR, review comments

### Phase 9: Release

| Command | Description |
|---------|-------------|
| `/release notes [version]` | Generate release notes from merged PRs |
| `/release checklist` | Pre-deployment verification checklist |
| `/release tag [version]` | Create Git tag and GitHub release |
| `/release status` | Check release readiness |

**Outputs:** `docs/releases/vX.Y.Z-release-notes.md`, Git tag, GitHub release

---

## Sample Project Walkthrough

Let's build **"TaskFlow"** — a task management application with team collaboration, using the full SDLC factory.

### Phase 1: Initialize & Ideate

```
> /sdlc init taskflow
```

This creates the project state and directory structure. Then:

```
> /ideate taskflow project-management
```

The ideate skill will:
- Research the project management tool market
- Analyze competitors (Jira, Asana, Trello, Linear)
- Generate `docs/ideation/product-vision.md` with vision, problems, target audience, value prop

**Check status:**
```
> /sdlc status
```
You'll see Phase 1 marked complete. Now advance:
```
> /sdlc next
```
Gate check passes (product-vision.md exists), advances to Phase 2.

### Phase 2: Requirements

```
> /enterprise-prd taskflow project-management
```

This generates a comprehensive PRD with:
- 5-7 user personas (Project Manager, Developer, Team Lead, etc.)
- Functional requirements (FR-001 through FR-XXX)
- Non-functional requirements (performance, security, scalability)
- Success metrics and KPIs
- Roadmap outline

Save it as `docs/prd/prd.md`, then:
```
> /sdlc next
```

### Phase 3: Project Setup

This phase has 3 skills to run:

```
> /github-project-setup taskflow your-github-username
```
Creates GitHub repo with issue templates, CI/CD workflows, labels, milestones.

```
> /agile-backlog taskflow 4
```
Generates 4 sprints worth of epics, user stories, and task breakdowns.

```
> /roadmap taskflow
```
Creates milestone roadmap with Gantt chart, maps epics to sprints.

```
> /sdlc next
```

### Phase 4: Design

Run all 4 design skills:

```
> /hld taskflow
```
Generates system architecture:
- Angular frontend + Spring Boot API + PostgreSQL + Redis + Kafka
- Component diagram with all services
- Deployment architecture (Docker Compose for dev, K8s for prod)
- Technology decisions with rationale

```
> /ddd-architect task-management
```
Generates DDD artifacts:
- Core domain: Task Management (tasks, projects, boards)
- Supporting domain: Team Management (users, teams, roles)
- Generic domain: Notifications, Authentication
- Bounded contexts with context map
- Aggregate design for each context

```
> /lld taskflow
```
Generates implementation-ready designs:
- Class diagrams for all bounded contexts
- Package structure for Spring Boot + Angular
- Sequence diagrams for key use cases (create task, assign task, move task)
- API contracts for all REST endpoints
- Interface definitions

```
> /tech-specs taskflow "angular spring-boot postgresql redis kafka"
```
Generates:
- PostgreSQL DDL schemas for all tables
- OpenAPI 3.0 specification
- Kafka topic definitions
- Redis caching strategy

```
> /sdlc next
```

### Phase 5: Development

Start with sprint planning:

```
> /scrum-sprint planning
```
Creates Sprint 1 plan, selects user stories from backlog, creates GitHub milestone and issues.

Now implement stories. Example — implementing user registration (US-001):

```
> /develop backend US-001
```
Creates:
- `backend/src/.../user/domain/model/User.java` (entity)
- `backend/src/.../user/domain/repository/UserRepository.java` (interface)
- `backend/src/.../user/application/service/UserApplicationService.java`
- `backend/src/.../user/application/command/CreateUserCommand.java`
- `backend/src/.../user/api/controller/UserController.java`
- `backend/src/.../user/api/dto/UserRequest.java`, `UserResponse.java`
- Database migration: `V1__create_users_table.sql`
- Unit tests for service and controller

```
> /develop frontend US-001
```
Creates:
- `frontend/src/app/features/auth/components/register/`
- `frontend/src/app/features/auth/services/auth.service.ts`
- `frontend/src/app/features/auth/models/user.model.ts`
- Component tests

Check sprint progress:
```
> /scrum-sprint progress
```

When sprint is done:
```
> /scrum-sprint review
> /scrum-sprint retro
> /sdlc next
```

### Phase 6: Testing

```
> /test-suite all
```
Runs unit, integration, and E2E tests. Generates coverage report.
Quality gate: Backend >= 80% coverage, Frontend >= 70%.

```
> /sdlc next
```

### Phase 7: Security

```
> /security-review
```
OWASP Top 10 audit of all code. Checks for:
- SQL injection, XSS, broken auth, security misconfig
- Hardcoded secrets, missing validation
- Generates `docs/security/security-review.md`

```
> /compliance-checklist general
```
General compliance check. For fintech: `/compliance-checklist fintech-eu`.

```
> /sdlc next
```

### Phase 8: Code Review

```
> /pr-review create
```
Creates a well-structured PR with:
- Summary of all changes
- Links to user stories
- Testing checklist
- Security checklist

```
> /pr-review review 1
```
Automated code review checking architecture, patterns, security, quality.

```
> /sdlc next
```

### Phase 9: Release

```
> /release notes v1.0.0
```
Generates release notes from all merged PRs since start.

```
> /release checklist
```
Pre-deployment verification checklist.

```
> /release tag v1.0.0
```
Creates Git tag and GitHub Release.

```
> /sdlc status
```

**Final dashboard shows all 9 phases complete!**

---

## Agent Teams Walkthrough

This section shows how to use Agent Teams for parallel execution across key SDLC phases.

### Phase 1: Interactive Ideation

```bash
bash scripts/start-sdlc.sh
> /sdlc init my-lending-platform
> /ideate digital-lending
```

The Research Agent (Opus) runs in interactive mode:
1. Asks you 8 structured questions (domain, problem, users, scale, competitors, constraints)
2. Researches based on your answers (web search)
3. Presents 3 product direction options with visual previews
4. You choose a direction
5. Agent generates informed `docs/ideation/product-vision.md`

### Phase 4: Parallel Design with Agent Teams

After HLD completes, run DDD and LLD in parallel:

```
> /hld my-lending-platform
# Architect Agent generates HLD (must complete first)

# Then launch Agent Team for parallel design:
> Create an agent team:
  - One architect for DDD domain modeling (writes to docs/ddd/)
  - One architect for LLD class/sequence diagrams (writes to docs/architecture/lld/)
  Both read the HLD. No file conflicts.
```

You'll see both architects working simultaneously in split panes.

### Phase 5: Parallel Development with Agent Teams

This is where Agent Teams really shine:

```
> /scrum-sprint planning

# Launch parallel development:
> /build-with-agent-team docs/sprints/sprint-1-plan.md
```

The `/build-with-agent-team` skill:
1. Reads the sprint plan and frozen contracts
2. Extracts relevant endpoints from `openapi.yaml`
3. Extracts relevant tables from `schema.sql`
4. Spawns Backend + Frontend + QA agents in parallel
5. Each agent has clear file ownership (no conflicts)
6. You monitor, relay messages, and mediate contract issues
7. After all agents report done, you run E2E validation

### Phase 7: Parallel Security + Governance

```
> Create an agent team for security review:
  - Security reviewer (Opus): OWASP Top 10 audit
  - Validator (Opus): DDD governance + drift detection
  Both are READ-ONLY. Neither modifies code.
```

Both agents scan the codebase simultaneously. Combined output determines if the code is safe to merge.

### Phase 8: Parallel PR Review

```
> /pr-review create
# DevOps Agent creates the PR

> /pr-review review 1
# Review Agent + Validator Agent review in parallel
# Combined verdict: APPROVE / REQUEST CHANGES
```

### Quick Reference Card

```
PHASE    COMMAND                           AGENTS                   MODE
-----    -------                           ------                   ----
1        /ideate [topic]                   research (Opus)          Interactive
2        /enterprise-prd [proj] [domain]   product (Opus)           Interactive
3        /github-project-setup             devops (Haiku)           Single
4        /hld [project]                    architect (Opus)         Single
4        /ddd + /lld                       architect (Opus) x2      Agent Team
5        /build-with-agent-team            backend + frontend + qa  Agent Team
6        /test-suite all                   qa (Sonnet)              Single
7        /security-review + /sdlc gate     security + validator     Agent Team
8        /pr-review create + review        devops + review + valid. Agent Team
9        /release tag [version]            devops (Haiku)           Single
```

---

## Quality Gates

Quality gates enforce that each phase is properly completed before advancing.

| Gate | From → To | What's Checked |
|------|-----------|---------------|
| G1 | Ideation → Requirements | `docs/ideation/product-vision.md` exists with vision + problems |
| G2 | Requirements → Setup | `docs/prd/prd.md` exists with FRs, NFRs, personas |
| G3 | Setup → Design | GitHub repo exists, roadmap generated, backlog populated |
| G4 | Design → Development | HLD, LLD docs exist in `docs/architecture/` |
| G5 | Development → Testing | Source code exists, build passes, sprint plan exists |
| G6 | Testing → Security | Test coverage meets thresholds (80% backend, 70% frontend) |
| G7 | Security → Code Review | Security review complete, no critical findings |
| G8 | Code Review → Release | PR created and approved, CI checks pass |
| G9 | Release Complete | Release notes generated, Git tag created |

### Bypassing Gates
In exceptional cases, use `--force`:
```
> /sdlc phase development --force
```
This logs the bypass in state.json for audit purposes.

---

## Tips & Best Practices

### 1. Use the Guided Flow for New Projects
Start with `/sdlc init` and use `/sdlc next` to walk through each phase. The orchestrator guides you on which skills to run next.

### 2. Use Independent Skills for Iteration
Already past the design phase but need to update the HLD? Just run `/hld [project]` directly — no need to go through the orchestrator.

### 3. Check Status Frequently
Run `/sdlc status` to see your dashboard. It shows what's done, what's in progress, and what's next.

### 4. Sprint-Based Development
During the development phase, use the full Scrum workflow:
1. `/scrum-sprint planning` — Plan the sprint
2. `/develop backend US-XXX` — Implement stories
3. `/scrum-sprint progress` — Check progress
4. `/scrum-sprint review` — Review the sprint
5. `/scrum-sprint retro` — Retrospective

### 5. Iterative Design
The Design phase has 4 skills that build on each other:
1. `/hld` first (system overview)
2. `/ddd-architect` second (domain modeling)
3. `/lld` third (implementation details, uses HLD + DDD)
4. `/tech-specs` fourth (concrete specifications)

### 6. Multiple Sprints
The Development phase supports multiple sprints. After each sprint:
- Run `/scrum-sprint review` and `/scrum-sprint retro`
- Then `/scrum-sprint planning` for the next sprint
- Continue until all planned stories are implemented

### 7. Customize Rules
Edit the files in `.claude/rules/` to match your team's conventions:
- `01-general.md` — Git workflow, file naming
- `02-quality-gates.md` — Adjust thresholds
- `03-java-patterns.md` — Spring Boot conventions
- `04-angular-patterns.md` — Angular conventions
- `05-security.md` — Security standards

### 8. Extending the Factory
Add new skills by creating directories in `.claude/skills/`:
```
.claude/skills/my-new-skill/SKILL.md
```
Follow the existing skill pattern with frontmatter, context injection, and state updates.

---

## File Reference

| File | Purpose |
|------|---------|
| `CLAUDE.md` | Project configuration, skill catalog, agent catalog, conventions |
| `.claude/agents/*.md` | Agent definitions (11 specialized agents) |
| `.claude/rules/*.md` | Coding standards and quality gate definitions |
| `.claude/skills/*/SKILL.md` | Skill definitions (10 skills including build-with-agent-team) |
| `.claude/hooks/*.sh` | Agent team event hooks (TaskCompleted, TeammateIdle) |
| `.claude/settings.local.json` | Permissions and agent team configuration |
| `.sdlc/state.json` | SDLC state tracker |
| `scripts/sdlc-gate-check.sh` | Quality gate validation script |
| `scripts/start-sdlc.sh` | Session launcher with Agent Teams + tmux |
| `scripts/setup-agents.sh` | One-time agent configuration setup |
| `docs/` | All generated documentation artifacts |

### Agent Definitions

| Agent | File | Model | Role |
|-------|------|-------|------|
| Research | `.claude/agents/research-agent.md` | Opus | Interactive market research |
| Product | `.claude/agents/product-agent.md` | Opus | PRD, epics, stories |
| Architect | `.claude/agents/architect-agent.md` | Opus | HLD, LLD, DDD, contracts |
| Backend | `.claude/agents/backend-agent.md` | Sonnet | Spring Boot DDD code |
| Frontend | `.claude/agents/frontend-agent.md` | Sonnet | Angular feature modules |
| QA | `.claude/agents/qa-agent.md` | Sonnet | Tests and coverage |
| Security | `.claude/agents/security-agent.md` | Opus | OWASP audit (read-only) |
| DevOps | `.claude/agents/devops-agent.md` | Haiku | Git, PRs, releases |
| Review | `.claude/agents/review-agent.md` | Opus | Code review (read-only) |
| Validator | `.claude/agents/validator-agent.md` | Opus | DDD governance (read-only) |
| Memory | `.claude/agents/memory-agent.md` | Haiku | Sprint summaries |

### Skills Created by This Factory

| Skill | Location | Description |
|-------|----------|-------------|
| `/sdlc` | `.claude/skills/sdlc/SKILL.md` | Master orchestrator with agent delegation |
| `/ideate` | `.claude/skills/ideate/SKILL.md` | Product ideation → research-agent |
| `/roadmap` | `.claude/skills/roadmap/SKILL.md` | Product roadmap → product-agent |
| `/hld` | `.claude/skills/hld/SKILL.md` | High-level design → architect-agent |
| `/lld` | `.claude/skills/lld/SKILL.md` | Low-level design → architect-agent |
| `/develop` | `.claude/skills/develop/SKILL.md` | Development → backend/frontend-agent |
| `/test-suite` | `.claude/skills/test-suite/SKILL.md` | Testing → qa-agent |
| `/pr-review` | `.claude/skills/pr-review/SKILL.md` | PR & review → devops/review/validator-agent |
| `/release` | `.claude/skills/release/SKILL.md` | Release → devops-agent + memory-agent |
| `/build-with-agent-team` | `.claude/skills/build-with-agent-team/SKILL.md` | Parallel sprint execution |

### Pre-existing Skills (Global — in ~/.claude/commands/)

| Skill | Purpose |
|-------|---------|
| `/enterprise-prd` | PRD generation |
| `/ddd-architect` | DDD design |
| `/scrum-sprint` | Sprint management |
| `/agile-backlog` | Backlog generation |
| `/github-project-setup` | GitHub repo setup |
| `/tech-specs` | Technical specifications |
| `/tdd-helper` | TDD guidance |
| `/security-review` | Security audit |
| `/compliance-checklist` | Compliance checks |
| `/angular-scaffold` | Angular scaffolding |
| `/spring-boot-scaffold` | Spring Boot scaffolding |
