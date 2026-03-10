# Clarity (todo-app) — Product Roadmap

**Project:** todo-app
**Created:** 2026-03-05
**Based on:** docs/prd/prd.md, docs/prd/backlog.md

---

## Roadmap Overview

### Release Strategy

| Release | Scope | Duration | Sprints | Target Date |
|---------|-------|----------|---------|-------------|
| **MVP (v1.0)** | P0 features: Auth, Task CRUD, Today View, Lists, Priorities, Due Dates | 12 weeks | Sprint 1–6 | 2026-05-29 |
| **Phase 2 (v1.1)** | P1 features: Upcoming View, Quick Add, Search/Filter, Recurring Tasks, Dark Mode | 12 weeks | Sprint 7–12 | 2026-08-21 |
| **Phase 3 (v2.0)** | P2 features: Drag-and-Drop, Subtasks, Analytics, Data Export | 12 weeks | Sprint 13–18 | 2026-11-13 |

### Total Timeline: 36 weeks (9 months)
### MVP Focus: 12 weeks (3 months) — this is the primary planning horizon

---

## Gantt Chart

```
2026     Mar         Apr         May         Jun         Jul         Aug
Week:    1    2    3    4    5    6    7    8    9   10   11   12
         ├────┼────┼────┼────┼────┼────┼────┼────┼────┼────┼────┤
Sprint 1 ████████████
         Architecture & Scaffold
Sprint 2              ████████████
                      Task CRUD
Sprint 3                           ████████████
                                   Lists, Priorities, Today View
Sprint 4                                        ████████████
                                                Due Dates & Auth
Sprint 5                                                     ████████████
                                                              Testing & Security
Sprint 6                                                                  ████████████
                                                                          Deploy & Release
         ├────── M1 ──────┤├────── M2 ──────┤├── M3 ─┤
         Architecture      Core Features      MVP Release v1.0
         & Foundation      Complete

--- Phase 2 (v1.1) ---
Week:   13   14   15   16   17   18   19   20   21   22   23   24
         ├────┼────┼────┼────┼────┼────┼────┼────┼────┼────┼────┤
Sprint 7 ████████████
         Upcoming View & Quick Add
Sprint 8              ████████████
                      Search & Filter
Sprint 9                           ████████████
                                   Recurring Tasks
Sprint 10                                       ████████████
                                                Dark Mode & OAuth2
Sprint 11                                                    ████████████
                                                             Testing & QA
Sprint 12                                                                 ████████████
                                                                          v1.1 Release
         ├────── M4 ──────┤├────── M5 ──────┤├── M6 ─┤
         Enhanced UX       Power Features     v1.1 Release
```

---

## Milestone Details

### M1: Architecture & Foundation (Weeks 1–4)

**Goal:** Establish the architectural foundation, scaffold both projects, set up the database and CI/CD pipeline.
**Sprint mapping:** Sprint 1
**Due date:** 2026-04-05

| Epic | User Stories | Priority | Sprint | Story Points |
|------|-------------|----------|--------|-------------|
| EPIC-001 | US-001: Project scaffolding | P0 | Sprint 1 | 8 |
| EPIC-001 | US-002: Database schema | P0 | Sprint 1 | 5 |
| EPIC-001 | US-003: CI/CD pipeline | P0 | Sprint 1 | 3 |

**Deliverables:**
- [ ] Spring Boot 3.x backend with DDD package structure
- [ ] Angular 17+ frontend with standalone components
- [ ] PostgreSQL schema (users, tasks, task_lists) via Flyway
- [ ] Docker Compose for local development
- [ ] CI pipeline green on GitHub Actions
- [ ] Design docs: HLD, LLD, DDD, Tech Specs

**Success Criteria:**
- `mvn verify` passes on backend
- `ng build --configuration=production` passes on frontend
- All Flyway migrations apply cleanly
- CI pipeline runs on push to main/develop

---

### M2: Core Features (Weeks 5–8)

**Goal:** Deliver the core task management experience — CRUD, lists, priorities, and the Today View.
**Sprint mapping:** Sprint 2–3
**Due date:** 2026-05-03

| Epic | User Stories | Priority | Sprint | Story Points |
|------|-------------|----------|--------|-------------|
| EPIC-003 | US-008: Create a task | P0 | Sprint 2 | 5 |
| EPIC-003 | US-009: View and edit tasks | P0 | Sprint 2 | 5 |
| EPIC-003 | US-010: Complete and undo | P0 | Sprint 2 | 3 |
| EPIC-003 | US-011: Delete a task | P0 | Sprint 2 | 2 |
| EPIC-004 | US-012: Create and manage lists | P0 | Sprint 3 | 5 |
| EPIC-004 | US-013: Assign tasks to lists | P0 | Sprint 3 | 3 |
| EPIC-004 | US-014: Sidebar navigation | P0 | Sprint 3 | 3 |
| EPIC-005 | US-015: Priority levels | P0 | Sprint 3 | 3 |
| EPIC-005 | US-016: Sort by priority | P0 | Sprint 3 | 3 |
| EPIC-006 | US-017: Today View | P0 | Sprint 3 | 5 |
| EPIC-006 | US-018: Overdue section | P0 | Sprint 3 | 3 |
| EPIC-006 | US-019: Daily progress | P0 | Sprint 3 | 3 |

