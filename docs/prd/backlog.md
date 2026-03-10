# Clarity (todo-app) — Product Backlog

**Project:** todo-app
**Sprints:** 6 (2-week sprints, 3 months total)
**Scope:** MVP v1.0 (P0 features: F01–F06 + Auth)
**Created:** 2026-03-05
**Based on:** docs/prd/prd.md

---

## Backlog Hierarchy

```
PRODUCT BACKLOG
│
├── EPIC-001: Project Foundation & Infrastructure
│   ├── US-001: Project scaffolding (backend + frontend)
│   ├── US-002: Database schema and migrations
│   └── US-003: CI/CD pipeline setup
│
├── EPIC-002: User Authentication
│   ├── US-004: User registration
│   ├── US-005: User login with JWT
│   ├── US-006: Logout and token management
│   └── US-007: Password reset
│
├── EPIC-003: Task Management (CRUD)
│   ├── US-008: Create a task
│   ├── US-009: View and edit tasks
│   ├── US-010: Complete and undo task completion
│   └── US-011: Delete a task
│
├── EPIC-004: Task Lists & Categories
│   ├── US-012: Create and manage task lists
│   ├── US-013: Assign tasks to lists
│   └── US-014: Sidebar navigation with list counts
│
├── EPIC-005: Priority System
│   ├── US-015: Priority levels on tasks
│   └── US-016: Sort tasks by priority
│
├── EPIC-006: Today View (Focus Mode)
│   ├── US-017: Today View with daily tasks
│   ├── US-018: Overdue tasks section
│   └── US-019: Daily progress tracking
│
├── EPIC-007: Due Dates & Reminders
│   ├── US-020: Assign due dates
│   ├── US-021: Overdue visual indicators
│   └── US-022: Browser notification reminders
│
└── EPIC-008: MVP QA, Security & Release
    ├── US-023: End-to-end testing
    ├── US-024: Security hardening
    └── US-025: Production deployment
```

---

## Part 1: Epics

### EPIC-001: Project Foundation & Infrastructure

| Field | Value |
|-------|-------|
| **Business Value** | Establishes the architectural foundation (DDD), project scaffold, database schema, and CI/CD pipeline. All subsequent development depends on this. |
| **Success Metrics** | Backend builds with `mvn verify`. Frontend builds with `ng build`. CI pipeline runs on push. Database migrations apply cleanly. |
| **User Stories** | US-001, US-002, US-003 |
| **Dependencies** | Design phase artifacts (HLD, LLD, DDD, Tech Specs) must be complete |
| **Estimated Duration** | 1 sprint |
| **Target Milestone** | M1: Architecture & Foundation |

### EPIC-002: User Authentication

| Field | Value |
|-------|-------|
| **Business Value** | Users can securely register, log in, and manage their sessions. Required before any user-specific data (tasks, lists) can be created. |
| **Success Metrics** | Registration, login, logout, and password reset flows work end-to-end. JWT tokens issued and validated. Account lockout enforced after 5 failures. |
| **User Stories** | US-004, US-005, US-006, US-007 |
| **Dependencies** | EPIC-001 (project scaffold and database) |
| **Estimated Duration** | 1.5 sprints |
| **Target Milestone** | M3: MVP Release v1.0 |

### EPIC-003: Task Management (CRUD)

| Field | Value |
|-------|-------|
| **Business Value** | Core functionality — users can create, view, edit, complete, and delete tasks. This is the foundation every other feature builds on. |
| **Success Metrics** | Full task lifecycle works: create → edit → complete → undo → delete. Optimistic UI updates. Soft delete implemented. |
| **User Stories** | US-008, US-009, US-010, US-011 |
| **Dependencies** | EPIC-001 (scaffold), EPIC-002 (auth — tasks are user-scoped) |
| **Estimated Duration** | 1.5 sprints |
| **Target Milestone** | M2: Core Features |

### EPIC-004: Task Lists & Categories

| Field | Value |
|-------|-------|
| **Business Value** | Users can organize tasks into custom lists (Work, Personal, Errands). Provides the organizational structure personas like Sam and Ravi need. |
| **Success Metrics** | Users can create/rename/delete lists. Tasks belong to exactly one list. Sidebar shows lists with counts. Default "Inbox" list exists. |
| **User Stories** | US-012, US-013, US-014 |
| **Dependencies** | EPIC-003 (tasks must exist to be categorized) |
| **Estimated Duration** | 1 sprint |
| **Target Milestone** | M2: Core Features |

### EPIC-005: Priority System

| Field | Value |
|-------|-------|
| **Business Value** | Users can assign and visualize priority levels (P1–P4) on tasks. Enables quick scanning and priority-based execution for users like Alex and Ravi. |
| **Success Metrics** | Four color-coded priority levels. Single-click priority change. Sort by priority in any view. |
| **User Stories** | US-015, US-016 |
| **Dependencies** | EPIC-003 (tasks must exist) |
| **Estimated Duration** | 0.5 sprints |
| **Target Milestone** | M2: Core Features |

