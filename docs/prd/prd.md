# Clarity (todo-app) — Product Requirements Document

**Project:** todo-app
**Domain:** Productivity / Personal Task Management
**Phase:** Requirements
**Created:** 2026-03-02
**Status:** Draft — Pending Review
**Based on:** docs/ideation/product-vision.md

---

## 1. Executive Summary

### Vision Statement

Clarity is a minimalist personal task management application for individual professionals who value simplicity over feature count. It delivers a distraction-free interface for capturing, organizing, and completing daily tasks — built on enterprise-grade architecture (Spring Boot 3.x, Angular 17+, PostgreSQL) but designed to feel like a well-crafted notebook.

### Problem Statement

| # | Problem | Who It Affects |
|---|---------|----------------|
| 1 | Feature-bloated todo apps create cognitive overload, causing users to spend more time configuring than completing tasks | Knowledge workers evaluating TickTick, ClickUp, Notion |
| 2 | Overdue task clutter in "Today" views erodes daily focus and makes planning feel punishing | Todoist users managing 20+ active tasks |
| 3 | Sync reliability issues break user trust — a single lost task causes permanent abandonment | Cross-device users on TickTick, Any.do |
| 4 | Subscription pricing ($48-60/year) creates friction for personal use | Students, freelancers, early-career professionals |
| 5 | Unreliable natural language parsing interrupts the quick-capture workflow | TickTick users relying on smart input |

### Proposed Solution

A free, cross-platform web application focused exclusively on the core task lifecycle: capture, organize, plan, complete. The MVP ships five P0 features (Task CRUD, Today View, Task Lists, Priority Levels, Due Dates) with a design benchmark of Things 3's aesthetic quality on an open, accessible stack.

### Timeline Overview

| Phase | Scope | Target |
|-------|-------|--------|
| **MVP (v1.0)** | P0 features: Task CRUD, Today View, Task Lists, Priorities, Due Dates | Month 1-3 |
| **Phase 2 (v1.1)** | P1 features: Upcoming View, Quick Add, Search/Filter, Recurring Tasks, Dark Mode | Month 4-6 |
| **Phase 3 (v2.0)** | P2 features: Drag-and-Drop, Subtasks, Analytics, Data Export | Month 7-9 |

---

## 2. Target Users (Personas)

| # | Persona | Role | Primary Goals | Pain Points |
|---|---------|------|---------------|-------------|
| 1 | **Alex the Freelancer** | Independent consultant, age 30, juggles 3-4 client projects | Capture tasks quickly between meetings; plan each morning in under 2 minutes; never miss a client deadline | Current tools (Todoist free tier) mix overdue tasks with today's plan; paying $48/year for a todo list feels wasteful |
| 2 | **Priya the Remote Developer** | Full-stack engineer at a startup, age 27, works from home | Track personal tasks separately from Jira/Linear work tickets; keyboard-first workflow; minimal context switching | Existing tools require too much mouse interaction; feature-heavy apps feel like "another project management tool" |
| 3 | **Sam the Graduate Student** | PhD candidate, age 25, manages coursework + research + TA duties | Organize tasks by category (Research, Coursework, Personal); see what's due this week at a glance | Cannot justify subscription costs; Apple Reminders is too basic; Notion is too complex for simple task tracking |
| 4 | **Jordan the Content Creator** | Freelance writer/YouTuber, age 33, manages editorial calendar and personal errands | Quick capture ideas and to-dos from phone or laptop; visually clean interface that doesn't feel corporate | Ad-supported free apps are distracting; TickTick's UI feels cluttered; wants something that "looks good" |
| 5 | **Maya the Early-Career PM** | Junior product manager at a mid-size company, age 24, first full-time role | Separate personal task management from work tools (Asana/Monday); build a daily planning habit | Overwhelmed by the number of features in productivity apps; wants something she can learn in 30 seconds |
| 6 | **Taylor the Part-Time Instructor** | Adjunct professor + freelance consultant, age 40, wears multiple hats | Track deadlines across teaching, consulting, and personal life in one place; recurring tasks for weekly prep | Existing apps don't clearly separate "today's focus" from the full backlog; overdue items create anxiety |
| 7 | **Ravi the Solopreneur** | Runs a one-person e-commerce business, age 35, manages everything alone | Daily task planning for order fulfillment, marketing, customer support; priority-based execution | Tools designed for teams (Asana, Monday) are overkill; simple tools (Apple Reminders) lack priority and filtering |