**Deliverables:**
- [ ] Full task CRUD (create, view, edit, complete, undo, delete)
- [ ] Task lists with CRUD and sidebar navigation
- [ ] Priority levels (P1–P4) with color-coded indicators
- [ ] Today View as default landing page with overdue section
- [ ] NgRx state management with optimistic updates

**Success Criteria:**
- All CRUD operations work end-to-end (API + UI)
- Tasks can be organized into lists and prioritized
- Today View correctly separates today's tasks, overdue, and completed
- API response time < 500ms (p95)

---

### M3: MVP Release v1.0 (Weeks 9–12)

**Goal:** Complete authentication, due dates, testing, security review, and production deployment.
**Sprint mapping:** Sprint 4–6
**Due date:** 2026-06-07

| Epic | User Stories | Priority | Sprint | Story Points |
|------|-------------|----------|--------|-------------|
| EPIC-007 | US-020: Assign due dates | P0 | Sprint 4 | 3 |
| EPIC-007 | US-021: Overdue indicators | P0 | Sprint 4 | 2 |
| EPIC-007 | US-022: Browser reminders | P0 | Sprint 4 | 5 |
| EPIC-002 | US-004: User registration | P0 | Sprint 4 | 5 |
| EPIC-002 | US-005: User login (JWT) | P0 | Sprint 4 | 8 |
| EPIC-002 | US-006: Logout & tokens | P0 | Sprint 4 | 3 |
| EPIC-002 | US-007: Password reset | P0 | Sprint 4 | 5 |
| EPIC-008 | US-023: E2E testing | P0 | Sprint 5 | 8 |
| EPIC-008 | US-024: Security hardening | P0 | Sprint 5 | 5 |
| EPIC-008 | US-025: Production deploy | P0 | Sprint 6 | 5 |

**Deliverables:**
- [ ] Due dates with date picker and overdue visual indicators
- [ ] Browser notification reminders at configurable intervals
- [ ] Full auth flow: register, login, logout, password reset, JWT
- [ ] Account lockout after 5 failed attempts
- [ ] 80% backend / 70% frontend test coverage
- [ ] Security review with 0 critical/high findings
- [ ] Production deployment (Docker, PostgreSQL, Redis)
- [ ] v1.0 release notes and Git tag

**Success Criteria:**
- All P0 features functional and tested
- Security review passes OWASP Top 10
- Application accessible via public URL
- Health check returns 200
- 5 beta testers confirm "fast and clean" experience

---

### M4: Enhanced UX (Weeks 13–16) — Post-MVP

**Goal:** Add Upcoming View and Quick Add for better planning and task capture.
**Sprint mapping:** Sprint 7–8
**Due date:** 2026-07-05

| Feature | Requirements | Priority | Story Points (est.) |
|---------|-------------|----------|---------------------|
| Upcoming View | FR-007.1, FR-007.2 | P1 | 8 |
| Quick Add | FR-008.1, FR-008.2 | P1 | 5 |
| OAuth2 login | FR-001.6 | P1 | 8 |

**Deliverables:**
- [ ] Upcoming View with date-grouped task display
- [ ] Quick Add modal (Ctrl/Cmd+K) with keyboard-only flow
- [ ] Google and GitHub OAuth2 login

---

### M5: Power Features (Weeks 17–20) — Post-MVP

**Goal:** Add search, filtering, and recurring task support.
**Sprint mapping:** Sprint 9–10
**Due date:** 2026-08-02

| Feature | Requirements | Priority | Story Points (est.) |
|---------|-------------|----------|---------------------|
| Search & Filter | FR-009.1, FR-009.2 | P1 | 8 |
| Recurring Tasks | FR-010.1, FR-010.2 | P1 | 8 |

**Deliverables:**
- [ ] Full-text search with as-you-type results
- [ ] Combinable filters (list, priority, due date, status)
- [ ] Recurring task patterns (daily, weekly, monthly, custom)

---

### M6: v1.1 Release (Weeks 21–24) — Post-MVP

**Goal:** Dark mode, polish, and v1.1 release.
**Sprint mapping:** Sprint 11–12
**Due date:** 2026-09-06

| Feature | Requirements | Priority | Story Points (est.) |
|---------|-------------|----------|---------------------|
| Dark Mode | FR-011.1 | P1 | 3 |
| List Reorder | FR-004.5 | P1 | 3 |
| Due Time | FR-006.4 | P1 | 3 |
| PWA support | — | P1 | 5 |