### EPIC-006: Today View (Focus Mode)

| Field | Value |
|-------|-------|
| **Business Value** | The primary differentiator — a clean daily focus view that separates today's intentional plan from overdue backlog. Directly addresses Problem P2 from the vision. |
| **Success Metrics** | Today View is the default landing page. Overdue tasks in a collapsible section. "Done today" section shows progress. "X of Y completed" counter. |
| **User Stories** | US-017, US-018, US-019 |
| **Dependencies** | EPIC-003 (tasks), EPIC-007 (due dates drive the Today View) |
| **Estimated Duration** | 1 sprint |
| **Target Milestone** | M2: Core Features |

### EPIC-007: Due Dates & Reminders

| Field | Value |
|-------|-------|
| **Business Value** | Users can set deadlines and receive browser notification reminders. Due dates drive the Today View and overdue indicators. |
| **Success Metrics** | Date picker works. Overdue tasks visually flagged in red. Browser notifications fire at configured intervals. |
| **User Stories** | US-020, US-021, US-022 |
| **Dependencies** | EPIC-003 (tasks must exist) |
| **Estimated Duration** | 1 sprint |
| **Target Milestone** | M3: MVP Release v1.0 |

### EPIC-008: MVP QA, Security & Release

| Field | Value |
|-------|-------|
| **Business Value** | Ensures the MVP meets quality gates: 80% backend / 70% frontend test coverage, zero critical security findings, and production-ready deployment. |
| **Success Metrics** | All tests pass. Coverage thresholds met. Security review completed with no critical/high findings. Application deployed and accessible. |
| **User Stories** | US-023, US-024, US-025 |
| **Dependencies** | All prior epics |
| **Estimated Duration** | 1 sprint |
| **Target Milestone** | M3: MVP Release v1.0 |

---

## Part 2: User Stories

### US-001: Project Scaffolding

| Field | Value |
|-------|-------|
| **Story** | **As a** developer, **I want to** have a scaffolded Spring Boot backend and Angular frontend with DDD package structure, **so that** I can start implementing features on a solid architectural foundation. |
| **Epic** | EPIC-001 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-1` `backend` `frontend` `infrastructure` |
| **Points** | 8 |

**Acceptance Criteria:**
- [ ] Given the backend directory, When I run `mvn verify`, Then the build succeeds with zero errors
- [ ] Given the frontend directory, When I run `ng build`, Then the production build succeeds
- [ ] Given the backend, When I inspect the package structure, Then it follows DDD layering: `domain/`, `application/`, `infrastructure/`, `api/` within a `task` bounded context
- [ ] Given the frontend, When I inspect the structure, Then it has `core/`, `shared/`, `features/`, and `layouts/` directories
- [ ] Given both projects, When I check configuration, Then Spring Boot uses `application.yml` with `dev`/`prod` profiles, and Angular uses environment files

**Tasks:**
- [ ] TASK-001: Initialize Spring Boot 3.x project with Maven, Java 17, and DDD package structure
- [ ] TASK-002: Configure Spring Boot dependencies (Web, JPA, Security, Validation, PostgreSQL, Redis, Flyway, MapStruct, Lombok)
- [ ] TASK-003: Initialize Angular 17+ project with standalone components, routing, and feature module structure
- [ ] TASK-004: Configure Angular dev proxy to Spring Boot backend
- [ ] TASK-005: Create Docker Compose for local development (PostgreSQL, Redis)

---

### US-002: Database Schema and Migrations

| Field | Value |
|-------|-------|
| **Story** | **As a** developer, **I want to** have Flyway migrations defining the initial database schema for users, tasks, and task lists, **so that** the database is version-controlled and reproducible. |
| **Epic** | EPIC-001 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-1` `backend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given the backend, When I start the application, Then Flyway applies migrations and creates `users`, `tasks`, and `task_lists` tables
- [ ] Given the schema, When I inspect tables, Then all have `id` (UUID), `created_at`, `updated_at`, `created_by`, `updated_by` audit columns
- [ ] Given the `tasks` table, Then it has columns: `title`, `description`, `priority`, `due_date`, `is_completed`, `is_deleted`, `is_today`, `completed_at`, `list_id`, `user_id`
- [ ] Given the `task_lists` table, Then it has columns: `name`, `color`, `sort_order`, `user_id`
- [ ] Given the `users` table, Then it has columns: `email`, `password_hash`, `display_name`, `is_locked`, `failed_login_attempts`, `locked_until`

**Tasks:**
- [ ] TASK-006: Create `V1__create_users_table.sql` migration
- [ ] TASK-007: Create `V2__create_task_lists_table.sql` migration
- [ ] TASK-008: Create `V3__create_tasks_table.sql` migration
- [ ] TASK-009: Create JPA entity classes mapped to the schema (infrastructure layer)
- [ ] TASK-010: Create domain model classes (entities, value objects) in domain layer

---

### US-003: CI/CD Pipeline Setup

| Field | Value |
|-------|-------|
| **Story** | **As a** developer, **I want to** have CI workflows that build, test, and scan both backend and frontend on every push, **so that** code quality is enforced automatically. |
| **Epic** | EPIC-001 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-1` `infrastructure` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given a push to `main` or `develop`, When CI runs, Then backend builds and tests pass
- [ ] Given a push, When CI runs, Then frontend lints, tests, and builds pass
- [ ] Given a push to `main`, When the security workflow runs, Then OWASP dependency check and secret scan execute

