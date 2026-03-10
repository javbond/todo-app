---
name: hld
description: High-Level Design generator. Creates system architecture, component diagrams, technology decisions, deployment architecture, and data flow diagrams. Use after PRD is complete and before low-level design.
argument-hint: [project-name]
user-invocable: true
---

# High-Level Design (HLD) Generator

You are a Solutions Architect. Generate comprehensive high-level design documents that bridge product requirements to technical implementation.

## Current SDLC State
!`python3 -c 'import json; s=json.load(open(".sdlc/state.json")); print("Project: " + s.get("project","?") + "  |  Phase: " + s.get("currentPhase","?"))' 2>/dev/null || echo "Project: Not initialized"`

## Context — PRD
!`cat docs/prd/prd.md 2>/dev/null | head -80 || echo "PRD not found."`

## Context — Product Vision
!`cat docs/ideation/product-vision.md 2>/dev/null | head -50 || echo "Vision not found."`


## Multi-Stack & Imported Architecture Context

### Before Designing: Check Existing Material
1. **Imported architecture docs**: Read `.sdlc/state.json → importedDocs.architecture`
   - If imported architecture documents exist, read the extracted `.md` — these are REFERENCE, not final
   - If `docs/architecture/hld/system-architecture.md` already exists with `<!-- IMPORTED -->` markers, READ it and fill gaps
2. **Reference docs**: Read `docs/tech-refs/` for project-level architecture guides
   - These may contain HLD/LLD sections, component diagrams, or deployment architecture
   - Use as reference input, but ensure the generated HLD is comprehensive and follows the required format
3. **Ask questions** to fill gaps — use `AskUserQuestion` for:
   - Architecture style confirmation (microservices vs modular monolith vs hybrid)
   - Deployment preferences (cloud provider, Kubernetes vs serverless)
   - Performance/scalability requirements not covered in reference docs

### Multi-Stack Technology Decisions
Read `.claude/rules/06-tech-stack-context.md` and `.sdlc/state.json → techStack` for the FULL project stack.

**The Technology Stack section (Section 3) MUST reflect ALL configured stacks**, not just Angular + Spring Boot defaults:
- Primary frontend and backend from `techStack.primary`
- Additional workspaces from `techStack.additional` (e.g., Go data plane, Envoy proxy)
- All databases, messaging, search, and infrastructure from `techStack`
- Component diagrams must include ALL workspaces and their integration points

### Cross-Workspace Integration
If additional workspaces exist, the HLD must include:
- Integration architecture between primary and additional stacks
- Communication patterns (REST, gRPC, Kafka, etc.) between workspaces
- Shared contract definitions (reference `docs/tech-specs/shared-schemas/`)
- Deployment architecture showing all workspace deployments

## Arguments
- `$1` = Project name

## Instructions

### Step 1: Read All Context
Read these files completely before designing:
- `docs/prd/prd.md` — Full PRD
- `docs/ideation/product-vision.md` — Product vision
- `docs/prd/roadmap.md` — Roadmap (if exists)

### Step 2: Generate HLD Document

Create `docs/architecture/hld/system-architecture.md`:

```markdown
# [Project Name] — High-Level Design

## 1. System Overview

### 1.1 Architecture Style
[Microservices / Modular Monolith / Event-Driven / Hybrid]

Justification: [Why this style fits the requirements]

### 1.2 System Context Diagram
```
                            ┌─────────────────┐
                            │   [System Name]  │
                            └────────┬────────┘
                                     │
              ┌──────────────────────┼──────────────────────┐
              │                      │                      │
    ┌─────────▼──────┐    ┌─────────▼──────┐    ┌─────────▼──────┐
    │   Frontend     │    │   Backend API   │    │  External      │
    │   (Angular)    │    │  (Spring Boot)  │    │  Systems       │
    └────────────────┘    └────────┬────────┘    └────────────────┘
                                   │
                    ┌──────────────┼──────────────┐
                    │              │              │
              ┌─────▼─────┐ ┌─────▼─────┐ ┌─────▼─────┐
              │ PostgreSQL │ │   Redis   │ │   Kafka   │
              └───────────┘ └───────────┘ └───────────┘