**Deliverables:**
- [ ] System-aware dark/light/auto theme
- [ ] Drag-and-drop list reordering in sidebar
- [ ] Optional due time on tasks
- [ ] Progressive Web App (installable, offline-capable)
- [ ] v1.1 release

---

## Sprint-to-Milestone Mapping

| Sprint | Dates | Milestone | Focus | Epics | Points |
|--------|-------|-----------|-------|-------|--------|
| Sprint 1 | Mar 9–20 | M1: Architecture | Design docs, scaffold, DB, CI/CD | EPIC-001 | 16 |
| Sprint 2 | Mar 23–Apr 3 | M2: Core Features | Task CRUD (create, view, edit, complete, delete) | EPIC-003 | 15 |
| Sprint 3 | Apr 6–17 | M2: Core Features | Lists, priorities, Today View | EPIC-004, 005, 006 | 28 |
| Sprint 4 | Apr 20–May 1 | M3: MVP Release | Due dates, reminders, authentication | EPIC-002, 007 | 31 |
| Sprint 5 | May 4–15 | M3: MVP Release | Testing (80%/70%), security review | EPIC-008 | 13 |
| Sprint 6 | May 18–29 | M3: MVP Release | Deploy, polish, release v1.0 | EPIC-008 | 5+ |
| **Total** | | | | | **108** |

---

## Dependencies & Risks

### Cross-Milestone Dependencies

```
M1 Architecture ──────► M2 Core Features ──────► M3 MVP Release
     │                       │                        │
     │ scaffold + DB         │ task CRUD               │ auth + deploy
     │                       │                        │
     └───────────────────────┘                        │
     M1 must complete before                          │
     any feature development                          │
                                                      │
                             M2 Today View ───────────┘
                             depends on due dates
                             from M3 (Sprint 4)
```

**Critical path:** M1 scaffold → M2 task CRUD → M3 auth integration → M3 deploy

### Risk Timeline

| Risk | Affects | Sprint | Probability | Impact | Mitigation |
|------|---------|--------|------------|--------|------------|
| Sprint 3 overload (28 pts) | M2 | Sprint 3 | High | Medium | Allow spillover to Sprint 4; Today View can initially use `is_today` flag without due dates |
| Sprint 4 overload (31 pts) | M3 | Sprint 4 | High | High | Password reset email can use console output in dev; OAuth2 deferred to v1.1 |
| DDD overhead slows development | M1–M2 | Sprint 1–3 | Medium | Medium | Keep single bounded context; simplify if overhead > 30% of dev time |
| Auth complexity (JWT + refresh + lockout) | M3 | Sprint 4 | Medium | High | Use Spring Security starter defaults; minimize custom token logic |
| 80% coverage target is aggressive | M3 | Sprint 5 | Medium | Medium | Write tests alongside features (Sprint 2–4), not just in Sprint 5 |
| Deployment environment issues | M3 | Sprint 6 | Low | High | Keep deployment simple (Docker Compose); avoid Kubernetes for MVP |

---

## Resource Allocation

| Sprint | Backend | Frontend | DevOps/Infra | QA/Testing | Design Docs |
|--------|---------|----------|--------------|------------|-------------|
| Sprint 1 | 30% | 20% | 10% | 0% | 40% |
| Sprint 2 | 50% | 40% | 0% | 10% | 0% |
| Sprint 3 | 40% | 50% | 0% | 10% | 0% |
| Sprint 4 | 50% | 40% | 0% | 10% | 0% |
| Sprint 5 | 30% | 30% | 0% | 40% | 0% |
| Sprint 6 | 10% | 10% | 40% | 30% | 10% |

---

## Feature-to-Persona Impact Matrix

| Feature | Alex | Priya | Sam | Jordan | Maya | Taylor | Ravi |
|---------|------|-------|-----|--------|------|--------|------|
| Task CRUD | H | H | H | H | H | H | H |
| Today View | H | M | M | M | H | H | H |
| Task Lists | M | M | H | M | M | H | H |
| Priorities | H | M | M | L | M | M | H |
| Due Dates | H | M | H | M | H | H | H |
| Auth | H | H | H | H | H | H | H |
| Reminders | H | L | H | M | H | H | H |

*H = High impact, M = Medium, L = Low*

---

## GitHub Milestones

Milestones already created on `javbond/todo-app`:

| # | Milestone | Due Date | Status |
|---|-----------|----------|--------|
| 1 | M1: Architecture & Foundation | 2026-04-05 | Open |
| 2 | M2: Core Features | 2026-05-03 | Open |
| 3 | M3: MVP Release v1.0 | 2026-06-07 | Open |
| 4 | M4: Enhanced UX | 2026-07-05 | Open |
| 5 | M5: Power Features | 2026-08-02 | Open |
| 6 | M6: v1.1 Release | 2026-09-06 | Open |

---

*Generated by SDLC Factory — Phase 3: Project Setup*
*Next: Run `/sdlc next` to advance to the Design phase.*