**Tasks:**
- [ ] TASK-011: Verify and update `.github/workflows/ci.yml` for actual project structure
- [ ] TASK-012: Verify and update `.github/workflows/security-scan.yml`
- [ ] TASK-013: Add test job output artifacts (JaCoCo report, coverage summary)

---

### US-004: User Registration

| Field | Value |
|-------|-------|
| **Story** | **As a** new user, **I want to** register with my email and password, **so that** I can create an account and start managing my tasks. |
| **Epic** | EPIC-002 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-5` `backend` `frontend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given the registration page, When I enter a valid email and strong password (min 8 chars, 1 uppercase, 1 number), Then my account is created and I see a success message
- [ ] Given registration, When I submit an email that already exists, Then I see a generic error (not revealing if email exists)
- [ ] Given registration success, When my account is created, Then a default "Inbox" task list is created for me
- [ ] Given the backend, When a user is stored, Then the password is hashed with BCrypt (cost factor 12+)

**Tasks:**
- [ ] TASK-014: Implement `UserRegistrationCommand` and `AuthApplicationService.register()`
- [ ] TASK-015: Create `POST /api/auth/register` endpoint with validation
- [ ] TASK-016: Create registration page component with reactive form and validation messages
- [ ] TASK-017: Auto-create default "Inbox" list on registration via domain event

---

### US-005: User Login with JWT

| Field | Value |
|-------|-------|
| **Story** | **As a** registered user, **I want to** log in with my email and password and receive a JWT token, **so that** I can securely access my tasks. |
| **Epic** | EPIC-002 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-5` `backend` `frontend` |
| **Points** | 8 |

**Acceptance Criteria:**
- [ ] Given valid credentials, When I log in, Then I receive a JWT access token (15 min expiry) and a refresh token (7 day expiry)
- [ ] Given invalid credentials, When I log in, Then I see a generic "Invalid email or password" error
- [ ] Given 5 failed login attempts, When I try a 6th time, Then my account is locked for 15 minutes
- [ ] Given a valid access token, When I call any API endpoint, Then the request is authenticated
- [ ] Given an expired access token, When I call `/api/auth/refresh` with a valid refresh token, Then I receive a new access token

**Tasks:**
- [ ] TASK-018: Implement Spring Security configuration with JWT filter
- [ ] TASK-019: Create `JwtTokenProvider` service (generate, validate, refresh)
- [ ] TASK-020: Create `POST /api/auth/login` and `POST /api/auth/refresh` endpoints
- [ ] TASK-021: Implement account lockout logic in `AuthApplicationService`
- [ ] TASK-022: Create login page component with error handling
- [ ] TASK-023: Create Angular HTTP interceptor for JWT token attachment
- [ ] TASK-024: Create Angular auth guard for protected routes

---

### US-006: Logout and Token Management

| Field | Value |
|-------|-------|
| **Story** | **As a** logged-in user, **I want to** log out and have my session invalidated, **so that** my account is secure when I'm done. |
| **Epic** | EPIC-002 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-5` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given I am logged in, When I click logout, Then my refresh token is invalidated and I am redirected to the login page
- [ ] Given a logged-out state, When I try to access a protected route, Then I am redirected to login

**Tasks:**
- [ ] TASK-025: Create `POST /api/auth/logout` endpoint that invalidates refresh token
- [ ] TASK-026: Implement token blacklist/invalidation in Redis
- [ ] TASK-027: Add logout button to app layout and wire to auth service

---

### US-007: Password Reset

| Field | Value |
|-------|-------|
| **Story** | **As a** user who forgot my password, **I want to** reset it via email, **so that** I can regain access to my account. |
| **Epic** | EPIC-002 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-5` `backend` `frontend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given the forgot password page, When I enter my email, Then I see "If an account exists, a reset link has been sent" (no email enumeration)
- [ ] Given a reset link, When I click it within 1 hour, Then I can set a new password
- [ ] Given a reset link older than 1 hour, When I click it, Then I see "This link has expired"

