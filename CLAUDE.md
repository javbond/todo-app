# AI-Native SDLC Factory

An AI-powered Software Development Lifecycle factory built on Claude Code. Transforms Claude Code into a complete software development team using orchestrated skills, enforced quality gates, and real-world Agile/Scrum methodology.

## Tech Stack
- **Frontend**: Angular 17+ (standalone components, NgRx, lazy loading)
- **Backend**: Spring Boot 3.x (Java 17+, DDD architecture)
- **Database**: PostgreSQL (primary), Redis (cache)
- **Messaging**: Kafka / RabbitMQ (event-driven)
- **Search**: Elasticsearch
- **VCS**: GitHub + GitHub Projects
- **Architecture**: Domain-Driven Design (DDD)

## Available Skills

### Master Orchestrator
- `/sdlc init [project]` — Initialize a new project
- `/sdlc status` — Show SDLC dashboard with phase progress
- `/sdlc next` — Advance to next phase (enforces quality gate)
- `/sdlc gate [phase]` — Check quality gate for a phase
- `/sdlc phase [name]` — Jump to a specific phase
- `/sdlc history` — Show completion timeline

### Phase Skills (invoke independently or via `/sdlc next`)
| Phase | Skill | Description |
|-------|-------|-------------|
| 1. Ideation | `/ideate [topic]` | Product vision, market research, value proposition |
| 2. Requirements | `/enterprise-prd [project] [domain]` | PRD with personas, requirements, KPIs |
| 3. Project Setup | `/github-project-setup [repo] [owner]` | GitHub repo, CI/CD, labels, templates |
| 3. Backlog | `/agile-backlog [project] [sprints]` | Epics, user stories, sprint plans |
| 3. Roadmap | `/roadmap [project]` | Milestone-based roadmap with Gantt chart |
| 4. Architecture | `/hld [project]` | System architecture, component design |
| 4. Design | `/lld [project]` | Class diagrams, sequence diagrams, API contracts |
| 4. DDD | `/ddd-architect [domain]` | Bounded contexts, aggregates, domain events |
| 4. Tech Specs | `/tech-specs [project] [stack]` | DB schemas, OpenAPI specs, workflows |
| 5. Sprint | `/scrum-sprint [action]` | Sprint planning, progress, review, retro |
| 5. Development | `/develop [layer] [story]` | Frontend/backend/API implementation |
| 6. Testing | `/test-suite [type]` | Unit, integration, E2E, coverage |
| 6. TDD | `/tdd-helper` | Test-driven development guidance |
| 7. Security | `/security-review` | OWASP security audit |
| 7. Compliance | `/compliance-checklist [industry]` | Regulatory compliance checks |
| 8. PR | `/pr-review [action]` | PR creation and code review |
| 9. Release | `/release [action]` | Release notes, checklist, tagging |

### Agent Team Orchestration
- `/build-with-agent-team [plan] [agents]` — Parallel sprint execution with split-pane visibility

## Available Agents

The SDLC Factory uses specialized agents with model-appropriate assignments. Each agent has defined ownership, tools, and governance rules.

### Agent Definitions (`.claude/agents/`)

| Agent | Model | Role | Tools | Read-Only? |
|-------|-------|------|-------|-----------|
| `research-agent` | Opus | Market research, interactive discovery | Read, Glob, Grep, WebSearch, WebFetch, AskUserQuestion | Yes |
| `product-agent` | Opus | PRD, epics, stories, roadmap | Read, Write, Edit, Glob, Grep, AskUserQuestion | No |
| `architect-agent` | Opus | HLD, LLD, DDD, contracts, Mermaid diagrams | Read, Write, Edit, Glob, Grep | No |
| `backend-agent` | Sonnet | Spring Boot DDD implementation | Read, Write, Edit, Glob, Grep, Bash | No |
| `frontend-agent` | Sonnet | Angular 17+ feature modules | Read, Write, Edit, Glob, Grep, Bash | No |
| `qa-agent` | Sonnet | JUnit5, Jest, Playwright tests | Read, Write, Edit, Glob, Grep, Bash | No |
| `security-agent` | Opus | OWASP Top 10 audit, credential scan | Read, Glob, Grep | Yes |
| `devops-agent` | Haiku | Git, gh CLI, CI/CD, releases | Read, Glob, Grep, Bash | Yes (code) |
| `review-agent` | Opus | PR diff analysis, architecture compliance | Read, Glob, Grep | Yes |
| `validator-agent` | Opus | DDD governance, drift detection, health score | Read, Glob, Grep | Yes |
| `memory-agent` | Haiku | Sprint summaries, context compression | Read, Write, Glob, Grep | No |

### Model Strategy
- **Opus (Thinkers)**: Research, product, architect, security, review, validator — deep analysis and reasoning
- **Sonnet (Builders)**: Backend, frontend, QA — code generation and implementation
- **Haiku (Executors)**: DevOps, memory — fast CLI operations and simple writes

### Agent Teams
Use Agent Teams for parallel execution with tmux split-pane visibility:
- **Setup**: `bash scripts/setup-agents.sh` (one-time)
- **Launch**: `bash scripts/start-sdlc.sh` (session launcher)
- **Parallel patterns**:
  - DDD + LLD (both read HLD)
  - Backend + Frontend (both read frozen contracts)
  - Security + Validator (both read-only)
  - Review + Validator (parallel PR review)

## Rules & Conventions
- @.claude/rules/01-general.md
- @.claude/rules/02-quality-gates.md
- @.claude/rules/03-java-patterns.md
- @.claude/rules/04-angular-patterns.md
- @.claude/rules/05-security.md

## SDLC State
Current project state is tracked in `.sdlc/state.json`. All phase artifacts are stored in `docs/`.

## Key Directories
| Directory | Purpose |
|-----------|---------|
| `.sdlc/` | State tracking and phase metadata |
| `docs/` | All generated documentation artifacts |
| `scripts/` | Automation, gate-check, and session launcher scripts |
| `.claude/agents/` | Agent definitions (11 specialized agents) |
| `.claude/skills/` | Project-specific SDLC skills |
| `.claude/rules/` | Coding standards and conventions |
| `.claude/hooks/` | Agent team event hooks (TaskCompleted, TeammateIdle) |

## Git Workflow (GitHub Flow)
- Branch from `main`, merge back via PR — no `develop` or `release` branches
- Branch naming: `feature/US-XXX-description`, `bugfix/BUG-XXX-description`
- Commit format: `type(scope): description` (conventional commits)
- Types: feat, fix, docs, style, refactor, test, chore
- All changes via PRs to `main` with at least 1 review
- Squash merge to `main`
- Post-merge: sync local main, delete feature branch
- Releases: tag on `main`, no release branches
