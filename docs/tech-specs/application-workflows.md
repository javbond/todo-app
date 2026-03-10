# Clarity (todo-app) -- Application Workflows

**Project:** todo-app
**Phase:** Design (Tech Specs)
**Created:** 2026-03-07
**Status:** Draft
**Based on:** docs/ddd/domain-model.md, docs/architecture/lld/sequence-diagrams.md, docs/prd/prd.md

---

## Table of Contents

1. [User Registration Workflow](#1-user-registration-workflow)
2. [User Login Workflow](#2-user-login-workflow)
3. [Token Refresh Workflow](#3-token-refresh-workflow)
4. [Password Reset Workflow](#4-password-reset-workflow)
5. [Task Lifecycle Workflow](#5-task-lifecycle-workflow)
6. [Today View Assembly Workflow](#6-today-view-assembly-workflow)
7. [Task List Deletion Workflow](#7-task-list-deletion-workflow)
8. [Cache Invalidation Workflow](#8-cache-invalidation-workflow)
9. [Account Lockout Workflow](#9-account-lockout-workflow)

---

## 1. User Registration Workflow

**Trigger:** User submits registration form
**Actors:** Browser, AuthController, AuthApplicationService, UserRepository, TaskListApplicationService
**Events:** UserRegisteredEvent

```
    Browser              AuthController    AuthApplicationService    UserRepo    TaskListAppService
      │                       │                    │                   │              │
      │── POST /auth/register─▶                    │                   │              │
      │   {email,password,    │                    │                   │              │
      │    displayName}       │                    │                   │              │
      │                       │── validate ────────▶                   │              │
      │                       │   @Valid           │                   │              │
      │                       │                    │── existsByEmail()─▶              │
      │                       │                    │◀─ false ──────────│              │
      │                       │                    │                   │              │
      │                       │                    │── BCrypt.hash() ──│              │
      │                       │                    │── User.register()─│              │
      │                       │                    │── save(user) ─────▶              │
      │                       │                    │◀─ user ───────────│              │
      │                       │                    │                   │              │
      │                       │                    │── generateJwt() ──│              │
      │                       │                    │── createRefresh()─│              │
      │                       │                    │                   │              │
      │                       │                    │── publish(UserRegisteredEvent) ──▶
      │                       │                    │                   │              │
      │                       │                    │                   │  @EventListener
      │                       │                    │                   │  onUserRegistered()
      │                       │                    │                   │  │
      │                       │                    │                   │  │── createInbox(userId)
      │                       │                    │                   │  │── save(inboxList)
      │                       │                    │                   │              │
      │                       │◀─ AuthResponse ────│                   │              │
      │◀── 201 Created ───────│                    │                   │              │
      │   {accessToken,       │                    │                   │              │
      │    refreshToken,      │                    │                   │              │
      │    expiresIn}         │                    │                   │              │
```

**Steps:**
1. Validate request body (email format, password strength, display name length)
2. Check email uniqueness (return generic error if duplicate to prevent enumeration)
3. Hash password with BCrypt (cost factor 12)
4. Create User domain entity via factory method
5. Persist user to database
6. Generate JWT access token (15 min) and refresh token (7 day)
7. Publish `UserRegisteredEvent`
8. Event handler creates default "Inbox" TaskList for the new user
9. Return auth tokens to client

**Error Paths:**
- Duplicate email → 409 (generic "Registration failed" message)
- Validation failure → 400 with field-level details
- Rate limit exceeded → 429

---

## 2. User Login Workflow

**Trigger:** User submits login form
**Actors:** Browser, AuthController, AuthApplicationService, UserRepository
**Events:** UserLockedOutEvent (on 5th failure)

```
    Browser              AuthController    AuthApplicationService    UserRepo
      │                       │                    │                   │
      │── POST /auth/login ──▶                    │                   │
      │   {email, password}   │                    │                   │
      │                       │── validate ────────▶                   │
      │                       │                    │── findByEmail() ──▶
      │                       │                    │◀─ user (or empty)─│
      │                       │                    │                   │
      │                       │    ┌───────── user not found ──────────┐
      │                       │    │  return 401 INVALID_CREDENTIALS   │
      │                       │    └───────────────────────────────────┘
      │                       │                    │                   │
      │                       │    ┌───────── account locked ──────────┐
      │                       │    │  if (user.isAccountLocked())      │
      │                       │    │    return 423 ACCOUNT_LOCKED      │
      │                       │    └───────────────────────────────────┘
      │                       │                    │                   │
      │                       │    ┌───────── password mismatch ───────┐
      │                       │    │  user.recordFailedLogin()         │
      │                       │    │  if (attempts >= 5)               │
      │                       │    │    user.lockAccount()             │
      │                       │    │    publish(UserLockedOutEvent)    │
      │                       │    │  save(user)                       │
      │                       │    │  return 401 INVALID_CREDENTIALS   │
      │                       │    └───────────────────────────────────┘
      │                       │                    │                   │
      │                       │    ── password matches ──              │
      │                       │                    │── resetFailedAttempts()
      │                       │                    │── save(user) ─────▶
      │                       │                    │── generateJwt() ──│
      │                       │                    │── createRefresh()─│
      │                       │◀─ AuthResponse ────│                   │
      │◀── 200 OK ────────────│                    │                   │
```

**Lockout Logic:**
- Each failed login increments `failed_login_attempts`
- On 5th consecutive failure: set `is_locked = true`, `locked_until = now() + 15min`
- On successful login: reset `failed_login_attempts` to 0
- Locked accounts auto-unlock after 15 minutes (check `locked_until < now()`)

---

## 3. Token Refresh Workflow

**Trigger:** Access token expired, frontend interceptor detects 401
**Actors:** Angular HTTP Interceptor, AuthController, AuthApplicationService

```
    Angular Interceptor     AuthController    AuthApplicationService    Redis
      │                          │                    │                   │
      │── API call ──────────────▶                    │                   │
      │◀── 401 TOKEN_EXPIRED ────│                    │                   │
      │                          │                    │                   │
      │── POST /auth/refresh ───▶                    │                   │
      │   {refreshToken}         │                    │                   │
      │                          │── validate ────────▶                   │
      │                          │                    │── findByToken() ──│
      │                          │                    │◀─ token entity ───│
      │                          │                    │                   │
      │                          │    ┌── validate token ────────────────┐
      │                          │    │  isRevoked? → 401                │
      │                          │    │  isExpired? → 401                │
      │                          │    └─────────────────────────────────┘
      │                          │                    │                   │
      │                          │                    │── revokeOldToken()│
      │                          │                    │── createNewRefresh()
      │                          │                    │── generateNewJwt()│
      │                          │◀─ AuthResponse ────│                   │
      │◀── 200 OK ───────────────│                    │                   │
      │   {newAccessToken,       │                    │                   │
      │    newRefreshToken}      │                    │                   │
      │                          │                    │                   │
      │── Retry original req ───▶                    │                   │
```

**Refresh Token Rotation:**
- On each refresh, the old refresh token is revoked and a new one is issued
- If a revoked refresh token is used, ALL refresh tokens for that user are revoked (potential token theft detected)
- The frontend interceptor queues concurrent requests during refresh to avoid multiple refresh calls

---

## 4. Password Reset Workflow

**Trigger:** User clicks "Forgot Password"
**Actors:** Browser, AuthController, AuthApplicationService, UserRepository

```
    Browser              AuthController    AuthApplicationService    UserRepo
      │                       │                    │                   │
      │ STEP 1: Request Reset │                    │                   │
      │── POST /auth/forgot-password ──▶           │                   │
      │   {email}             │                    │                   │
      │                       │── validate ────────▶                   │
      │                       │                    │── findByEmail() ──▶
      │                       │                    │◀─ user (or empty)─│
      │                       │                    │                   │
      │                       │    ── if user exists: ──               │
      │                       │                    │── generateResetToken()
      │                       │                    │── save(user) ─────▶
      │                       │                    │── logResetToken()  │  (console in dev)
      │                       │                    │                   │
      │                       │    ── always return 200 ──             │
      │◀── 200 OK ────────────│                    │                   │
      │   "If account exists, │                    │                   │
      │    reset link sent"   │                    │                   │
      │                       │                    │                   │
      │ STEP 2: Reset Password│                    │                   │
      │── POST /auth/reset-password ──▶            │                   │
      │   {token, newPassword}│                    │                   │
      │                       │── validate ────────▶                   │
      │                       │                    │── findByResetToken()▶
      │                       │                    │◀─ user ───────────│
      │                       │                    │                   │
      │                       │    ┌── validate ────────────────────────┐
      │                       │    │  token expired? → 401              │
      │                       │    │  password valid? → 400 if not      │
      │                       │    └────────────────────────────────────┘
      │                       │                    │                   │
      │                       │                    │── BCrypt.hash(newPwd)
      │                       │                    │── user.resetPassword()
      │                       │                    │── user.clearResetToken()
      │                       │                    │── revokeAllRefreshTokens()
      │                       │                    │── save(user) ─────▶
      │◀── 200 OK ────────────│                    │                   │
      │   "Password reset     │                    │                   │
      │    successfully"      │                    │                   │
```

**Security Notes:**
- Password reset token expires after 1 hour
- Always return 200 on forgot-password (even if email doesn't exist) to prevent email enumeration
- After password reset, ALL refresh tokens for the user are revoked
- In dev, the reset token is logged to console (no SMTP server in MVP)

---

## 5. Task Lifecycle Workflow

**Trigger:** Various user actions on tasks
**Actors:** Browser, TaskController, TaskApplicationService, TaskRepository, Redis

```
    ┌──────────────────────────────────────────────────────────────────────┐
    │                       TASK STATE MACHINE                             │
    └──────────────────────────────────────────────────────────────────────┘

                          ┌─────────────────────┐
                          │      CREATED         │
                          │  (Active, in list)   │
              ┌──────────►│                     │◄──────────┐
              │           └──────┬──────────────┘           │
              │                  │                          │
              │    updateTitle() │  complete()              │ uncomplete()
              │    updateDesc()  │                          │ (undo within 5s)
              │    changePriority()                         │
              │    assignDueDate()                          │
              │    moveToList()  │                          │
              │    toggleToday() │                          │
              │                  ▼                          │
              │           ┌─────────────────────┐          │
              │           │     COMPLETED        │──────────┘
              │           │  (isCompleted=true,  │
              │           │   completedAt set)   │
              │           └──────┬──────────────┘
              │                  │
              │                  │ softDelete()
              │                  │
    softDelete() from any        │
    active state                 │
              │                  │
              └────────┐         │
                       ▼         ▼
                ┌──────────────────────┐
                │    SOFT-DELETED       │
                │  (is_deleted=true)    │
                │  Terminal state.      │
                │  No further           │
                │  transitions.         │
                └──────────────────────┘
```

### Create Task

```
Browser (NgRx)               TaskController    TaskApplicationService    TaskRepo     Redis
  │                               │                    │                   │           │
  │── dispatch(createTask) ──┐    │                    │                   │           │
  │                          │    │                    │                   │           │
  │   Optimistic UI:         │    │                    │                   │           │
  │   add temp task to store │    │                    │                   │           │
  │                          │    │                    │                   │           │
  │── POST /api/v1/tasks ───▶    │                    │                   │           │
  │                               │── createTask() ───▶                   │           │
  │                               │                    │── resolve listId │           │
  │                               │                    │   (Inbox if null)│           │
  │                               │                    │── Task.create()  │           │
  │                               │                    │── save() ────────▶           │
  │                               │                    │◀─ task ──────────│           │
  │                               │                    │── invalidate ────────────────▶
  │                               │                    │   cache:tasks:*  │           │
  │                               │                    │   cache:today:*  │           │
  │                               │                    │   cache:lists:*  │           │
  │                               │                    │── publish(TaskCreatedEvent)  │
  │                               │◀─ TaskResponse ────│                  │           │
  │◀── 201 Created ───────────────│                    │                  │           │
  │                               │                    │                  │           │
  │   NgRx Effect:                │                    │                  │           │
  │   replace temp with real task │                    │                  │           │
```

### Complete Task with Undo

```
Browser (NgRx)               TaskController    TaskApplicationService    TaskRepo
  │                               │                    │                   │
  │── dispatch(completeTask) ──┐  │                    │                   │
  │                            │  │                    │                   │
  │   Optimistic UI:           │  │                    │                   │
  │   move to "Done today"     │  │                    │                   │
  │   show undo toast (5s)     │  │                    │                   │
  │                            │  │                    │                   │
  │── PATCH /tasks/{id}/complete──▶                    │                   │
  │                               │── completeTask() ──▶                   │
  │                               │                    │── task.complete()│
  │                               │                    │── save() ────────▶
  │                               │                    │── publish(TaskCompletedEvent)
  │                               │◀─ TaskResponse ────│                   │
  │◀── 200 OK ────────────────────│                    │                   │
  │                               │                    │                   │
  │   ┌── User clicks UNDO (within 5s) ──┐            │                   │
  │   │                                    │            │                   │
  │── PATCH /tasks/{id}/uncomplete ──────▶            │                   │
  │                               │── uncompleteTask()▶                   │
  │                               │                    │── task.uncomplete()
  │                               │                    │── save() ────────▶
  │                               │◀─ TaskResponse ────│                   │
  │◀── 200 OK ────────────────────│                    │                   │
  │                               │                    │                   │
  │   NgRx Effect:                │                    │                   │
  │   restore to active list      │                    │                   │
  │                               │                    │                   │
  │   ┌── Undo timer expires (5s passed) ──┐           │                   │
  │   │  Toast dismissed.                   │           │                   │
  │   │  No further action.                 │           │                   │
  │   └────────────────────────────────────-┘           │                   │
```

---

## 6. Today View Assembly Workflow

**Trigger:** User navigates to Today View (default landing page)
**Actors:** Browser, TaskController, TodayViewApplicationService, TaskRepository, Redis

```
Browser (NgRx)         TaskController     TodayViewAppService     TaskRepo      Redis
  │                         │                    │                   │            │
  │── GET /tasks/today ────▶                    │                   │            │
  │                         │── getTodayView() ─▶                   │            │
  │                         │                    │                   │            │
  │                         │                    │── check cache ───────────────▶│
  │                         │                    │◀─ HIT: return ────────────────│
  │                         │                    │   (or MISS: continue)         │
  │                         │                    │                   │            │
  │                         │    CACHE MISS:     │                   │            │
  │                         │                    │                   │            │
  │                         │                    │── findTodayTasks() ──────────▶│
  │                         │                    │   (due_date = today OR        │
  │                         │                    │    is_today = true,           │
  │                         │                    │    active only)               │
  │                         │                    │◀─ todayTasks ────│            │
  │                         │                    │                   │            │
  │                         │                    │── findOverdueTasks() ────────▶│
  │                         │                    │   (due_date < today,          │
  │                         │                    │    active only)               │
  │                         │                    │◀─ overdueTasks ──│            │
  │                         │                    │                   │            │
  │                         │                    │── findCompletedToday() ──────▶│
  │                         │                    │   (completed_at = today)      │
  │                         │                    │◀─ doneTasks ─────│            │
  │                         │                    │                   │            │
  │                         │                    │── assemble ───────│            │
  │                         │                    │   sort today by priority      │
  │                         │                    │   sort overdue by due_date    │
  │                         │                    │   sort done by completed_at   │
  │                         │                    │   calc: X of Y completed      │
  │                         │                    │                   │            │
  │                         │                    │── write cache ────────────────▶│
  │                         │                    │   TTL: 2 minutes  │            │
  │                         │                    │                   │            │
  │                         │◀─ TodayViewResponse│                   │            │
  │◀── 200 OK ─────────────│                    │                   │            │
  │   {today: [],           │                    │                   │            │
  │    overdue: [],         │                    │                   │            │
  │    completedToday: [],  │                    │                   │            │
  │    totalToday: N,       │                    │                   │            │
  │    completedCount: M}   │                    │                   │            │
```

**Response Structure:**

```json
{
  "today": [
    { "id": "...", "title": "...", "priority": "P1", ... }
  ],
  "overdue": [
    { "id": "...", "title": "...", "dueDate": "2026-03-05", ... }
  ],
  "completedToday": [
    { "id": "...", "title": "...", "completedAt": "2026-03-07T14:30:00Z", ... }
  ],
  "totalToday": 8,
  "completedCount": 3
}
```

---

## 7. Task List Deletion Workflow

**Trigger:** User deletes a custom list
**Actors:** Browser, TaskListController, TaskListApplicationService, TaskListDeletionService, TaskRepository

```
Browser              TaskListController  TaskListAppService  TaskListDeletionSvc  TaskRepo  ListRepo
  │                       │                    │                   │                │         │
  │── DELETE /lists/{id} ─▶                    │                   │                │         │
  │                       │── deleteList() ────▶                   │                │         │
  │                       │                    │── findByIdAndUser()──────────────────────────▶
  │                       │                    │◀─ list ──────────────────────────────────────│
  │                       │                    │                   │                │         │
  │                       │    ┌── is Inbox? ────────────────────────────────────────┐        │
  │                       │    │  YES → throw InboxUndeletableException → 422       │        │
  │                       │    └────────────────────────────────────────────────────-┘        │
  │                       │                    │                   │                │         │
  │                       │                    │── deleteList() ───▶                │         │
  │                       │                    │                   │                │         │
  │                       │                    │                   │── findDefaultByUserId() ▶│
  │                       │                    │                   │◀─ inboxList ────────────-│
  │                       │                    │                   │                │         │
  │                       │                    │                   │── reassignTasks()        │
  │                       │                    │                   │   (from deleted → Inbox) │
  │                       │                    │                   │── UPDATE tasks ─▶        │
  │                       │                    │                   │   SET list_id = inbox.id │
  │                       │                    │                   │   WHERE list_id = deleted│
  │                       │                    │                   │◀─ count: N tasks─│       │
  │                       │                    │                   │                │         │
  │                       │                    │                   │── deleteList() ──────────▶
  │                       │                    │                   │◀─ done ──────────────────│
  │                       │                    │                   │                │         │
  │                       │                    │── publish(TaskListDeletedEvent)    │         │
  │                       │                    │   {listId, userId, reassignedCount}│         │
  │                       │                    │── invalidate caches                │         │
  │                       │◀─ 204 ─────────────│                   │                │         │
  │◀── 204 No Content ────│                    │                   │                │         │
```

**Key Rules:**
- The default "Inbox" list cannot be deleted (INV-L2)
- All tasks in the deleted list are moved to Inbox (not deleted)
- This is a cross-aggregate operation, so it's handled by `TaskListDeletionService` (domain service)
- The operation is wrapped in a single `@Transactional` at the application service level

---

## 8. Cache Invalidation Workflow

**Pattern:** Cache-aside with event-driven invalidation

```
    Application Service              Redis              PostgreSQL
         │                             │                     │
         │── 1. Write to DB ───────────────────────────────▶│
         │◀── OK ──────────────────────────────────────────-│
         │                             │                     │
         │── 2. Invalidate cache ─────▶│                     │
         │   DEL cache:tasks:user:*    │                     │
         │   DEL cache:today:user:*    │                     │
         │   DEL cache:lists:user:*    │                     │
         │◀── OK ──────────────────────│                     │
         │                             │                     │
         │── 3. Return response        │                     │
         │                             │                     │

    Next Read Request:
         │                             │                     │
         │── GET (cache check) ────────▶                     │
         │◀── MISS ────────────────────│                     │
         │                             │                     │
         │── Query DB ─────────────────────────────────────▶│
         │◀── Result ──────────────────────────────────────-│
         │                             │                     │
         │── Write cache (TTL) ────────▶                     │
         │◀── OK ──────────────────────│                     │
         │                             │                     │
         │── Return response           │                     │
```

**Invalidation Strategy:** Write-invalidate (not write-through). On any mutation, the relevant cache keys are deleted. The next read populates the cache. This avoids stale data and is simpler than write-through for the MVP.

---

## 9. Account Lockout Workflow

**Trigger:** 5th consecutive failed login attempt
**Actors:** AuthApplicationService, User entity, Spring Security

```
    Login Attempt 1 (fail)       Login Attempt 5 (fail)       After 15 min
         │                            │                            │
         │── recordFailedLogin()      │── recordFailedLogin()      │── isAccountLocked()
         │   attempts: 0 → 1          │   attempts: 4 → 5          │   locked_until < now()
         │── save(user)               │                            │   → auto-unlock
         │                            │── lockAccount()             │
         │                            │   is_locked = true          │── resetFailedAttempts()
    Login Attempt 2 (fail)            │   locked_until = now+15m   │── is_locked = false
         │                            │                            │── save(user)
         │── recordFailedLogin()      │── publish(UserLockedOutEvent)
         │   attempts: 1 → 2          │── save(user)               │
         │── save(user)               │                            │
         │                            │── return 423               │
    Login Attempt 3 (fail)            │   ACCOUNT_LOCKED           │
         │                            │   "Try again in 15 min"    │
         │── recordFailedLogin()      │                            │
         │   attempts: 2 → 3          │                            │
         │── save(user)          Attempt during lockout:           │
         │                            │                            │
    Login Attempt 4 (fail)            │── isAccountLocked()        │
         │                            │   locked_until > now()     │
         │── recordFailedLogin()      │   → reject immediately     │
         │   attempts: 3 → 4          │── return 423               │
         │── save(user)               │                            │
```

**Lockout Rules:**
- Failed attempts counter resets on successful login
- Lockout duration: 15 minutes (not configurable in MVP)
- Lockout check happens before password verification (avoid timing attacks)
- `UserLockedOutEvent` is logged for security monitoring
- Auto-unlock: `locked_until` is checked on each login attempt; no background job needed

---

*Generated by SDLC Factory -- Phase 4: Design (Tech Specs)*