**Tasks:**
- [ ] TASK-028: Create `POST /api/auth/forgot-password` and `POST /api/auth/reset-password` endpoints
- [ ] TASK-029: Implement password reset token generation and storage (1-hour expiry)
- [ ] TASK-030: Create forgot-password and reset-password page components
- [ ] TASK-031: Integrate email service (placeholder/log in dev, real SMTP in prod)

---

### US-008: Create a Task

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** create a new task with a title, **so that** I can track things I need to do. |
| **Epic** | EPIC-003 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-3` `backend` `frontend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given the task list view, When I click "Add task" or press Enter in the inline input, Then a new task is created with the entered title
- [ ] Given task creation, When I provide only a title, Then the task defaults to priority P4, no due date, assigned to the current list (or Inbox)
- [ ] Given task creation, When I leave the title empty, Then validation prevents creation with an error message
- [ ] Given task creation, When the task is saved, Then it appears at the top of the current list immediately (optimistic UI)

**Tasks:**
- [ ] TASK-032: Create `Task` aggregate root in domain layer with factory method
- [ ] TASK-033: Create `TaskRepository` interface (domain) and `JpaTaskRepository` implementation (infrastructure)
- [ ] TASK-034: Create `CreateTaskCommand` and `TaskApplicationService.createTask()`
- [ ] TASK-035: Create `POST /api/tasks` endpoint with validation
- [ ] TASK-036: Create task creation UI component (inline input + form)
- [ ] TASK-037: Create Angular `TaskService` and NgRx store (actions, reducer, effects, selectors)

---

### US-009: View and Edit Tasks

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** view my task list and edit any task's details, **so that** I can keep my tasks up to date. |
| **Epic** | EPIC-003 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-3` `backend` `frontend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given I am logged in, When I navigate to a task list, Then I see all active (non-deleted, non-completed) tasks sorted by priority then due date
- [ ] Given a task, When I click on it, Then a detail panel opens showing title, description, priority, due date, list, and timestamps
- [ ] Given the detail panel, When I edit any field and click away, Then the change is saved immediately (optimistic UI)
- [ ] Given a task, When I view timestamps, Then I see "created 2 hours ago" (relative) or "Mar 5, 2026" (absolute for older)

**Tasks:**
- [ ] TASK-038: Create `GET /api/tasks` endpoint with filtering (listId, completed, deleted)
- [ ] TASK-039: Create `GET /api/tasks/{id}` endpoint for task detail
- [ ] TASK-040: Create `PUT /api/tasks/{id}` endpoint for task updates
- [ ] TASK-041: Create task list view component with virtual scrolling (up to 200 tasks)
- [ ] TASK-042: Create task detail panel component with inline editing
- [ ] TASK-043: Implement optimistic UI updates in NgRx effects

---

### US-010: Complete and Undo Task Completion

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** mark a task as complete and undo it within 5 seconds, **so that** I can track my progress and recover from mistakes. |
| **Epic** | EPIC-003 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-3` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given an active task, When I click the checkbox, Then the task shows a strikethrough and fades with animation
- [ ] Given a just-completed task, When the toast appears, Then I have 5 seconds to click "Undo" to restore it
- [ ] Given a completed task, When the undo window expires, Then the task moves to the completed section
- [ ] Given the backend, When a task is completed, Then `is_completed` is set to true and `completed_at` is recorded

**Tasks:**
- [ ] TASK-044: Create `PATCH /api/tasks/{id}/complete` and `PATCH /api/tasks/{id}/uncomplete` endpoints
- [ ] TASK-045: Implement task completion animation and strikethrough styling
- [ ] TASK-046: Create toast notification component with undo action and 5-second timer

---

### US-011: Delete a Task

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** delete a task I no longer need, **so that** my task list stays clean. |
| **Epic** | EPIC-003 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-3` `backend` `frontend` |
| **Points** | 2 |

**Acceptance Criteria:**
- [ ] Given a task, When I click the delete action, Then the task is soft-deleted and disappears from all views
- [ ] Given the backend, When a task is deleted, Then `is_deleted` is set to true (not physically removed)
- [ ] Given any view, When tasks are loaded, Then soft-deleted tasks are excluded from results

**Tasks:**
- [ ] TASK-047: Create `DELETE /api/tasks/{id}` endpoint (soft delete)
- [ ] TASK-048: Add delete action to task context menu / detail panel
- [ ] TASK-049: Update task queries to exclude deleted tasks

---

### US-012: Create and Manage Task Lists

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** create custom task lists with names and colors, **so that** I can organize my tasks into categories like Work, Personal, and Errands. |
| **Epic** | EPIC-004 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given the sidebar, When I click "New list", Then a form appears to enter a list name (max 50 chars) and pick a color (8 presets)
- [ ] Given an existing list, When I right-click or click the menu, Then I can rename or delete the list
- [ ] Given I delete a list, When confirmed, Then all tasks in that list move to "Inbox"
- [ ] Given registration, When my account is created, Then a default "Inbox" list exists that cannot be deleted