---

## 3. Functional Requirements

### FR-001: User Authentication

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-001.1 | Users can register with email and password | [P0] | Registration form validates email format and password strength (min 8 chars, 1 uppercase, 1 number). Confirmation message displayed on success. |
| FR-001.2 | Users can log in with email and password | [P0] | Login returns JWT access token (15 min expiry) and refresh token (7 day expiry). Invalid credentials show generic error message. |
| FR-001.3 | Users can log out from all devices | [P0] | Logout invalidates current refresh token. User is redirected to login page. |
| FR-001.4 | Users can reset their password via email | [P0] | Password reset email sent within 30 seconds. Reset link expires after 1 hour. |
| FR-001.5 | Account lockout after 5 failed login attempts | [P0] | Account locked for 15 minutes after 5 consecutive failures. User informed of lockout. |
| FR-001.6 | OAuth2 login via Google and GitHub | [P1] | Users can register/login with Google or GitHub. Account linking if email matches existing account. |

### FR-002: Task Management (CRUD)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-002.1 | Users can create a task with a title | [P0] | Title is required, max 255 characters. Task created with default priority (P4) and no due date. |
| FR-002.2 | Tasks support an optional description field | [P0] | Plain text description, max 2000 characters. Displayed below task title in detail view. |
| FR-002.3 | Users can edit task title, description, priority, due date, and list assignment | [P0] | All fields are independently editable. Changes persist immediately (optimistic UI update). |
| FR-002.4 | Users can mark a task as complete | [P0] | Completed tasks show a strikethrough visual. Completed tasks are hidden from active views by default but accessible via filter. |
| FR-002.5 | Users can delete a task | [P0] | Soft delete (is_deleted flag). Deleted tasks are not shown in any view. No undo in MVP. |
| FR-002.6 | Users can undo task completion within 5 seconds | [P0] | Toast notification with "Undo" action appears for 5 seconds after marking complete. Clicking undo restores the task to active. |
| FR-002.7 | Tasks display creation date and last modified date | [P0] | Timestamps shown in task detail view. Format: relative time ("2 hours ago") for recent, absolute date for older. |

### FR-003: Today View (Focus Mode)

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-003.1 | Today View shows only tasks with today's due date or manually added to today | [P0] | Default landing page after login. Only shows tasks intentionally planned for today. |
| FR-003.2 | Overdue tasks appear in a separate collapsible section | [P0] | Overdue section is collapsed by default. Badge shows count of overdue tasks. Expanding reveals the list. |
| FR-003.3 | Users can add any task to Today View regardless of due date | [P0] | "Add to Today" action available on any task. Removes from Today with "Remove from Today" action. |
| FR-003.4 | Completed tasks in Today View move to a "Done today" section | [P0] | Completed tasks shift to a bottom section with completion time. Provides a sense of daily progress. |
| FR-003.5 | Today View shows a task count summary | [P0] | Header displays "X of Y tasks completed today" with a subtle progress indicator. |

### FR-004: Task Lists / Categories

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-004.1 | Users can create custom task lists with a name and optional color | [P0] | List name required, max 50 characters. Color picker with 8 preset colors. Default list "Inbox" created on registration. |
| FR-004.2 | Users can rename and delete custom lists | [P0] | Deleting a list moves its tasks to "Inbox" (not deleted). Confirmation dialog before deletion. |
| FR-004.3 | Every task belongs to exactly one list | [P0] | Tasks default to "Inbox" if no list specified. Users can move tasks between lists. |
| FR-004.4 | Sidebar navigation displays all lists with task counts | [P0] | Lists shown in sidebar with unresolved task count badge. Clicking a list filters the main view. |
| FR-004.5 | Users can reorder lists in the sidebar | [P1] | Drag-and-drop reordering of lists. Order persists across sessions. |

### FR-005: Priority Levels

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-005.1 | Tasks support four priority levels: P1 (Urgent), P2 (High), P3 (Medium), P4 (Low/Default) | [P0] | Color-coded indicators: P1=red, P2=orange, P3=blue, P4=gray. Default is P4. |
| FR-005.2 | Users can change task priority from the task list view | [P0] | Single-click on priority indicator opens a dropdown. Change is immediate. |
| FR-005.3 | Tasks can be sorted by priority within any view | [P0] | Sort toggle in view header: by priority, by due date, by creation date. Default: priority then due date. |