```

### 1.3 Key Architecture Decisions

| # | Decision | Options Considered | Chosen | Rationale |
|---|----------|-------------------|--------|-----------|
| ADR-001 | [Decision] | [Options] | [Chosen] | [Why] |

## 2. Component Architecture

### 2.1 Component Diagram
```
┌──────────────────────────────────────────────────────────────────┐
│                        PRESENTATION LAYER                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐    │
│  │  Module 1 │  │  Module 2 │  │  Module 3 │  │ Shared/Core  │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────────┘    │
├──────────────────────────────────────────────────────────────────┤
│                          API GATEWAY                              │
│  ┌──────────────────────────────────────────────────────────┐    │
│  │  Authentication │ Rate Limiting │ Routing │ CORS          │    │
│  └──────────────────────────────────────────────────────────┘    │
├──────────────────────────────────────────────────────────────────┤
│                       APPLICATION LAYER                           │
│  ┌────────────┐  ┌────────────┐  ┌────────────┐                 │
│  │  Service 1  │  │  Service 2  │  │  Service 3  │                 │
│  │ (Context A) │  │ (Context B) │  │ (Context C) │                 │
│  └──────┬─────┘  └──────┬─────┘  └──────┬─────┘                 │
├─────────┼───────────────┼───────────────┼────────────────────────┤
│         │    DATA LAYER │               │                         │
│  ┌──────▼──────┐ ┌──────▼──────┐ ┌──────▼──────┐                │
│  │ PostgreSQL  │ │    Redis    │ │    Kafka    │                │
│  │  (Primary)  │ │   (Cache)   │ │  (Events)   │                │
│  └─────────────┘ └─────────────┘ └─────────────┘                │
└──────────────────────────────────────────────────────────────────┘
```

### 2.2 Component Responsibilities

| Component | Responsibility | Technology | Communication |
|-----------|---------------|------------|---------------|
| Frontend | User interface, client-side logic | Angular 17+ | REST API |
| API Gateway | Auth, routing, rate limiting | Spring Cloud Gateway | HTTP |
| Service A | [Business domain A] | Spring Boot | REST + Events |
| PostgreSQL | Primary data store | PostgreSQL 15+ | JDBC |
| Redis | Caching, sessions | Redis 7+ | Redis protocol |
| Kafka | Event streaming, async | Kafka 3.x | Kafka protocol |

## 3. Technology Stack

### 3.1 Stack Decisions

| Layer | Technology | Version | Justification |
|-------|-----------|---------|---------------|
| Frontend | Angular | 17+ | [Why] |
| Backend | Spring Boot | 3.x | [Why] |
| Database | PostgreSQL | 15+ | [Why] |
| Cache | Redis | 7+ | [Why] |
| Messaging | Kafka | 3.x | [Why] |
| Search | Elasticsearch | 8.x | [Why] |
| Build | Maven | 3.9+ | [Why] |
| CI/CD | GitHub Actions | - | [Why] |

### 3.2 Library & Framework Choices

| Purpose | Library | Justification |
|---------|---------|---------------|
| ORM | Spring Data JPA + Hibernate | Standard, well-supported |
| DTO Mapping | MapStruct | Compile-time, type-safe |
| Validation | Bean Validation (Jakarta) | Standard, annotation-based |
| Security | Spring Security + JWT | Industry standard |
| API Docs | SpringDoc OpenAPI | Auto-generated from code |
| Testing | JUnit5 + Mockito + TestContainers | Comprehensive testing |

## 4. Data Architecture

### 4.1 Data Flow Diagram
```
[User] → [Angular App] → [API Gateway] → [Service]
                                              │
                              ┌───────────────┼───────────────┐
                              │               │               │
                         [PostgreSQL]     [Redis Cache]   [Kafka]
                         (Read/Write)    (Read Cache)    (Events)
                              │                              │
                              └──────────────────────────────▼
                                                    [Consumer Service]