**Tasks:**
- [ ] TASK-050: Create `TaskList` aggregate root in domain layer
- [ ] TASK-051: Create `TaskListRepository` interface and JPA implementation
- [ ] TASK-052: Create CRUD endpoints: `POST/GET/PUT/DELETE /api/lists`
- [ ] TASK-053: Create list creation/edit form component with color picker
- [ ] TASK-054: Implement list deletion with task migration to Inbox

---

### US-013: Assign Tasks to Lists

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** assign a task to a specific list and move tasks between lists, **so that** my tasks are properly organized. |
| **Epic** | EPIC-004 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given task creation, When no list is selected, Then the task is assigned to "Inbox"
- [ ] Given a task's detail panel, When I change the list dropdown, Then the task moves to the new list immediately
- [ ] Given a list view, When I view tasks, Then only tasks assigned to that list are shown

**Tasks:**
- [ ] TASK-055: Add list selection dropdown to task creation and edit forms
- [ ] TASK-056: Create `PATCH /api/tasks/{id}/move` endpoint for list reassignment
- [ ] TASK-057: Update task list view to filter by selected list

---

### US-014: Sidebar Navigation with List Counts

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** see all my lists in a sidebar with active task counts, **so that** I can quickly navigate between lists and see where my tasks are. |
| **Epic** | EPIC-004 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given the app layout, When I look at the sidebar, Then I see "Today", "All Tasks", and all custom lists
- [ ] Given each list in the sidebar, When tasks change, Then the count badge updates in real-time
- [ ] Given a sidebar list item, When I click it, Then the main view filters to that list

**Tasks:**
- [ ] TASK-058: Create sidebar layout component with navigation items
- [ ] TASK-059: Create `GET /api/lists/counts` endpoint returning list IDs with active task counts
- [ ] TASK-060: Implement sidebar list selection with route-based navigation

---

### US-015: Priority Levels on Tasks

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** assign priority levels (P1–P4) to tasks with color-coded indicators, **so that** I can quickly identify what's most important. |
| **Epic** | EPIC-005 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given a task in the list view, When I click the priority indicator, Then a dropdown shows P1 (red), P2 (orange), P3 (blue), P4 (gray)
- [ ] Given a priority change, When I select a new level, Then the indicator color updates immediately
- [ ] Given task creation, When no priority is set, Then the task defaults to P4

**Tasks:**
- [ ] TASK-061: Create `Priority` value object in domain layer with color mapping
- [ ] TASK-062: Create priority indicator component with dropdown
- [ ] TASK-063: Wire priority changes to `PATCH /api/tasks/{id}` endpoint

---

### US-016: Sort Tasks by Priority

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** sort tasks by priority, due date, or creation date, **so that** I can view my tasks in the order most useful to me. |
| **Epic** | EPIC-005 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given any task view, When I click the sort toggle, Then I can choose: priority, due date, or creation date
- [ ] Given sort by priority, Then tasks order P1 → P2 → P3 → P4, with due date as tiebreaker
- [ ] Given the default sort, Then tasks sort by priority first, then by due date ascending

**Tasks:**
- [ ] TASK-064: Add `sort` query parameter to `GET /api/tasks` (priority, dueDate, createdAt)
- [ ] TASK-065: Create sort toggle component in task list header
- [ ] TASK-066: Persist sort preference in local storage

---

### US-017: Today View with Daily Tasks

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** see a Today View showing only tasks I've planned for today, **so that** I can focus on what matters right now without distraction. |
| **Epic** | EPIC-006 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given I log in, When the app loads, Then I land on the Today View by default
- [ ] Given the Today View, When I view tasks, Then I see only tasks with today's due date OR tasks manually marked as "today"
- [ ] Given any task, When I click "Add to Today", Then it appears in my Today View
- [ ] Given a task in Today View, When I click "Remove from Today", Then it disappears from this view (but remains in its list)

**Tasks:**
- [ ] TASK-067: Create `GET /api/tasks/today` endpoint filtering by due date = today OR `is_today` = true
- [ ] TASK-068: Create `PATCH /api/tasks/{id}/today` endpoint to toggle `is_today` flag
- [ ] TASK-069: Create Today View page component as default route
- [ ] TASK-070: Add "Add to Today" / "Remove from Today" action to task context menu

---

### US-018: Overdue Tasks Section

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** see overdue tasks in a separate collapsible section in my Today View, **so that** past-due items don't clutter my daily focus. |
| **Epic** | EPIC-006 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given the Today View, When overdue tasks exist, Then a "Overdue (N)" collapsible section appears above today's tasks
- [ ] Given the overdue section, When it first loads, Then it is collapsed by default
- [ ] Given I expand the overdue section, When I see a task, Then I can reschedule it to today or another date