### FR-006: Due Dates and Reminders

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-006.1 | Users can assign a due date to any task | [P0] | Date picker component. Date stored as UTC. Displayed in user's local timezone. |
| FR-006.2 | Tasks with past due dates are visually marked as overdue | [P0] | Overdue tasks show red due date text. Overdue indicator visible in all views. |
| FR-006.3 | Browser notification reminders at configurable intervals | [P0] | Default reminder: 30 minutes before due time. Options: at due time, 15min, 30min, 1hr, 1 day before. Requires browser notification permission. |
| FR-006.4 | Users can set a specific due time (not just date) | [P1] | Optional time picker alongside date picker. If no time set, task is "all day" and due at end of day. |

### FR-007: Upcoming View

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-007.1 | Chronological view grouping tasks by date: Today, Tomorrow, This Week, Next Week, Later | [P1] | Tasks without due dates shown under "No date" section at the bottom. |
| FR-007.2 | Users can assign a due date directly from the Upcoming View | [P1] | Drag a task to a date group or click to set date. |

### FR-008: Quick Add

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-008.1 | Global keyboard shortcut (Ctrl/Cmd + K) opens a minimal task creation modal | [P1] | Modal appears in < 100ms. Focus is on title input. Tab order: title, list, priority, due date, save. |
| FR-008.2 | Quick Add supports keyboard-only task creation | [P1] | Enter to save, Escape to cancel. No mouse required for basic task capture. |

### FR-009: Search and Filter

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-009.1 | Full-text search across task titles and descriptions | [P1] | Results appear as-you-type with > 3 characters. Results highlighted with matching text. |
| FR-009.2 | Filter tasks by list, priority, due date range, and completion status | [P1] | Filters combinable (AND logic). Active filters shown as removable chips. Filter state persists during session. |

### FR-010: Recurring Tasks

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-010.1 | Support recurrence patterns: daily, weekly, monthly, yearly, custom interval | [P1] | Custom supports "every N days/weeks/months." Recurrence set at task creation or edit time. |
| FR-010.2 | Completing a recurring task generates the next occurrence | [P1] | Next occurrence created with same title, description, priority, and list. Due date advanced per pattern. |

### FR-011: Dark Mode

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-011.1 | System-aware theme toggle (light/dark/auto) | [P1] | Auto follows OS preference. Manual override persists. Smooth CSS transition (200ms). |

### FR-012: Drag-and-Drop Reordering

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-012.1 | Users can reorder tasks within a list via drag-and-drop | [P2] | Custom order persists. Drag handle visible on hover. |

### FR-013: Subtasks

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-013.1 | Tasks support one level of nesting (parent → children) | [P2] | Parent task shows progress (e.g., "2/5 subtasks done"). Completing all subtasks does not auto-complete parent. |

### FR-014: Basic Analytics

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-014.1 | Dashboard showing tasks completed per day (7-day bar chart) and completion streak | [P2] | Data calculated from task completion timestamps. No gamification — informational only. |

### FR-015: Data Export

| ID | Requirement | Priority | Acceptance Criteria |
|----|-------------|----------|---------------------|
| FR-015.1 | Export all tasks as JSON or CSV | [P2] | Includes all fields: title, description, priority, due date, list, status, timestamps. Download triggered from settings page. |

---

## 4. Non-Functional Requirements

### NFR-001: Performance

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-001.1 | API response time for task CRUD operations | < 500ms (p95) | [P0] |
| NFR-001.2 | Initial page load (First Contentful Paint) | < 2 seconds on 4G connection | [P0] |
| NFR-001.3 | Time to Interactive after login | < 3 seconds | [P0] |
| NFR-001.4 | Task list rendering for up to 200 tasks | < 100ms (no perceptible lag) | [P0] |
| NFR-001.5 | Search results returned as-you-type | < 300ms from keystroke | [P1] |
| NFR-001.6 | Concurrent users supported | 100 simultaneous users (MVP) | [P0] |

