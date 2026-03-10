# Clarity (todo-app) -- Low-Level Design: Sequence Diagrams

**Project:** todo-app
**Phase:** Design (LLD)
**Created:** 2026-03-07
**Status:** Draft
**Based on:** docs/architecture/hld/system-architecture.md, docs/prd/prd.md, docs/architecture/lld/class-design.md

---

## Table of Contents

1. [User Registration](#1-user-registration)
2. [User Login](#2-user-login)
3. [Create Task](#3-create-task)
4. [Complete Task with Undo](#4-complete-task-with-undo)
5. [Today View Load](#5-today-view-load)
6. [Token Refresh](#6-token-refresh)

---

## 1. User Registration

**Use Case:** A new user registers with email, password, and display name. On success, a default "Inbox" task list is created via a domain event, and a JWT token pair is returned so the user is immediately logged in.

**Participants:** Browser, Angular SPA, AuthController, AuthApplicationService, User (domain), UserRepository, TaskListApplicationService, TaskListRepository, JwtTokenProvider, Redis

### ASCII Sequence Diagram

```
Browser          Angular SPA         AuthController      AuthAppService       User(domain)       UserRepo         TaskListAppSvc     TaskListRepo      JwtProvider       Redis
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │  Fill form       │                     │                   │                   │                  │                   │                  │                │              │
  │  + Submit        │                     │                   │                   │                  │                   │                  │                │              │
  │─────────────────>│                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │  POST /api/v1/      │                   │                   │                  │                   │                  │                │              │
  │                  │  auth/register      │                   │                   │                  │                   │                  │                │              │
  │                  │  {email, password,  │                   │                   │                  │                   │                  │                │              │
  │                  │   displayName}      │                   │                   │                  │                   │                  │                │              │
  │                  │───────────────────> │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │  @Valid passes    │                   │                  │                   │                  │                │              │
  │                  │                     │  register(req)    │                   │                  │                   │                  │                │              │
  │                  │                     │──────────────────>│                   │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │  existsByEmail()  │                  │                   │                  │                │              │
  │                  │                     │                   │─────────────────────────────────────>│                   │                  │                │              │
  │                  │                     │                   │  false            │                  │                   │                  │                │              │
  │                  │                     │                   │<─────────────────────────────────────│                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │  BCrypt hash pwd  │                  │                   │                  │                │              │
  │                  │                     │                   │  (cost factor 12) │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │  User.register(   │                  │                   │                  │                │              │
  │                  │                     │                   │    email, hash,   │                  │                   │                  │                │              │
  │                  │                     │                   │    displayName)   │                  │                   │                  │                │              │
  │                  │                     │                   │─────────────────> │                  │                   │                  │                │              │
  │                  │                     │                   │  new User +       │                  │                   │                  │                │              │
  │                  │                     │                   │  UserRegistered   │                  │                   │                  │                │              │
  │                  │                     │                   │  Event            │                  │                   │                  │                │              │
  │                  │                     │                   │<─────────────────── │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │  save(user)       │                  │                   │                  │                │              │
  │                  │                     │                   │─────────────────────────────────────>│                   │                  │                │              │
  │                  │                     │                   │  persisted        │                  │                   │                  │                │              │
  │                  │                     │                   │<─────────────────────────────────────│                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │  publish(UserRegisteredEvent)        │                   │                  │                │              │
  │                  │                     │                   │  ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─>│                  │                │              │
  │                  │                     │                   │                   │                  │   createDefault   │                  │                │              │
  │                  │                     │                   │                   │                  │   Inbox(userId)   │                  │                │              │
  │                  │                     │                   │                   │                  │──────────────────>│                  │                │              │
  │                  │                     │                   │                   │                  │                   │  save(inbox)     │                │              │
  │                  │                     │                   │                   │                  │                   │─────────────────>│                │              │
  │                  │                     │                   │                   │                  │                   │  saved           │                │              │
  │                  │                     │                   │                   │                  │                   │<─────────────────│                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │  generate JWT     │                  │                   │                  │                │              │
  │                  │                     │                   │  access + refresh │                  │                   │                  │                │              │
  │                  │                     │                   │─────────────────────────────────────────────────────────────────────────────────────────────>│              │
  │                  │                     │                   │  {access, refresh}│                  │                   │                  │                │              │
  │                  │                     │                   │<─────────────────────────────────────────────────────────────────────────────────────────────│              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │                     │  AuthResponse     │                   │                  │                   │                  │                │              │
  │                  │                     │  {accessToken,    │                   │                  │                   │                  │                │              │
  │                  │                     │   refreshToken,   │                   │                  │                   │                  │                │              │
  │                  │                     │   expiresIn}      │                   │                  │                   │                  │                │              │
  │                  │                     │<──────────────────│                   │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │  201 Created        │                   │                   │                  │                   │                  │                │              │
  │                  │  {accessToken,      │                   │                   │                  │                   │                  │                │              │
  │                  │   refreshToken,     │                   │                   │                  │                   │                  │                │              │
  │                  │   expiresIn, type}  │                   │                   │                  │                   │                  │                │              │
  │                  │<───────────────────  │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │                  │  Store tokens       │                   │                   │                  │                   │                  │                │              │
  │                  │  in memory          │                   │                   │                  │                   │                  │                │              │
  │                  │                     │                   │                   │                  │                   │                  │                │              │
  │  Redirect to     │                     │                   │                   │                  │                   │                  │                │              │
  │  /today          │                     │                   │                   │                  │                   │                  │                │              │
  │<─────────────────│                     │                   │                   │                  │                   │                  │                │              │
```

### Mermaid Sequence Diagram

See standalone file: `docs/architecture/lld/diagrams/registration-sequence.mmd`

```mermaid
sequenceDiagram
    actor User as Browser
    participant SPA as Angular SPA
    participant Ctrl as AuthController
    participant Svc as AuthApplicationService
    participant Dom as User (Domain)
    participant URepo as UserRepository
    participant TLSvc as TaskListApplicationService
    participant TLRepo as TaskListRepository
    participant JWT as JwtTokenProvider

    User->>SPA: Fill registration form + submit
    SPA->>Ctrl: POST /api/v1/auth/register<br/>{email, password, displayName}
    Ctrl->>Ctrl: @Valid (Bean Validation)
    Ctrl->>Svc: register(request)
    Svc->>URepo: existsByEmail(email)
    URepo-->>Svc: false
    Svc->>Svc: BCrypt.hash(password, costFactor=12)
    Svc->>Dom: User.register(email, hash, displayName)
    Dom-->>Svc: User instance + UserRegisteredEvent
    Svc->>URepo: save(user)
    URepo-->>Svc: persisted User

    Note over Svc,TLSvc: Spring ApplicationEventPublisher<br/>dispatches UserRegisteredEvent

    Svc-)TLSvc: @EventListener UserRegisteredEvent
    TLSvc->>TLRepo: save(TaskList.createInbox(userId))
    TLRepo-->>TLSvc: persisted Inbox list

    Svc->>JWT: generateAccessToken(user)
    JWT-->>Svc: accessToken (15 min)
    Svc->>JWT: generateRefreshToken(user)
    JWT-->>Svc: refreshToken (7 day)

    Svc-->>Ctrl: AuthResponse
    Ctrl-->>SPA: 201 Created {accessToken, refreshToken, expiresIn, tokenType}
    SPA->>SPA: Store tokens in memory
    SPA-->>User: Redirect to /today
```

### Key Design Decisions

- **No email verification in MVP:** Registration succeeds immediately. Email verification is a post-MVP feature.
- **Immediate login after registration:** The response includes tokens so the user does not need to log in separately.
- **Inbox creation via domain event:** Decoupled from registration logic. The `UserRegisteredEvent` triggers an `@EventListener` that creates the default Inbox list.
- **Generic error on duplicate email:** The API returns a generic "Registration failed" message, not "Email already exists", to prevent email enumeration (OWASP A01).

---

## 2. User Login

**Use Case:** A registered user logs in with email and password. The system validates credentials, checks account lockout status, issues a JWT token pair on success, or increments the failure counter (and potentially locks the account) on failure.

**Participants:** Browser, Angular SPA, AuthController, AuthApplicationService, User (domain), UserRepository, JwtTokenProvider, RefreshTokenRepository, Redis

### ASCII Sequence Diagram

```
Browser          Angular SPA         AuthController      AuthAppService       User(domain)       UserRepo       JwtProvider      RefreshRepo       Redis
  │                  │                     │                   │                   │                  │                │               │              │
  │  Login form      │                     │                   │                   │                  │                │               │              │
  │  + Submit        │                     │                   │                   │                  │                │               │              │
  │─────────────────>│                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │  POST /api/v1/      │                   │                   │                  │                │               │              │
  │                  │  auth/login         │                   │                   │                  │                │               │              │
  │                  │  {email, password}  │                   │                   │                  │                │               │              │
  │                  │───────────────────> │                   │                   │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │  login(request)   │                   │                  │                │               │              │
  │                  │                     │──────────────────>│                   │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  findByEmail()    │                  │                │               │              │
  │                  │                     │                   │─────────────────────────────────────>│                │               │              │
  │                  │                     │                   │  Optional<User>   │                  │                │               │              │
  │                  │                     │                   │<─────────────────────────────────────│                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  user.isAccount   │                  │                │               │              │
  │                  │                     │                   │  Locked()?        │                  │                │               │              │
  │                  │                     │                   │─────────────────> │                  │                │               │              │
  │                  │                     │                   │  false (or throw  │                  │                │               │              │
  │                  │                     │                   │  AccountLocked)   │                  │                │               │              │
  │                  │                     │                   │<─────────────────  │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  BCrypt.matches(  │                  │                │               │              │
  │                  │                     │                   │    pwd, hash)?    │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  ── SUCCESS PATH ──                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  user.reset       │                  │                │               │              │
  │                  │                     │                   │  FailedLogin()    │                  │                │               │              │
  │                  │                     │                   │─────────────────> │                  │                │               │              │
  │                  │                     │                   │<─────────────────  │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  save(user)       │                  │                │               │              │
  │                  │                     │                   │─────────────────────────────────────>│                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  generate tokens  │                  │                │               │              │
  │                  │                     │                   │─────────────────────────────────────────────────────>│               │              │
  │                  │                     │                   │  {access, refresh}│                  │                │               │              │
  │                  │                     │                   │<─────────────────────────────────────────────────────│               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  save(refreshTkn) │                  │                │               │              │
  │                  │                     │                   │─────────────────────────────────────────────────────────────────────>│              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │  200 OK           │                   │                  │                │               │              │
  │                  │                     │  AuthResponse     │                   │                  │                │               │              │
  │                  │                     │<──────────────────│                   │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │  200 OK             │                   │                   │                  │                │               │              │
  │                  │  {accessToken,      │                   │                   │                  │                │               │              │
  │                  │   refreshToken,     │                   │                   │                  │                │               │              │
  │                  │   expiresIn}        │                   │                   │                  │                │               │              │
  │                  │<────────────────────│                   │                   │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │  Store tokens       │                   │                   │                  │                │               │              │
  │  Redirect to     │                     │                   │                   │                  │                │               │              │
  │  /today          │                     │                   │                   │                  │                │               │              │
  │<─────────────────│                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  ── FAILURE PATH ──                  │                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │                   │  user.recordFailed│                  │                │               │              │
  │                  │                     │                   │  Login()          │                  │                │               │              │
  │                  │                     │                   │─────────────────> │                  │                │               │              │
  │                  │                     │                   │  (if attempts >=5 │                  │                │               │              │
  │                  │                     │                   │   lockAccount())  │                  │                │               │              │
  │                  │                     │                   │<─────────────────  │                  │                │               │              │
  │                  │                     │                   │  save(user)       │                  │                │               │              │
  │                  │                     │                   │─────────────────────────────────────>│                │               │              │
  │                  │                     │                   │                   │                  │                │               │              │
  │                  │                     │  401 Unauthorized │                   │                  │                │               │              │
  │                  │                     │  "Invalid email   │                   │                  │                │               │              │
  │                  │                     │   or password"    │                   │                  │                │               │              │
  │                  │                     │<──────────────────│                   │                  │                │               │              │
  │                  │  401 error          │                   │                   │                  │                │               │              │
  │                  │<────────────────────│                   │                   │                  │                │               │              │
  │  Show error msg  │                     │                   │                   │                  │                │               │              │
  │<─────────────────│                     │                   │                   │                  │                │               │              │
```

### Mermaid Sequence Diagram

See standalone file: `docs/architecture/lld/diagrams/login-sequence.mmd`

```mermaid
sequenceDiagram
    actor User as Browser
    participant SPA as Angular SPA
    participant Ctrl as AuthController
    participant Svc as AuthApplicationService
    participant Dom as User (Domain)
    participant URepo as UserRepository
    participant JWT as JwtTokenProvider
    participant RTRepo as RefreshTokenRepository

    User->>SPA: Enter email + password, click Login
    SPA->>Ctrl: POST /api/v1/auth/login<br/>{email, password}
    Ctrl->>Svc: login(request)
    Svc->>URepo: findByEmail(email)
    URepo-->>Svc: Optional(User)

    alt User not found
        Svc-->>Ctrl: throw InvalidCredentialsException
        Ctrl-->>SPA: 401 {code: "INVALID_CREDENTIALS"}
        SPA-->>User: "Invalid email or password"
    end

    Svc->>Dom: user.isAccountLocked()
    alt Account is locked
        Dom-->>Svc: true (lockedUntil > now)
        Svc-->>Ctrl: throw AccountLockedException
        Ctrl-->>SPA: 423 {code: "ACCOUNT_LOCKED", message: "...try again in X minutes"}
        SPA-->>User: "Account locked. Try again later."
    end

    Dom-->>Svc: false (not locked)
    Svc->>Svc: BCrypt.matches(password, user.passwordHash)

    alt Password incorrect
        Svc->>Dom: user.recordFailedLogin()
        Note over Dom: failedAttempts++<br/>if attempts >= 5: lockAccount()
        Svc->>URepo: save(user)
        Svc-->>Ctrl: throw InvalidCredentialsException
        Ctrl-->>SPA: 401 {code: "INVALID_CREDENTIALS"}
        SPA-->>User: "Invalid email or password"
    end

    Note over Svc: Password matches -- success path
    Svc->>Dom: user.resetFailedLoginAttempts()
    Svc->>URepo: save(user)

    Svc->>JWT: generateAccessToken(user)
    JWT-->>Svc: accessToken (15 min expiry)
    Svc->>JWT: generateRefreshToken(user)
    JWT-->>Svc: refreshToken (7 day expiry)
    Svc->>RTRepo: save(RefreshToken)

    Svc-->>Ctrl: AuthResponse
    Ctrl-->>SPA: 200 OK {accessToken, refreshToken, expiresIn, tokenType}
    SPA->>SPA: Store tokens in memory
    SPA-->>User: Redirect to /today
```

### Key Design Decisions

- **Generic error messages:** Both "user not found" and "wrong password" return the same 401 message to prevent user enumeration.
- **Account lockout at domain level:** The `User` aggregate enforces the 5-attempt lockout rule via `recordFailedLogin()` and `isAccountLocked()`. The 15-minute cooldown is checked against `lockedUntil`.
- **Refresh token persisted in DB:** Refresh tokens are stored in PostgreSQL (not just Redis) to survive Redis restarts and support "logout from all devices" via `revokeAllByUserId()`.

---

## 3. Create Task

**Use Case:** An authenticated user creates a new task by entering a title in the inline input field. The task defaults to priority P4, no due date, and the currently selected list (or Inbox if none selected). The UI updates optimistically before the API response returns.

**Participants:** Browser, Angular SPA, NgRx Store, TaskService, AuthInterceptor, TaskController, TaskApplicationService, Task (domain), TaskRepository, Redis Cache

### ASCII Sequence Diagram

```
Browser         Angular SPA        NgRx Store         TaskService        Interceptor       TaskController     TaskAppService      Task(domain)      TaskRepo        Redis
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │  Type title     │                   │                  │                  │                   │                  │                  │                │              │
  │  + press Enter  │                   │                  │                  │                   │                  │                  │                │              │
  │────────────────>│                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │  dispatch(         │                  │                  │                   │                  │                  │                │              │
  │                 │    createTask      │                  │                  │                   │                  │                  │                │              │
  │                 │    {title,listId}) │                  │                  │                   │                  │                  │                │              │
  │                 │──────────────────>│                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │  OPTIMISTIC:     │                  │                   │                  │                  │                │              │
  │                 │                   │  Add temp task   │                  │                   │                  │                  │                │              │
  │                 │                   │  to state with   │                  │                   │                  │                  │                │              │
  │                 │                   │  tempId          │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │  UI shows new   │                   │                  │                  │                   │                  │                  │                │              │
  │  task instantly  │                   │                  │                  │                   │                  │                  │                │              │
  │<────────────────│                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │  Effect:         │                  │                   │                  │                  │                │              │
  │                 │                   │  createTask$     │                  │                   │                  │                  │                │              │
  │                 │                   │────────────────>│                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │  POST /api/v1/  │                   │                  │                  │                │              │
  │                 │                   │                  │  tasks           │                   │                  │                  │                │              │
  │                 │                   │                  │  {title, listId} │                   │                  │                  │                │              │
  │                 │                   │                  │────────────────>│                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │  Add Bearer JWT   │                  │                  │                │              │
  │                 │                   │                  │                  │──────────────────>│                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │  @Valid passes   │                  │                │              │
  │                 │                   │                  │                  │                   │  createTask(cmd) │                  │                │              │
  │                 │                   │                  │                  │                   │─────────────────>│                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │  resolve listId  │                │              │
  │                 │                   │                  │                  │                   │                  │  (default Inbox  │                │              │
  │                 │                   │                  │                  │                   │                  │   if null)       │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │  Task.create(    │                │              │
  │                 │                   │                  │                  │                   │                  │    title, listId,│                │              │
  │                 │                   │                  │                  │                   │                  │    userId)       │                │              │
  │                 │                   │                  │                  │                   │                  │─────────────────>│                │              │
  │                 │                   │                  │                  │                   │                  │  new Task +      │                │              │
  │                 │                   │                  │                  │                   │                  │  TaskCreatedEvent│                │              │
  │                 │                   │                  │                  │                   │                  │<─────────────────│                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │  save(task)      │                │              │
  │                 │                   │                  │                  │                   │                  │─────────────────────────────────>│              │
  │                 │                   │                  │                  │                   │                  │  persisted       │                │              │
  │                 │                   │                  │                  │                   │                  │<─────────────────────────────────│              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │  invalidate      │                │              │
  │                 │                   │                  │                  │                   │                  │  cache keys      │                │              │
  │                 │                   │                  │                  │                   │                  │──────────────────────────────────────────────>│
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │  TaskResponse    │                  │                │              │
  │                 │                   │                  │                  │                   │<─────────────────│                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │                  │  201 Created     │                   │                  │                  │                │              │
  │                 │                   │                  │  TaskResponse    │                   │                  │                  │                │              │
  │                 │                   │                  │<─────────────────────────────────────│                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │                 │                   │  createTaskSuccess│                 │                   │                  │                  │                │              │
  │                 │                   │  (replace tempId  │                 │                   │                  │                  │                │              │
  │                 │                   │   with real id)   │                 │                   │                  │                  │                │              │
  │                 │                   │<─────────────────│                  │                   │                  │                  │                │              │
  │                 │                   │                  │                  │                   │                  │                  │                │              │
  │  UI reconciles  │                   │                  │                  │                   │                  │                  │                │              │
  │  (no visible    │                   │                  │                  │                   │                  │                  │                │              │
  │   change)       │                   │                  │                  │                   │                  │                  │                │              │
  │<────────────────│                   │                  │                  │                   │                  │                  │                │              │
```

### Mermaid Sequence Diagram

See standalone file: `docs/architecture/lld/diagrams/create-task-sequence.mmd`

```mermaid
sequenceDiagram
    actor User as Browser
    participant SPA as Angular SPA
    participant Store as NgRx Store
    participant Effect as NgRx Effect
    participant TkSvc as TaskService
    participant Intc as AuthInterceptor
    participant Ctrl as TaskController
    participant Svc as TaskApplicationService
    participant Dom as Task (Domain)
    participant Repo as TaskRepository
    participant Cache as Redis Cache

    User->>SPA: Type title + press Enter
    SPA->>Store: dispatch(createTask({title, listId}))

    Note over Store: Optimistic update:<br/>Add temp task with tempId

    Store-->>SPA: State updated (task appears in UI)
    SPA-->>User: Task visible instantly

    Store->>Effect: createTask$ effect triggered
    Effect->>TkSvc: createTask(request)
    TkSvc->>Intc: POST /api/v1/tasks {title, listId}
    Intc->>Intc: Attach Authorization: Bearer <JWT>
    Intc->>Ctrl: POST /api/v1/tasks

    Ctrl->>Ctrl: @Valid(CreateTaskRequest)
    Ctrl->>Svc: createTask(command)
    Svc->>Svc: Resolve listId (default to Inbox if null)
    Svc->>Dom: Task.create(title, listId, userId)
    Dom-->>Svc: Task + TaskCreatedEvent
    Svc->>Repo: save(task)
    Repo-->>Svc: persisted Task

    Note over Svc,Cache: @EventListener TaskCreatedEvent<br/>invalidates cache keys

    Svc-)Cache: DEL cache:tasks:user:{uid}:list:{lid}<br/>DEL cache:counts:user:{uid}<br/>DEL cache:today:user:{uid}

    Svc-->>Ctrl: TaskResponse
    Ctrl-->>TkSvc: 201 Created {TaskResponse}
    TkSvc-->>Effect: TaskResponse
    Effect->>Store: dispatch(createTaskSuccess(response))

    Note over Store: Replace tempId with real ID<br/>from server response

    Store-->>SPA: State reconciled
    SPA-->>User: No visible change (already displayed)
```

### Key Design Decisions

- **Optimistic UI:** The NgRx reducer inserts a temporary task with a client-generated `tempId` immediately on dispatch. When the API response arrives, the effect dispatches `createTaskSuccess` which replaces the `tempId` with the server-assigned UUID. If the API fails, `createTaskFailure` removes the temp task and shows an error toast.
- **Cache invalidation on write:** Task creation invalidates three Redis cache keys: the list-specific task cache, the user's list count cache, and the today view cache. The cache is not updated (cache-aside pattern: invalidate, not update).
- **Default list resolution:** If no `listId` is provided in the request, the application service looks up the user's default Inbox list via `TaskListRepository.findDefaultByUserId()`.

---

## 4. Complete Task with Undo

**Use Case:** A user clicks the checkbox on a task to mark it complete. The UI immediately shows a strikethrough with animation and a toast notification with a 5-second "Undo" button. If the user clicks Undo within 5 seconds, the task is reverted to active. If the timer expires, the completion is finalized.

**Participants:** Browser, Angular SPA, NgRx Store, TaskService, TaskController, TaskApplicationService, Task (domain), TaskRepository, Redis Cache

### ASCII Sequence Diagram

```
Browser          Angular SPA        NgRx Store         TaskService       TaskController     TaskAppService      Task(domain)      TaskRepo       Redis
  │                  │                   │                  │                   │                  │                  │                │              │
  │  Click checkbox  │                   │                  │                  │                   │                  │                  │                │              │
  │─────────────────>│                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │  dispatch(         │                  │                  │                   │                  │                  │                │              │
  │                  │    completeTask    │                  │                  │                   │                  │                  │                │              │
  │                  │    {taskId})       │                  │                  │                   │                  │                  │                │              │
  │                  │──────────────────>│                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │  OPTIMISTIC:     │                  │                   │                  │                  │                │              │
  │                  │                   │  Mark task as    │                  │                   │                  │                  │                │              │
  │                  │                   │  completed in    │                  │                   │                  │                  │                │              │
  │                  │                   │  state           │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │  Strikethrough   │                   │                  │                  │                   │                  │                  │                │              │
  │  + fade anim     │                   │                  │                  │                   │                  │                  │                │              │
  │  + Toast with    │                   │                  │                  │                   │                  │                  │                │              │
  │    "Undo" btn    │                   │                  │                  │                   │                  │                  │                │              │
  │    (5s timer)    │                   │                  │                  │                   │                  │                  │                │              │
  │<─────────────────│                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │  Effect:         │                  │                   │                  │                  │                │              │
  │                  │                   │  completeTask$   │                  │                   │                  │                  │                │              │
  │                  │                   │────────────────>│                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │  PATCH /api/v1/  │                   │                  │                  │                │              │
  │                  │                   │                  │  tasks/{id}/     │                   │                  │                  │                │              │
  │                  │                   │                  │  complete        │                   │                  │                  │                │              │
  │                  │                   │                  │─────────────────>│                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │  completeTask     │                  │                  │                │              │
  │                  │                   │                  │                  │  (taskId, userId) │                  │                  │                │              │
  │                  │                   │                  │                  │─────────────────>│                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │  findById(id,    │                  │                │              │
  │                  │                   │                  │                  │                   │  userId)         │                  │                │              │
  │                  │                   │                  │                  │                   │─────────────────────────────────────>│              │
  │                  │                   │                  │                  │                   │  task            │                  │                │              │
  │                  │                   │                  │                  │                   │<─────────────────────────────────────│              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │  task.complete() │                  │                │              │
  │                  │                   │                  │                  │                   │─────────────────>│                  │                │              │
  │                  │                   │                  │                  │                   │  TaskCompleted   │                  │                │              │
  │                  │                   │                  │                  │                   │  Event           │                  │                │              │
  │                  │                   │                  │                  │                   │<─────────────────│                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │  save(task)      │                  │                │              │
  │                  │                   │                  │                  │                   │─────────────────────────────────────>│              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │  invalidate      │                  │                │              │
  │                  │                   │                  │                  │                   │  cache           │                  │                │              │
  │                  │                   │                  │                  │                   │──────────────────────────────────────────────────>│
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │  200 OK          │                   │                  │                  │                │              │
  │                  │                   │                  │  TaskResponse    │                   │                  │                  │                │              │
  │                  │                   │                  │<─────────────────│                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │  completeTask    │                  │                   │                  │                  │                │              │
  │                  │                   │  Success         │                  │                   │                  │                  │                │              │
  │                  │                   │<─────────────────│                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │  ── CASE A: Undo NOT clicked (timer expires) ──       │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │  Toast disappears│                   │                  │                  │                   │                  │                  │                │              │
  │  Task moves to   │                   │                  │                  │                   │                  │                  │                │              │
  │  "completed"     │                   │                  │                  │                   │                  │                  │                │              │
  │  section         │                   │                  │                  │                   │                  │                  │                │              │
  │<─────────────────│                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │  ── CASE B: Undo clicked within 5 seconds ──         │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │  Click "Undo"    │                   │                  │                  │                   │                  │                  │                │              │
  │─────────────────>│                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │  dispatch(         │                  │                  │                   │                  │                  │                │              │
  │                  │  uncompleteTask    │                  │                  │                   │                  │                  │                │              │
  │                  │  {taskId})         │                  │                  │                   │                  │                  │                │              │
  │                  │──────────────────>│                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │  OPTIMISTIC:     │                  │                   │                  │                  │                │              │
  │                  │                   │  Revert task     │                  │                   │                  │                  │                │              │
  │                  │                   │  to active       │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │  Strikethrough   │                   │                  │                  │                   │                  │                  │                │              │
  │  removed,        │                   │                  │                  │                   │                  │                  │                │              │
  │  task restored   │                   │                  │                  │                   │                  │                  │                │              │
  │<─────────────────│                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │  Effect:         │                  │                   │                  │                  │                │              │
  │                  │                   │  uncompleteTask$ │                  │                   │                  │                  │                │              │
  │                  │                   │────────────────>│                  │                   │                  │                  │                │              │
  │                  │                   │                  │  PATCH /api/v1/  │                   │                  │                  │                │              │
  │                  │                   │                  │  tasks/{id}/     │                   │                  │                  │                │              │
  │                  │                   │                  │  uncomplete      │                   │                  │                  │                │              │
  │                  │                   │                  │─────────────────>│                   │                  │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │                  │  task.uncomplete()│                  │                  │                │              │
  │                  │                   │                  │                  │─────────────────>│──────────────────>│                  │                │              │
  │                  │                   │                  │                  │                   │  isCompleted =   │                  │                │              │
  │                  │                   │                  │                  │                   │  false           │                  │                │              │
  │                  │                   │                  │                  │                   │  completedAt =   │                  │                │              │
  │                  │                   │                  │                  │                   │  null            │                  │                │              │
  │                  │                   │                  │                  │                   │  save + cache    │                  │                │              │
  │                  │                   │                  │                  │                   │  invalidation    │                  │                │              │
  │                  │                   │                  │                  │                   │                  │                  │                │              │
  │                  │                   │                  │  200 OK          │                   │                  │                  │                │              │
  │                  │                   │                  │<─────────────────│                   │                  │                  │                │              │
  │                  │                   │  uncompleteTask  │                  │                   │                  │                  │                │              │
  │                  │                   │  Success         │                  │                   │                  │                  │                │              │
  │                  │                   │<─────────────────│                  │                   │                  │                  │                │              │
```

### Mermaid Sequence Diagram

```mermaid
sequenceDiagram
    actor User as Browser
    participant SPA as Angular SPA
    participant Store as NgRx Store
    participant TkSvc as TaskService
    participant Ctrl as TaskController
    participant Svc as TaskApplicationService
    participant Dom as Task (Domain)
    participant Repo as TaskRepository
    participant Cache as Redis Cache

    User->>SPA: Click task checkbox
    SPA->>Store: dispatch(completeTask({taskId}))

    Note over Store: Optimistic: mark isCompleted=true

    Store-->>SPA: State updated
    SPA-->>User: Strikethrough animation + Undo toast (5s)

    Store->>TkSvc: completeTask$ effect
    TkSvc->>Ctrl: PATCH /api/v1/tasks/{id}/complete
    Ctrl->>Svc: completeTask(taskId, userId)
    Svc->>Repo: findByIdAndUserId(id, userId)
    Repo-->>Svc: Task
    Svc->>Dom: task.complete()

    Note over Dom: Sets isCompleted=true,<br/>completedAt=now(),<br/>emits TaskCompletedEvent

    Dom-->>Svc: TaskCompletedEvent
    Svc->>Repo: save(task)
    Svc-)Cache: Invalidate task + count + today caches
    Svc-->>Ctrl: TaskResponse
    Ctrl-->>TkSvc: 200 OK
    TkSvc-->>Store: dispatch(completeTaskSuccess)

    alt User clicks Undo within 5 seconds
        User->>SPA: Click "Undo" button on toast
        SPA->>Store: dispatch(uncompleteTask({taskId}))

        Note over Store: Optimistic: revert isCompleted=false

        Store-->>SPA: State updated
        SPA-->>User: Strikethrough removed, task restored

        Store->>TkSvc: uncompleteTask$ effect
        TkSvc->>Ctrl: PATCH /api/v1/tasks/{id}/uncomplete
        Ctrl->>Svc: uncompleteTask(taskId, userId)
        Svc->>Repo: findByIdAndUserId(id, userId)
        Repo-->>Svc: Task
        Svc->>Dom: task.uncomplete()

        Note over Dom: Sets isCompleted=false,<br/>completedAt=null

        Svc->>Repo: save(task)
        Svc-)Cache: Invalidate caches
        Svc-->>Ctrl: TaskResponse
        Ctrl-->>TkSvc: 200 OK
        TkSvc-->>Store: dispatch(uncompleteTaskSuccess)
    else Timer expires (no undo)
        Note over SPA: Toast disappears<br/>Task moves to completed section
    end
```

### Key Design Decisions

- **Server-first completion, client-side undo window:** The complete API call fires immediately (not deferred until the undo window expires). This ensures the server is the source of truth. If the user clicks Undo, a second API call (uncomplete) reverses the action. This approach avoids client-side queueing complexity.
- **Two separate endpoints:** `PATCH /complete` and `PATCH /uncomplete` are distinct endpoints rather than a single toggle. This makes the intent explicit and avoids race conditions with concurrent requests.
- **Domain invariant enforcement:** `Task.complete()` throws if the task is already deleted (`is_deleted = true`). `Task.uncomplete()` is idempotent -- calling it on an already-active task is a no-op.

---

## 5. Today View Load

**Use Case:** When an authenticated user navigates to the application (or clicks "Today" in the sidebar), the Today View loads showing three sections: (1) today's planned tasks, (2) overdue tasks in a collapsed section, and (3) tasks completed today in a "Done" section. A progress counter shows "X of Y completed."

**Participants:** Browser, Angular SPA, NgRx Store, TaskService, AuthInterceptor, TaskController, TaskApplicationService, TaskRepository, Redis Cache

### ASCII Sequence Diagram

```
Browser          Angular SPA        NgRx Store         TaskService        Interceptor       TaskController     TaskAppService      TaskRepo         Redis
  │                  │                   │                  │                  │                   │                  │                  │              │
  │  Navigate to     │                   │                  │                  │                   │                  │                  │              │
  │  / or /today     │                   │                  │                  │                   │                  │                  │              │
  │─────────────────>│                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │  AuthGuard:       │                  │                  │                   │                  │                  │              │
  │                  │  check token      │                  │                  │                   │                  │                  │              │
  │                  │  valid? Yes       │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │  TodayView        │                  │                  │                   │                  │                  │              │
  │                  │  Component init   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │  dispatch(        │                  │                  │                   │                  │                  │              │
  │                  │    loadTodayView) │                  │                  │                   │                  │                  │              │
  │                  │──────────────────>│                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │  Set loading=true│                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │  Show skeleton   │                   │                  │                  │                   │                  │                  │              │
  │  loader          │                   │                  │                  │                   │                  │                  │              │
  │<─────────────────│                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │  Effect:         │                  │                   │                  │                  │              │
  │                  │                   │  loadTodayView$  │                  │                   │                  │                  │              │
  │                  │                   │────────────────>│                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │  GET /api/v1/    │                   │                  │                  │              │
  │                  │                   │                  │  tasks/today     │                   │                  │                  │              │
  │                  │                   │                  │────────────────>│                   │                  │                  │              │
  │                  │                   │                  │                  │  + Bearer JWT     │                  │                  │              │
  │                  │                   │                  │                  │─────────────────>│                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │  getTodayView    │                  │              │
  │                  │                   │                  │                  │                   │  (userId)        │                  │              │
  │                  │                   │                  │                  │                   │─────────────────>│                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │  Check Redis     │              │
  │                  │                   │                  │                  │                   │                  │  cache:today:    │              │
  │                  │                   │                  │                  │                   │                  │  user:{userId}   │              │
  │                  │                   │                  │                  │                   │                  │──────────────────────────────>│
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │  ── CACHE MISS ──│              │
  │                  │                   │                  │                  │                   │                  │<──────────────────────────────│
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │  findTodayTasks  │              │
  │                  │                   │                  │                  │                   │                  │  (userId, today) │              │
  │                  │                   │                  │                  │                   │                  │─────────────────>│              │
  │                  │                   │                  │                  │                   │                  │  [tasks where    │              │
  │                  │                   │                  │                  │                   │                  │   dueDate=today  │              │
  │                  │                   │                  │                  │                   │                  │   OR isToday]    │              │
  │                  │                   │                  │                  │                   │                  │<─────────────────│              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │  findOverdueTasks│              │
  │                  │                   │                  │                  │                   │                  │  (userId, today) │              │
  │                  │                   │                  │                  │                   │                  │─────────────────>│              │
  │                  │                   │                  │                  │                   │                  │  [tasks where    │              │
  │                  │                   │                  │                  │                   │                  │   dueDate<today  │              │
  │                  │                   │                  │                  │                   │                  │   AND !completed │              │
  │                  │                   │                  │                  │                   │                  │   AND !deleted]  │              │
  │                  │                   │                  │                  │                   │                  │<─────────────────│              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │  findCompleted   │              │
  │                  │                   │                  │                  │                   │                  │  TodayTasks      │              │
  │                  │                   │                  │                  │                   │                  │  (userId, today) │              │
  │                  │                   │                  │                  │                   │                  │─────────────────>│              │
  │                  │                   │                  │                  │                   │                  │  [tasks where    │              │
  │                  │                   │                  │                  │                   │                  │   completedAt    │              │
  │                  │                   │                  │                  │                   │                  │   = today]       │              │
  │                  │                   │                  │                  │                   │                  │<─────────────────│              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │  Store in Redis  │              │
  │                  │                   │                  │                  │                   │                  │  TTL=2min        │              │
  │                  │                   │                  │                  │                   │                  │──────────────────────────────>│
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │  TodayView       │                  │              │
  │                  │                   │                  │                  │                   │  Response        │                  │              │
  │                  │                   │                  │                  │                   │<─────────────────│                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │                  │  200 OK          │                   │                  │                  │              │
  │                  │                   │                  │  TodayViewResp   │                   │                  │                  │              │
  │                  │                   │                  │<─────────────────────────────────────│                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │                  │                   │  loadTodayView   │                  │                   │                  │                  │              │
  │                  │                   │  Success         │                  │                   │                  │                  │              │
  │                  │                   │  {todayTasks,    │                  │                   │                  │                  │              │
  │                  │                   │   overdueTasks,  │                  │                   │                  │                  │              │
  │                  │                   │   doneTodayTasks,│                  │                   │                  │                  │              │
  │                  │                   │   totalToday,    │                  │                   │                  │                  │              │
  │                  │                   │   completedToday}│                  │                   │                  │                  │              │
  │                  │                   │<─────────────────│                  │                   │                  │                  │              │
  │                  │                   │                  │                  │                   │                  │                  │              │
  │  Render:         │                   │                  │                  │                   │                  │                  │              │
  │  - "3 of 7       │                   │                  │                  │                   │                  │                  │              │
  │    completed"    │                   │                  │                  │                   │                  │                  │              │
  │  - Today tasks   │                   │                  │                  │                   │                  │                  │              │
  │  - Overdue (N)   │                   │                  │                  │                   │                  │                  │              │
  │    (collapsed)   │                   │                  │                  │                   │                  │                  │              │
  │  - Done today    │                   │                  │                  │                   │                  │                  │              │
  │<─────────────────│                   │                  │                  │                   │                  │                  │              │
```

### Mermaid Sequence Diagram

See standalone file: `docs/architecture/lld/diagrams/today-view-sequence.mmd`

```mermaid
sequenceDiagram
    actor User as Browser
    participant SPA as Angular SPA
    participant Guard as AuthGuard
    participant Store as NgRx Store
    participant Effect as NgRx Effect
    participant TkSvc as TaskService
    participant Ctrl as TaskController
    participant Svc as TaskApplicationService
    participant Repo as TaskRepository
    participant Cache as Redis Cache

    User->>SPA: Navigate to / or /today
    SPA->>Guard: canActivate()
    Guard-->>SPA: true (token valid)

    SPA->>Store: dispatch(loadTodayView())
    Store-->>SPA: loading = true
    SPA-->>User: Show skeleton loader

    Store->>Effect: loadTodayView$ effect
    Effect->>TkSvc: getTodayView()
    TkSvc->>Ctrl: GET /api/v1/tasks/today (+ Bearer JWT)
    Ctrl->>Svc: getTodayView(userId)

    Svc->>Cache: GET cache:today:user:{userId}

    alt Cache HIT
        Cache-->>Svc: cached TodayViewResponse
    else Cache MISS
        Cache-->>Svc: null

        par Parallel queries
            Svc->>Repo: findTodayTasks(userId, today)
            Note over Repo: WHERE (due_date = today OR is_today = true)<br/>AND is_completed = false AND is_deleted = false
            Repo-->>Svc: todayTasks
        and
            Svc->>Repo: findOverdueTasks(userId, today)
            Note over Repo: WHERE due_date < today<br/>AND is_completed = false AND is_deleted = false
            Repo-->>Svc: overdueTasks
        and
            Svc->>Repo: findCompletedTodayTasks(userId, today)
            Note over Repo: WHERE completed_at::date = today
            Repo-->>Svc: doneTodayTasks
        end

        Svc->>Cache: SET cache:today:user:{userId} TTL=2min
    end

    Svc-->>Ctrl: TodayViewResponse
    Ctrl-->>TkSvc: 200 OK {todayTasks, overdueTasks, doneTodayTasks, totalToday, completedToday}
    TkSvc-->>Effect: TodayViewResponse
    Effect->>Store: dispatch(loadTodayViewSuccess(response))

    Store-->>SPA: State updated (loading=false)
    SPA-->>User: Render: progress counter + today tasks + overdue (collapsed) + done today
```

### Key Design Decisions

- **Single API call, three data groups:** The `GET /api/v1/tasks/today` endpoint returns a `TodayViewResponse` containing all three groups (today, overdue, done-today) in a single response. This avoids three separate API calls from the frontend and reduces latency.
- **Parallel DB queries:** The application service runs the three repository queries in parallel (using `CompletableFuture` or a similar mechanism) to minimize total query time.
- **Cache TTL of 2 minutes:** The today view is the most frequently accessed view. A short TTL ensures freshness while still absorbing repeated navigations.
- **Overdue section collapsed by default:** The frontend receives the `overdueTasks` array and renders it inside a `CollapsibleSectionComponent` that starts collapsed, showing only the count badge.
- **Progress counter from response:** The `totalToday` and `completedToday` fields are calculated server-side to avoid client-side recomputation and to keep the counter accurate even when tasks are modified on other devices.

---

## 6. Token Refresh

**Use Case:** When an API call fails with HTTP 401 (expired access token), the Angular HTTP interceptor automatically attempts to refresh the token using the stored refresh token. If the refresh succeeds, the original request is retried transparently. If the refresh fails (expired or revoked refresh token), the user is redirected to the login page.

**Participants:** Browser, Angular SPA, AuthInterceptor, AuthService, AuthController, AuthApplicationService, JwtTokenProvider, RefreshTokenRepository, Redis

### ASCII Sequence Diagram

```
Browser          Angular SPA        AuthInterceptor      API (any)        AuthService        AuthController     AuthAppService     JwtProvider      RefreshRepo     Redis
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │  User action     │                     │                  │                  │                   │                  │                │              │              │
  │  (e.g. load      │                     │                  │                  │                   │                  │                │              │              │
  │  task list)      │                     │                  │                  │                   │                  │                │              │              │
  │─────────────────>│                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │  GET /api/v1/tasks  │                  │                  │                   │                  │                │              │              │
  │                  │  + Bearer <expired> │                  │                  │                   │                  │                │              │              │
  │                  │────────────────────>│                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  Attach JWT      │                  │                   │                  │                │              │              │
  │                  │                     │  (expired)       │                  │                   │                  │                │              │              │
  │                  │                     │─────────────────>│                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  401 Unauthorized│                  │                   │                  │                │              │              │
  │                  │                     │  "Token expired" │                  │                   │                  │                │              │              │
  │                  │                     │<─────────────────│                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  Is this a 401?  │                  │                   │                  │                │              │              │
  │                  │                     │  Yes.            │                  │                   │                  │                │              │              │
  │                  │                     │  Is refresh      │                  │                   │                  │                │              │              │
  │                  │                     │  already in      │                  │                   │                  │                │              │              │
  │                  │                     │  progress? No.   │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  Queue original  │                  │                   │                  │                │              │              │
  │                  │                     │  request         │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  Call refresh    │                  │                   │                  │                │              │              │
  │                  │                     │─────────────────────────────────────>│                  │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │  POST /api/v1/    │                  │                │              │              │
  │                  │                     │                  │                  │  auth/refresh      │                  │                │              │              │
  │                  │                     │                  │                  │  {refreshToken}   │                  │                │              │              │
  │                  │                     │                  │                  │─────────────────>│                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │  refresh(token)  │                │              │              │
  │                  │                     │                  │                  │                   │─────────────────>│                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │  findByToken   │              │              │
  │                  │                     │                  │                  │                   │                  │  (refreshToken)│              │              │
  │                  │                     │                  │                  │                   │                  │───────────────────────────>│              │
  │                  │                     │                  │                  │                   │                  │  token entity  │              │              │
  │                  │                     │                  │                  │                   │                  │<───────────────────────────│              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │  isValid()?    │              │              │
  │                  │                     │                  │                  │                   │                  │  (!revoked &&  │              │              │
  │                  │                     │                  │                  │                   │                  │   !expired)    │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │  ── SUCCESS PATH ──          │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │  Revoke old    │              │              │
  │                  │                     │                  │                  │                   │                  │  refresh token │              │              │
  │                  │                     │                  │                  │                   │                  │───────────────────────────>│              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │  Generate new  │              │              │
  │                  │                     │                  │                  │                   │                  │  access + new  │              │              │
  │                  │                     │                  │                  │                   │                  │  refresh token │              │              │
  │                  │                     │                  │                  │                   │                  │──────────────>│              │              │
  │                  │                     │                  │                  │                   │                  │  tokens        │              │              │
  │                  │                     │                  │                  │                   │                  │<──────────────│              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │  Save new      │              │              │
  │                  │                     │                  │                  │                   │                  │  refresh token │              │              │
  │                  │                     │                  │                  │                   │                  │───────────────────────────>│              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │  AuthResponse    │                │              │              │
  │                  │                     │                  │                  │                   │<─────────────────│                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │  200 OK           │                  │                │              │              │
  │                  │                     │                  │                  │  {new accessToken │                  │                │              │              │
  │                  │                     │                  │                  │   new refreshToken│                  │                │              │              │
  │                  │                     │                  │                  │   expiresIn}      │                  │                │              │              │
  │                  │                     │                  │                  │<─────────────────│                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  New tokens      │                  │                   │                  │                │              │              │
  │                  │                     │  stored          │                  │                   │                  │                │              │              │
  │                  │                     │<─────────────────────────────────────│                  │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  RETRY original  │                  │                   │                  │                │              │              │
  │                  │                     │  request with    │                  │                   │                  │                │              │              │
  │                  │                     │  new access token│                  │                   │                  │                │              │              │
  │                  │                     │─────────────────>│                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  200 OK (tasks)  │                  │                   │                  │                │              │              │
  │                  │                     │<─────────────────│                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │  200 OK (tasks)     │                  │                  │                   │                  │                │              │              │
  │                  │<────────────────────│                  │                  │                   │                  │                │              │              │
  │  Render tasks    │                     │                  │                  │                   │                  │                │              │              │
  │<─────────────────│                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  ── FAILURE PATH (refresh fails) ── │                  │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │                     │  401 from refresh│                  │                   │                  │                │              │              │
  │                  │                     │  (token expired  │                  │                   │                  │                │              │              │
  │                  │                     │   or revoked)    │                  │                   │                  │                │              │              │
  │                  │                     │<─────────────────────────────────────│                  │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │                  │  Clear stored       │                  │                  │                   │                  │                │              │              │
  │                  │  tokens             │                  │                  │                   │                  │                │              │              │
  │                  │<────────────────────│                  │                  │                   │                  │                │              │              │
  │                  │                     │                  │                  │                   │                  │                │              │              │
  │  Redirect to     │                     │                  │                  │                   │                  │                │              │              │
  │  /auth/login     │                     │                  │                  │                   │                  │                │              │              │
  │<─────────────────│                     │                  │                  │                   │                  │                │              │              │
```

### Mermaid Sequence Diagram

See standalone file: `docs/architecture/lld/diagrams/token-refresh-sequence.mmd`

```mermaid
sequenceDiagram
    actor User as Browser
    participant SPA as Angular SPA
    participant Intc as AuthInterceptor
    participant API as API Endpoint
    participant Auth as AuthService
    participant Ctrl as AuthController
    participant Svc as AuthApplicationService
    participant JWT as JwtTokenProvider
    participant RTRepo as RefreshTokenRepository

    User->>SPA: Trigger API call (e.g., load tasks)
    SPA->>Intc: GET /api/v1/tasks (+ expired JWT)
    Intc->>Intc: Attach Authorization: Bearer <expired>
    Intc->>API: GET /api/v1/tasks
    API-->>Intc: 401 Unauthorized (token expired)

    Intc->>Intc: Is this a 401? Yes.<br/>Is refresh in progress? No.<br/>Set refreshing = true.<br/>Queue original request.

    Intc->>Auth: refreshToken()
    Auth->>Ctrl: POST /api/v1/auth/refresh<br/>{refreshToken}
    Ctrl->>Svc: refresh(refreshToken)
    Svc->>RTRepo: findByToken(refreshToken)
    RTRepo-->>Svc: RefreshToken entity

    alt Token valid (not expired, not revoked)
        Svc->>RTRepo: revoke(oldRefreshToken)
        Note over Svc: Refresh token rotation:<br/>old token invalidated

        Svc->>JWT: generateAccessToken(user)
        JWT-->>Svc: new accessToken (15 min)
        Svc->>JWT: generateRefreshToken(user)
        JWT-->>Svc: new refreshToken (7 day)
        Svc->>RTRepo: save(newRefreshToken)

        Svc-->>Ctrl: AuthResponse
        Ctrl-->>Auth: 200 OK {accessToken, refreshToken, expiresIn}
        Auth->>Auth: Store new tokens in memory
        Auth-->>Intc: New access token

        Intc->>Intc: Set refreshing = false
        Intc->>Intc: Replay queued requests with new token

        Intc->>API: GET /api/v1/tasks (+ new JWT)
        API-->>Intc: 200 OK {tasks}
        Intc-->>SPA: 200 OK {tasks}
        SPA-->>User: Render tasks (seamless)
    else Token invalid (expired or revoked)
        Svc-->>Ctrl: throw TokenExpiredException
        Ctrl-->>Auth: 401 Unauthorized
        Auth->>Auth: Clear stored tokens
        Auth-->>Intc: Refresh failed

        Intc->>Intc: Set refreshing = false
        Intc->>Intc: Reject all queued requests

        Intc-->>SPA: 401 (propagated)
        SPA->>SPA: Router.navigate(['/auth/login'])
        SPA-->>User: Redirect to login page
    end
```

### Key Design Decisions

- **Refresh token rotation:** On every successful refresh, the old refresh token is revoked and a new one is issued. This limits the window of exposure if a refresh token is compromised. A stolen refresh token can only be used once.
- **Request queuing during refresh:** While a token refresh is in progress, all subsequent 401 responses are queued instead of triggering additional refresh attempts. Once the refresh completes (or fails), all queued requests are replayed (or rejected). This prevents a thundering herd of refresh requests.
- **Transparent retry:** The user experiences no interruption. The interceptor handles the 401 -> refresh -> retry cycle transparently. The calling code (NgRx effects, services) sees only the final successful response or the ultimate failure.
- **No refresh for the refresh endpoint:** The interceptor does not attach a Bearer token to `POST /api/v1/auth/refresh` (it uses the refresh token in the request body, not the Authorization header). If the refresh endpoint itself returns 401, the user is redirected to login -- no infinite retry loop.

---

*Generated by SDLC Factory -- Phase 4: Design (LLD - Sequence Diagrams)*
*See also: `docs/architecture/lld/class-design.md`, `docs/architecture/lld/api-contracts.md`*