**Tasks:**
- [ ] TASK-071: Create collapsible section component with count badge
- [ ] TASK-072: Modify `GET /api/tasks/today` to include overdue tasks as a separate group
- [ ] TASK-073: Add "Reschedule to today" quick action on overdue tasks

---

### US-019: Daily Progress Tracking

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** see how many tasks I've completed today and a "Done today" section, **so that** I feel a sense of accomplishment. |
| **Epic** | EPIC-006 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-4` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given the Today View header, When tasks are present, Then I see "X of Y tasks completed today"
- [ ] Given a completed task, When it was completed today, Then it moves to a "Done today" section at the bottom
- [ ] Given the progress counter, When a task is completed or uncompleted, Then the counter updates immediately

**Tasks:**
- [ ] TASK-074: Create progress counter component with animated number transition
- [ ] TASK-075: Create "Done today" section showing tasks completed today with completion times
- [ ] TASK-076: Wire progress counter to NgRx selectors for real-time updates

---

### US-020: Assign Due Dates

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** set a due date on any task, **so that** I can plan when things need to be done. |
| **Epic** | EPIC-007 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-5` `backend` `frontend` |
| **Points** | 3 |

**Acceptance Criteria:**
- [ ] Given a task, When I click the due date field, Then a date picker opens
- [ ] Given a date selection, When I pick a date, Then the due date is saved and displayed in my local timezone
- [ ] Given a due date, When I want to remove it, Then I can clear the date field

**Tasks:**
- [ ] TASK-077: Create date picker component (or integrate Angular Material datepicker)
- [ ] TASK-078: Add due date display to task list items (formatted relative or absolute)
- [ ] TASK-079: Store due dates as UTC in PostgreSQL, convert to local timezone in frontend

---

### US-021: Overdue Visual Indicators

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** see overdue tasks highlighted in red, **so that** I can immediately identify what's past due. |
| **Epic** | EPIC-007 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-5` `backend` `frontend` |
| **Points** | 2 |

**Acceptance Criteria:**
- [ ] Given a task with a past due date, When I view it in any list, Then the due date text is red
- [ ] Given a task due today, When I view it, Then the due date text is amber/warning color
- [ ] Given a task due in the future, When I view it, Then the due date text is default color

**Tasks:**
- [ ] TASK-080: Create due date display pipe with overdue/today/future color logic
- [ ] TASK-081: Apply overdue styling consistently across all task views

---

### US-022: Browser Notification Reminders

| Field | Value |
|-------|-------|
| **Story** | **As a** user, **I want to** receive browser notifications before a task is due, **so that** I don't miss deadlines. |
| **Epic** | EPIC-007 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-5` `backend` `frontend` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given a task with a due date, When I set a reminder, Then I can choose: at due time, 15min, 30min, 1hr, or 1 day before
- [ ] Given a reminder is set, When the time arrives, Then a browser notification fires with the task title
- [ ] Given the browser, When notification permission is not granted, Then the app prompts for permission and falls back to an in-app badge

**Tasks:**
- [ ] TASK-082: Create reminder settings UI on task detail (interval dropdown)
- [ ] TASK-083: Implement browser Notification API integration in Angular service
- [ ] TASK-084: Create scheduling logic (check reminders on a polling interval or Service Worker timer)
- [ ] TASK-085: Handle notification permission request flow gracefully

---

### US-023: End-to-End Testing

| Field | Value |
|-------|-------|
| **Story** | **As a** developer, **I want to** have comprehensive test coverage (80% backend, 70% frontend), **so that** the MVP meets quality gates before release. |
| **Epic** | EPIC-008 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-6` `backend` `frontend` |
| **Points** | 8 |

**Acceptance Criteria:**
- [ ] Given the backend, When I run `mvn verify`, Then JaCoCo reports >= 80% line coverage
- [ ] Given the frontend, When I run `ng test --code-coverage`, Then Istanbul reports >= 70% line coverage
- [ ] Given all test suites, When they run, Then there are zero test failures
- [ ] Given integration tests, When they run against a test database, Then all CRUD flows pass

**Tasks:**
- [ ] TASK-086: Write unit tests for domain layer (Task aggregate, value objects)
- [ ] TASK-087: Write unit tests for application services (create, update, complete, delete flows)
- [ ] TASK-088: Write integration tests for REST endpoints (MockMvc)
- [ ] TASK-089: Write frontend component tests (task list, task detail, today view, sidebar)
- [ ] TASK-090: Write frontend service tests (TaskService, AuthService, ListService)

---

### US-024: Security Hardening

| Field | Value |
|-------|-------|
| **Story** | **As a** developer, **I want to** pass a security review with no critical/high findings, **so that** the MVP is safe for real users. |
| **Epic** | EPIC-008 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-6` `security` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given the security review, When OWASP Top 10 is checked, Then no critical or high findings exist
- [ ] Given all API endpoints, When tested, Then they require authentication (except `/api/auth/**` and `/actuator/health`)
- [ ] Given the application, When deployed, Then security headers are configured (CSP, HSTS, X-Frame-Options)
- [ ] Given the code, When scanned, Then no secrets, tokens, or credentials are hardcoded