### NFR-002: Security

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-002.1 | Authentication via JWT with short-lived access tokens | 15-minute access token, 7-day refresh token | [P0] |
| NFR-002.2 | Password hashing with BCrypt | Cost factor 12+ | [P0] |
| NFR-002.3 | All API endpoints require authentication (except /health, /auth/**) | 100% enforcement | [P0] |
| NFR-002.4 | HTTPS/TLS 1.2+ for all traffic | No plain HTTP in production | [P0] |
| NFR-002.5 | OWASP Top 10 compliance | No critical or high findings | [P0] |
| NFR-002.6 | Rate limiting on auth endpoints | 10 requests/minute per IP | [P0] |
| NFR-002.7 | CORS configured for specific origins only | No wildcard (*) in production | [P0] |
| NFR-002.8 | Security headers (CSP, HSTS, X-Frame-Options, X-Content-Type-Options) | All configured | [P0] |
| NFR-002.9 | Input validation on all API request bodies | Bean Validation on every DTO | [P0] |
| NFR-002.10 | No sensitive data in logs (passwords, tokens, PII) | Zero occurrences | [P0] |

### NFR-003: Availability & Reliability

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-003.1 | Application uptime | 99% (single-server deployment) | [P0] |
| NFR-003.2 | Data durability | Zero data loss for committed transactions | [P0] |
| NFR-003.3 | Graceful degradation if Redis is unavailable | App functions without caching (slower but operational) | [P1] |
| NFR-003.4 | Health check endpoint | /actuator/health returns 200 within 1 second | [P0] |

### NFR-004: Scalability

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-004.1 | Stateless backend (no server-side sessions) | JWT-based auth, no session affinity | [P0] |
| NFR-004.2 | Database connection pooling | HikariCP with configurable pool size | [P0] |
| NFR-004.3 | Architecture supports horizontal scaling | Additional backend instances behind load balancer (not required for MVP) | [P1] |

### NFR-005: Usability & Accessibility

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-005.1 | Keyboard navigation for all core workflows | Tab order, Enter to confirm, Escape to cancel | [P0] |
| NFR-005.2 | WCAG 2.1 Level AA compliance | Color contrast ratios, ARIA labels, screen reader compatibility | [P1] |
| NFR-005.3 | Responsive design (desktop + tablet + mobile) | Usable at 320px-2560px viewport widths | [P0] |
| NFR-005.4 | User onboarding time (registration to first task) | < 30 seconds | [P0] |

### NFR-006: Maintainability

| ID | Requirement | Target | Priority |
|----|-------------|--------|----------|
| NFR-006.1 | Backend test coverage | >= 80% line coverage (JaCoCo) | [P0] |
| NFR-006.2 | Frontend test coverage | >= 70% line coverage (Istanbul/Jest) | [P0] |
| NFR-006.3 | DDD layered architecture enforced | Domain layer has zero infrastructure imports | [P0] |
| NFR-006.4 | Conventional commits and PR-based workflow | All changes via reviewed PRs | [P0] |
| NFR-006.5 | Database migrations via Flyway | Versioned SQL migrations, no manual schema changes | [P0] |

---

## 5. Success Metrics (KPIs)

| # | Metric | Target (v1.0) | Target (v1.1) | Measurement Method |
|---|--------|---------------|---------------|-------------------|
| 1 | P0 features shipped and tested | 5/5 (F01-F05) | All P1 features added | GitHub Project board tracking |
| 2 | Backend test coverage | >= 80% | >= 80% maintained | JaCoCo report in CI pipeline |
| 3 | Frontend test coverage | >= 70% | >= 70% maintained | Istanbul/Jest report in CI pipeline |
| 4 | API response time (p95) | < 500ms | < 300ms | Spring Actuator + Prometheus metrics |
| 5 | Page load time (FCP) | < 2 seconds | < 1.5 seconds | Lighthouse CI audit |
| 6 | Security findings (critical/high) | 0 | 0 | OWASP Dependency-Check + security review |
| 7 | Application uptime | 99% | 99.5% | Health check endpoint monitoring |
| 8 | User onboarding time | < 30 seconds | < 20 seconds | Manual UX testing |
| 9 | Zero-bug launch (P1/P2 bugs in core flows) | 0 | 0 | QA test suite pass rate |
| 10 | Beta tester satisfaction | 5 testers report "fast and clean" | 10+ weekly active users | User interviews, feedback forms |

---

## 6. Roadmap & Milestones

```
  Month 1          Month 2          Month 3          Month 4          Month 5          Month 6
 ─────────────── ─────────────── ─────────────── ─────────────── ─────────────── ───────────────
│ SPRINT 1-2    │ SPRINT 3-4    │ SPRINT 5-6    │ SPRINT 7-8    │ SPRINT 9-10   │ SPRINT 11-12  │
│               │               │               │               │               │               │
│ [M1] DESIGN   │ [M2] CORE MVP │ [M3] MVP      │ [M4] P1       │ [M5] P1       │ [M6] P1       │
│ & SETUP       │               │ LAUNCH        │ START         │ COMPLETE      │ LAUNCH        │
│               │               │               │               │               │               │
│ - HLD/LLD/DDD│ - FR-002 CRUD │ - FR-006 Dates│ - FR-007      │ - FR-009      │ - FR-011 Dark │
│ - Tech specs  │ - FR-003 Today│ - FR-001 Auth │   Upcoming    │   Search      │   Mode        │
│ - DB schema   │ - FR-004 Lists│ - QA & bug fix│ - FR-008      │ - FR-010      │ - Polish &    │
│ - Project     │ - FR-005      │ - Security    │   Quick Add   │   Recurring   │   release     │
│   scaffold    │   Priority    │   review      │               │               │               │
│ - CI/CD setup │               │ - Deploy v1.0 │               │               │ - Deploy v1.1 │
 ─────────────── ─────────────── ─────────────── ─────────────── ─────────────── ───────────────

  MILESTONE 1       MILESTONE 2       MILESTONE 3       MILESTONE 4       MILESTONE 5       MILESTONE 6
  Architecture      Core Features     MVP Release       Enhanced UX       Power Features    v1.1 Release
  & Foundation      Complete          v1.0              & Navigation      & Automation

  ◆ Gate: Design    ◆ Gate: Dev       ◆ Gate: Release   ◆ Gate: Dev       ◆ Gate: Dev       ◆ Gate: Release
```

### Milestone Details

| Milestone | Sprint | Deliverables | Quality Gate |
|-----------|--------|-------------|-------------|
| **M1: Architecture & Foundation** | 1-2 | HLD, LLD, DDD, Tech Specs, DB Schema, Project Scaffold, CI/CD | Design gate: all design docs complete |
| **M2: Core Features** | 3-4 | Task CRUD, Today View, Task Lists, Priority Levels | Development gate: all P0 features pass unit tests |
| **M3: MVP Release (v1.0)** | 5-6 | Due Dates, Auth, Security Review, QA, Deployment | Release gate: 80% backend / 70% frontend coverage, 0 critical bugs |
| **M4: Enhanced UX** | 7-8 | Upcoming View, Quick Add keyboard shortcut | Development gate: features pass tests |
| **M5: Power Features** | 9-10 | Search/Filter, Recurring Tasks | Development gate: features pass tests |
| **M6: v1.1 Release** | 11-12 | Dark Mode, Polish, Performance Optimization, Deploy | Release gate: all P1 features tested and deployed |

---

## 7. Technical Constraints

### Technology Stack (Fixed)

| Layer | Technology | Version | Constraint |
|-------|-----------|---------|------------|
| Backend framework | Spring Boot | 3.x | Java 17+ required |
| Frontend framework | Angular | 17+ | Standalone components, signals API |
| Primary database | PostgreSQL | 15+ | Single instance for MVP |
| Cache layer | Redis | 7+ | Optional degradation if unavailable |
| Messaging | Kafka / RabbitMQ | — | Interfaces defined, not implemented in MVP |
| Architecture | DDD | — | Single bounded context (Task Management) for MVP |
| Build (backend) | Maven or Gradle | — | Standardized build pipeline |
| Build (frontend) | Angular CLI | — | ng build --configuration=production |
| VCS | Git + GitHub | — | PR-based workflow, conventional commits |

### Integration Constraints

- OAuth2 providers (Google, GitHub) require client credentials configuration
- Browser Notification API requires user permission and HTTPS
- No native mobile app — web-only for MVP
- No third-party analytics SDK — privacy-first approach

### Regulatory / Compliance

- GDPR awareness: users can export and delete their data (FR-015, FR-002.5)
- No PII in logs (NFR-002.10)
- Password storage follows OWASP guidelines (NFR-002.2)
- No regulatory certification required for MVP scope

### Resource Constraints

- Single-developer project augmented by AI SDLC Factory agents
- Single-server deployment (no Kubernetes, no microservices)
- Free-tier hosting acceptable for MVP (or self-hosted)

---

## 8. Assumptions & Dependencies

### Assumptions

| ID | Assumption | Risk if Wrong | Validation |
|----|-----------|---------------|------------|
| A1 | Individual professionals are underserved by free minimalist task managers | Product finds no audience | Beta user interviews (5+ testers) |
| A2 | Web-first approach is sufficient — native mobile not needed for MVP | Users abandon due to lack of mobile app | Monitor mobile access %. If > 50%, prioritize PWA. |
| A3 | DDD architecture overhead is acceptable for an MVP learning project | Development slows beyond sprint estimates | Track velocity. Simplify if DDD overhead > 30% of dev time. |
| A4 | Email/password auth is sufficient for MVP; OAuth2 can wait for v1.1 | Higher registration friction | Monitor registration dropout rate |
| A5 | 100 concurrent users is sufficient capacity for MVP | Server overload if product gains traction | Horizontal scaling architecture is ready but not deployed |
| A6 | Browser notifications are an acceptable substitute for mobile push | Low notification engagement | Track notification permission grant rates |

### External Dependencies

| Dependency | Type | Risk | Mitigation |
|------------|------|------|------------|
| PostgreSQL availability | Infrastructure | Database downtime = full outage | Health checks, connection pooling, automated restarts |
| Redis availability | Infrastructure | Cache miss = slower responses, not outage | Graceful fallback to database-only (NFR-003.3) |
| GitHub (VCS + CI/CD) | Development | GitHub outage blocks development | Local git workflow unaffected; CI/CD can wait |
| Browser Notification API | Platform | Users deny permission; Safari limitations | Fallback: in-app notification badge |
| Google/GitHub OAuth2 (v1.1) | Integration | Provider API changes or outage | Email/password remains primary auth method |

---

## Appendix A: Requirement Traceability Matrix

| Feature (Vision) | Functional Reqs | NFRs | Persona |
|-------------------|----------------|------|---------|
| F01: Task CRUD | FR-002.1 — FR-002.7 | NFR-001.1, NFR-001.4 | All |
| F02: Today View | FR-003.1 — FR-003.5 | NFR-001.2, NFR-005.1 | Alex, Taylor |
| F03: Task Lists | FR-004.1 — FR-004.5 | NFR-001.4 | Sam, Ravi |
| F04: Priority Levels | FR-005.1 — FR-005.3 | — | Ravi, Alex |
| F05: Due Dates | FR-006.1 — FR-006.4 | NFR-001.1 | All |
| F06: Upcoming View | FR-007.1 — FR-007.2 | NFR-001.2 | Taylor, Sam |
| F07: Quick Add | FR-008.1 — FR-008.2 | NFR-005.1 | Priya, Jordan |
| F08: Search/Filter | FR-009.1 — FR-009.2 | NFR-001.5 | All (20+ tasks) |
| F09: Recurring Tasks | FR-010.1 — FR-010.2 | — | Taylor, Alex |
| F10: Dark Mode | FR-011.1 | NFR-005.3 | Priya, Jordan |
| Auth | FR-001.1 — FR-001.6 | NFR-002.1 — NFR-002.10 | All |

---

## Appendix B: Glossary

| Term | Definition |
|------|-----------|
| **Task** | A single unit of work with a title, optional description, priority, due date, and list assignment |
| **Today View** | The default landing page showing tasks planned for today, with overdue items in a separate section |
| **Task List** | A user-created category for organizing tasks (e.g., "Work", "Personal") |
| **Inbox** | The default task list where tasks land if no list is specified |
| **Quick Add** | A keyboard-triggered modal for rapid task creation |
| **Recurring Task** | A task with a recurrence pattern that auto-generates the next occurrence on completion |
| **Focus Mode** | Synonym for Today View — emphasizes intentional daily planning |

---

*Generated by SDLC Factory — Phase 2: Requirements*
*Next: Run `/sdlc next` to advance to Project Setup, or run `/agile-backlog todo-app 6` to generate the sprint backlog.*