```

### 4.2 Data Storage Strategy

| Data Type | Storage | Justification |
|-----------|---------|---------------|
| Transactional | PostgreSQL | ACID compliance, relational |
| Session/Cache | Redis | Low latency, TTL support |
| Events/Audit | Kafka + PostgreSQL | Event sourcing, replay |
| Search Index | Elasticsearch | Full-text search, aggregations |
| File Storage | S3-compatible | Scalable object storage |

## 5. Integration Architecture

### 5.1 Integration Patterns

| Pattern | Used For | Implementation |
|---------|----------|----------------|
| REST API | Synchronous queries | Spring MVC + WebClient |
| Event-Driven | Async processing | Kafka topics |
| CQRS | Read/write separation | Separate query/command models |

### 5.2 External Integrations

| System | Protocol | Purpose |
|--------|----------|---------|
| [System 1] | REST API | [Purpose] |
| [System 2] | Webhook | [Purpose] |

## 6. Deployment Architecture

### 6.1 Deployment Diagram
```
┌─────────────── Production Environment ──────────────────┐
│                                                          │
│  ┌──────────── Kubernetes Cluster ──────────────┐       │
│  │                                               │       │
│  │  ┌─────────┐  ┌─────────┐  ┌─────────┐      │       │
│  │  │ Angular │  │  API    │  │ Service │      │       │
│  │  │  (Nginx)│  │ Gateway │  │   Pods  │      │       │
│  │  │  x2     │  │  x2     │  │   x3    │      │       │
│  │  └─────────┘  └─────────┘  └─────────┘      │       │
│  │                                               │       │
│  └───────────────────────────────────────────────┘       │
│                                                          │
│  ┌──────────── Data Layer ──────────────────────┐       │
│  │ PostgreSQL (Primary/Replica)                  │       │
│  │ Redis Cluster                                 │       │
│  │ Kafka Cluster (3 brokers)                     │       │
│  └───────────────────────────────────────────────┘       │
└──────────────────────────────────────────────────────────┘
```

### 6.2 Environment Strategy
| Environment | Purpose | Infrastructure |
|-------------|---------|---------------|
| Development | Local dev | Docker Compose |
| Staging | Pre-prod testing | Kubernetes (single node) |
| Production | Live system | Kubernetes (HA cluster) |

## 7. Cross-Cutting Concerns

### 7.1 Security Architecture
- Authentication: JWT with refresh tokens
- Authorization: RBAC with Spring Security
- API Security: Rate limiting, CORS, CSP headers
- Data encryption: TLS in transit, AES-256 at rest

### 7.2 Observability
- Logging: Structured JSON (Logback + SLF4J)
- Metrics: Micrometer + Prometheus
- Tracing: OpenTelemetry
- Alerting: Prometheus Alertmanager

### 7.3 Performance
- Caching strategy: Redis L1 (hot data), CDN (static assets)
- Connection pooling: HikariCP
- Async processing: Kafka for heavy operations
- Pagination: Cursor-based for large datasets

## 8. Non-Functional Requirements Mapping

| NFR | Target | Approach |
|-----|--------|----------|
| Response time | < 200ms (P95) | Caching, query optimization |
| Availability | 99.9% | Kubernetes HA, health checks |
| Throughput | [X] req/sec | Horizontal scaling, async |
| Concurrent users | [N] | Load balancing, connection pools |
```

### Step 3: Update SDLC State
Update `.sdlc/state.json`:
- Add `"docs/architecture/hld/system-architecture.md"` to `phases.design.artifacts`
- If design phase is pending, set status to `"in_progress"`
- Update `updatedAt`

### Step 4: Present Summary
```
High-Level Design for [Project Name] has been generated.

Artifact: docs/architecture/hld/system-architecture.md

Components: [N] major components identified
Integrations: [N] integration points
ADRs: [N] architecture decisions documented

Next Steps:
1. Review the HLD
2. Run /ddd-architect [domain] for domain modeling
3. Run /lld [project] for low-level design
4. Run /tech-specs [project] [stack] for technical specifications
```

---

## Agent Delegation

This skill delegates to the **Architect Agent** (`.claude/agents/architect-agent.md`).

### When using Agent Teams (recommended for parallel design):
The Architect Agent runs on **Opus** model. In Agent Teams mode, you can run HLD, DDD, and LLD in parallel after HLD completes:
- `/hld` → Architect Agent (must complete first)
- `/ddd-architect` ║ `/lld` → Two Architect Agents in parallel (both read HLD)

### When using Task tool:
```
Use the Task tool to spawn an architect-agent subagent:
- subagent_type: "general-purpose"
- model: "opus"
- Provide: project name, PRD content, product vision
- Agent produces: docs/architecture/hld/system-architecture.md
```

### Dual Diagram Output:
The Architect Agent generates BOTH ASCII (terminal-friendly) and Mermaid (GitHub/VS Code rendered) diagrams. Standalone Mermaid files are saved to `docs/architecture/hld/diagrams/*.mmd`.

### Agent produces:
- `docs/architecture/hld/system-architecture.md` — System architecture with ADRs
- `docs/architecture/hld/diagrams/*.mmd` — Mermaid diagram files

### Agent reads:
- `docs/prd/prd.md`, `docs/ideation/product-vision.md`, `docs/prd/roadmap.md`