**Tasks:**
- [ ] TASK-091: Configure Spring Security with endpoint authorization rules
- [ ] TASK-092: Add security headers filter (CSP, HSTS, X-Frame-Options, X-Content-Type-Options)
- [ ] TASK-093: Add rate limiting on auth endpoints (10 requests/minute per IP)
- [ ] TASK-094: Run `/security-review` and remediate findings
- [ ] TASK-095: Verify no PII in logs, no secrets in code

---

### US-025: Production Deployment

| Field | Value |
|-------|-------|
| **Story** | **As a** developer, **I want to** deploy the MVP to a production environment, **so that** beta testers can use the application. |
| **Epic** | EPIC-008 |
| **Priority** | [P0] |
| **Labels** | `user-story` `P0` `sprint-6` `infrastructure` |
| **Points** | 5 |

**Acceptance Criteria:**
- [ ] Given the application, When deployed, Then it is accessible via a public URL
- [ ] Given the deployment, When the health check is called, Then `/actuator/health` returns 200
- [ ] Given production config, When the app starts, Then it uses production database, Redis, and HTTPS
- [ ] Given a Dockerfile, When built, Then backend and frontend are containerized

**Tasks:**
- [ ] TASK-096: Create Dockerfile for backend (multi-stage build)
- [ ] TASK-097: Create Dockerfile for frontend (nginx serving Angular build)
- [ ] TASK-098: Create `docker-compose.prod.yml` with PostgreSQL, Redis, backend, frontend
- [ ] TASK-099: Configure production `application-prod.yml` with externalized secrets
- [ ] TASK-100: Deploy and verify health check, SSL, and basic smoke tests

---

## Part 3: Sprint Plans

### SPRINT 1: Architecture & Design

| Field | Value |
|-------|-------|
| **Goal** | Complete all design phase artifacts (HLD, LLD, DDD, Tech Specs) and scaffold the project |
| **Duration** | 2 weeks |
| **Start** | 2026-03-09 |
| **End** | 2026-03-20 |
| **Story Points** | 16 |

| Priority | Story | Title | Points | Labels |
|----------|-------|-------|--------|--------|
| P0 | US-001 | Project scaffolding (backend + frontend) | 8 | `backend` `frontend` `infrastructure` |
| P0 | US-002 | Database schema and migrations | 5 | `backend` |
| P0 | US-003 | CI/CD pipeline setup | 3 | `infrastructure` |

**Sprint Goals:**
1. All design docs complete (HLD, LLD, DDD, Tech Specs) — via `/hld`, `/lld`, `/ddd-architect`, `/tech-specs`
2. Backend and frontend scaffolded with DDD structure
3. Database migrations applied, CI pipeline green

**Risks:**
- Design docs may take longer than expected if requirements are unclear
- Docker Compose setup may hit environment-specific issues

---

### SPRINT 2: Task CRUD (Backend + Frontend)

| Field | Value |
|-------|-------|
| **Goal** | Implement full task CRUD lifecycle — create, view, edit, complete, undo, delete |
| **Duration** | 2 weeks |
| **Start** | 2026-03-23 |
| **End** | 2026-04-03 |
| **Story Points** | 15 |

| Priority | Story | Title | Points | Labels |
|----------|-------|-------|--------|--------|
| P0 | US-008 | Create a task | 5 | `backend` `frontend` |
| P0 | US-009 | View and edit tasks | 5 | `backend` `frontend` |
| P0 | US-010 | Complete and undo task completion | 3 | `backend` `frontend` |
| P0 | US-011 | Delete a task | 2 | `backend` `frontend` |

**Sprint Goals:**
1. Full task CRUD working end-to-end (API + UI)
2. NgRx store operational with optimistic updates
3. Task domain model and repository fully implemented

**Risks:**
- Auth not yet in place — use a test user or mock auth for Sprint 2
- Optimistic UI updates may require extra frontend complexity

---

### SPRINT 3: Task Lists, Priorities & Today View

| Field | Value |
|-------|-------|
| **Goal** | Implement task organization (lists, priorities) and the core Today View |
| **Duration** | 2 weeks |
| **Start** | 2026-04-06 |
| **End** | 2026-04-17 |
| **Story Points** | 28 |

| Priority | Story | Title | Points | Labels |
|----------|-------|-------|--------|--------|
| P0 | US-012 | Create and manage task lists | 5 | `backend` `frontend` |
| P0 | US-013 | Assign tasks to lists | 3 | `backend` `frontend` |
| P0 | US-014 | Sidebar navigation with list counts | 3 | `frontend` |
| P0 | US-015 | Priority levels on tasks | 3 | `backend` `frontend` |
| P0 | US-016 | Sort tasks by priority | 3 | `backend` `frontend` |
| P0 | US-017 | Today View with daily tasks | 5 | `backend` `frontend` |
| P0 | US-018 | Overdue tasks section | 3 | `frontend` |
| P0 | US-019 | Daily progress tracking | 3 | `frontend` |

**Sprint Goals:**
1. Full task organization: lists, priorities, sidebar navigation
2. Today View functional as the default landing page
3. Overdue task separation and daily progress counter working

**Risks:**
- This is the highest point sprint (28 pts) — some stories may spill into Sprint 4
- Today View depends on due dates — due date assignment (US-020) is in Sprint 4

**Mitigation:** Today View can initially work with `is_today` flag only. Due date integration added in Sprint 4.

---

### SPRINT 4: Due Dates & Authentication

| Field | Value |
|-------|-------|
| **Goal** | Implement due dates with reminders and full authentication flow |
| **Duration** | 2 weeks |
| **Start** | 2026-04-20 |
| **End** | 2026-05-01 |
| **Story Points** | 31 |

| Priority | Story | Title | Points | Labels |
|----------|-------|-------|--------|--------|
| P0 | US-020 | Assign due dates | 3 | `backend` `frontend` |
| P0 | US-021 | Overdue visual indicators | 2 | `frontend` |
| P0 | US-022 | Browser notification reminders | 5 | `frontend` |
| P0 | US-004 | User registration | 5 | `backend` `frontend` |
| P0 | US-005 | User login with JWT | 8 | `backend` `frontend` |
| P0 | US-006 | Logout and token management | 3 | `backend` `frontend` |
| P0 | US-007 | Password reset | 5 | `backend` `frontend` |

**Sprint Goals:**
1. Due dates, overdue indicators, and reminders fully functional
2. Complete auth flow: register, login, logout, password reset
3. JWT tokens, refresh tokens, and account lockout working

**Risks:**
- Highest point sprint (31 pts) — authentication is complex
- Browser notification permission UX needs careful handling

**Mitigation:** Password reset (US-007) can use console/log output for email in dev mode. Full email integration deferred to deployment.

---

### SPRINT 5: Integration, Testing & Security

| Field | Value |
|-------|-------|
| **Goal** | Achieve test coverage targets, pass security review, integration testing |
| **Duration** | 2 weeks |
| **Start** | 2026-05-04 |
| **End** | 2026-05-15 |
| **Story Points** | 13 |

| Priority | Story | Title | Points | Labels |
|----------|-------|-------|--------|--------|
| P0 | US-023 | End-to-end testing | 8 | `backend` `frontend` |
| P0 | US-024 | Security hardening | 5 | `security` |

**Sprint Goals:**
1. Backend test coverage >= 80% (JaCoCo)
2. Frontend test coverage >= 70% (Istanbul)
3. Security review complete with zero critical/high findings
4. All integration tests passing

**Risks:**
- Achieving 80% coverage may require refactoring test-unfriendly code
- Security findings may require architectural changes

---

### SPRINT 6: Polish, Deploy & Release

| Field | Value |
|-------|-------|
| **Goal** | Production deployment, UX polish, bug fixes, and v1.0 release |
| **Duration** | 2 weeks |
| **Start** | 2026-05-18 |
| **End** | 2026-05-29 |
| **Story Points** | 5 + buffer |

| Priority | Story | Title | Points | Labels |
|----------|-------|-------|--------|--------|
| P0 | US-025 | Production deployment | 5 | `infrastructure` |
| — | — | Bug fixes and UX polish (buffer) | — | — |

**Sprint Goals:**
1. Application deployed and accessible via public URL
2. All P0 features verified in production
3. Release notes generated, Git tag created
4. Bug fixes from testing phase resolved

**Risks:**
- Deployment environment issues (DNS, SSL, Docker networking)
- Last-minute bug discoveries

---

## Summary

| Metric | Value |
|--------|-------|
| **Total Epics** | 8 |
| **Total User Stories** | 25 |
| **Total Tasks** | 100 |
| **Total Story Points** | 108 |
| **Sprint Velocity Target** | ~18 points/sprint |
| **Sprints** | 6 (12 weeks) |
| **MVP Scope** | All P0 features (FR-001 to FR-006) + Auth + Deploy |

---

*Generated by SDLC Factory — Phase 3: Project Setup*
*Next: Run `/roadmap todo-app` to generate the milestone roadmap, then `/sdlc next` to advance to Design.*
