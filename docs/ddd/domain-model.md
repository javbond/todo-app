# Clarity (todo-app) -- Domain Model

**Project:** todo-app
**Phase:** Design (DDD)
**Created:** 2026-03-07
**Status:** Draft
**Based on:** docs/prd/prd.md, docs/architecture/hld/system-architecture.md, docs/architecture/lld/class-design.md, docs/architecture/lld/api-contracts.md

---

## Table of Contents

1. [Part 1: Strategic Design](#part-1-strategic-design)
2. [Part 2: Tactical Design](#part-2-tactical-design)
3. [Part 3: Domain Events](#part-3-domain-events)
4. [Part 4: Ubiquitous Language](#part-4-ubiquitous-language)
5. [Part 5: Aggregate Rules Summary](#part-5-aggregate-rules-summary)

---

# PART 1: STRATEGIC DESIGN

## 1.1 Domain Classification

| Classification | Domain | Rationale |
|----------------|--------|-----------|
| **Core Domain** | Task Management | The differentiating value of Clarity: focus mode (Today View), clean task lifecycle, and intentional daily planning. This is where the product competes and where engineering investment must be highest. |
| **Supporting Domain** | Identity & Access | Necessary for multi-user operation (registration, login, JWT tokens, account lockout) but follows standard patterns. No competitive differentiation. |
| **Generic Domain** | Notification | Browser notifications for reminders. Uses platform APIs (Web Notification API). No custom domain logic beyond scheduling. |

### Domain Investment Matrix

```
                HIGH investment
                     ^
                     |
                     |   [Core]
                     |   Task Management
                     |     - Today View
                     |     - Task lifecycle
                     |     - Lists & priority
                     |
                     |               [Supporting]
                     |               Identity & Access
                     |                 - Auth flows
                     |                 - JWT tokens
                     |
                     |                             [Generic]
                     |                             Notification
                     |                               - Browser API
                     +---------------------------------------------->
                LOW investment                              Platform API
```

## 1.2 Bounded Contexts

### Context 1: Task Management (Core)

```
Context Name:    Task Management
Purpose:         Manages the full lifecycle of tasks and task lists -- creation, editing,
                 completion, deletion, priority assignment, due dates, and the Today View
                 focus mode. This is the core business capability that differentiates Clarity.
Core Concepts:   Task, TaskList, Priority, DueDate, TodayView, Completion, SoftDelete
External Dependencies:
  - Identity & Access context (consumes userId for task/list ownership)
  - Browser Notification API (translates reminders into browser notifications)
Integration Type:
  - Receives userId from Identity & Access via Customer-Supplier
  - Publishes domain events consumed by Notification (in-process)
Owned Data:      tasks table, task_lists table
```

### Context 2: Identity & Access (Supporting)

```
Context Name:    Identity & Access
Purpose:         Handles user registration, authentication (JWT), authorization, account
                 lockout, password reset, and token lifecycle management. Provides the
                 authenticated userId that all other contexts consume.
Core Concepts:   User, Email, Password, RefreshToken, AccountLockout, PasswordReset
External Dependencies:
  - SMTP server (future -- password reset emails)
  - OAuth2 providers (v1.1 -- Google, GitHub)
Integration Type:
  - Supplies userId to Task Management (Customer-Supplier)
Owned Data:      users table, refresh_tokens table
```

### Bounded Context Diagram (ASCII)

```
 ┌──────────────────────────────────────────────────────────────────────────────┐
 │                         MODULAR MONOLITH (com.clarity)                        │
 │                                                                              │
 │  ┌────────────────────────────────────┐  ┌────────────────────────────────┐  │
 │  │    IDENTITY & ACCESS (Supporting)  │  │    TASK MANAGEMENT (Core)      │  │
 │  │                                    │  │                                │  │
 │  │  Entities:                         │  │  Aggregates:                   │  │
 │  │    User (Aggregate Root)           │  │    Task (Aggregate Root)       │  │
 │  │    RefreshToken                    │  │    TaskList (Aggregate Root)   │  │
 │  │                                    │  │                                │  │
 │  │  Value Objects:                    │  │  Value Objects:                │  │
 │  │    Email                           │  │    Priority                    │  │
 │  │    Password (hash)                 │  │    DueDate                     │  │
 │  │                                    │  │    ListColor                   │  │
 │  │  Services:                         │  │    ReminderSettings            │  │
 │  │    AuthApplicationService          │  │                                │  │
 │  │    JwtTokenProvider                │  │  Services:                     │  │
 │  │                                    │  │    TaskApplicationService      │  │
 │  │  Repositories:                     │  │    TaskListApplicationService  │  │
 │  │    UserRepository                  │  │    TodayViewApplicationService │  │
 │  │    RefreshTokenRepository          │  │    TaskCompletionService       │  │
 │  │                                    │  │    TodayViewService            │  │
 │  │  Events Published:                 │  │    TaskListDeletionService     │  │
 │  │    UserRegisteredEvent             │  │                                │  │
 │  │    UserLockedOutEvent              │  │  Repositories:                 │  │
 │  │                                    │  │    TaskRepository              │  │
 │  │  Tables: users, refresh_tokens     │  │    TaskListRepository          │  │
 │  │                                    │  │                                │  │
 │  │  Package: com.clarity.auth         │  │  Events Published:             │  │
 │  └─────────────┬──────────────────────┘  │    TaskCreatedEvent            │  │
 │                │                         │    TaskCompletedEvent           │  │
 │                │  userId (UUID)          │    TaskDeletedEvent             │  │
 │                │                         │    TaskMovedToListEvent         │  │
 │                └─────────────────────────▶    TaskAddedToTodayEvent        │  │
 │                   Customer-Supplier      │    TaskListCreatedEvent         │  │
 │                                          │    TaskListDeletedEvent         │  │
 │                                          │                                │  │
 │                                          │  Tables: tasks, task_lists      │  │
 │                                          │                                │  │
 │                                          │  Package: com.clarity.task      │  │
 │                                          └────────────────────────────────┘  │
 │                                                                              │
 │  ┌────────────────────────────────────────────────────────────────────────┐  │
 │  │                        SHARED KERNEL (com.clarity.shared)               │  │
 │  │  BaseEntity, AuditableEntity, GlobalExceptionHandler, ErrorResponse    │  │
 │  │  CorsConfig, RedisConfig, AuditingConfig                               │  │
 │  └────────────────────────────────────────────────────────────────────────┘  │
 └──────────────────────────────────────────────────────────────────────────────┘
```

## 1.3 Context Map

### Integration Relationships

| Source Context | Target Context | Integration Type | Data Flow | Notes |
|----------------|---------------|------------------|-----------|-------|
| Identity & Access | Task Management | **Customer-Supplier** | userId (UUID) flows downstream | Auth provides the authenticated userId. Task Management consumes it as an opaque identifier. Auth is the supplier (upstream), Task Management is the customer (downstream). |
| Task Management | Browser Notification API | **Anti-Corruption Layer** | ReminderSettings translated to Web Notification API calls | The Angular NotificationService acts as the ACL, translating domain reminder concepts into browser-specific API calls. Prevents browser API concepts from leaking into the domain. |
| Identity & Access | Task Management | **Domain Event** (in-process) | UserRegisteredEvent triggers default Inbox list creation | When a user registers, the event handler in Task Management creates the default "Inbox" TaskList. |

### Context Map Diagram (ASCII)

```
  ┌─────────────────────────────┐
  │    IDENTITY & ACCESS        │
  │       (Supporting)          │
  │                             │
  │  [Upstream / Supplier]      │
  └──────────┬──────────────────┘
             │
             │  Customer-Supplier
             │  (userId: UUID)
             │
             │  Domain Event (in-process)
             │  UserRegisteredEvent
             │
  ┌──────────▼──────────────────┐
  │    TASK MANAGEMENT          │
  │         (Core)              │
  │                             │
  │  [Downstream / Customer]    │
  └──────────┬──────────────────┘
             │
             │  Anti-Corruption Layer
             │  (ReminderSettings --> Web Notification API)
             │
  ┌──────────▼──────────────────┐
  │  BROWSER NOTIFICATION API   │
  │       (Generic / External)  │
  │                             │
  │  Web Notification API       │
  │  (Client-Side Only)         │
  └─────────────────────────────┘
```

### Context Map Diagram (Mermaid)

See: `docs/ddd/diagrams/context-map.mmd`

### Integration Contract: Identity & Access --> Task Management

```
Interface:     userId reference
Type:          Customer-Supplier
Protocol:      In-process method call (shared JVM)
Data:          UUID (opaque identifier -- Task Management never queries User fields)
Constraint:    Task Management must not import auth.domain.model.User
Enforcement:   ArchUnit test in ArchitectureTest.java
```

### Integration Contract: UserRegisteredEvent --> Inbox Creation

```
Interface:     Spring ApplicationEventPublisher
Type:          Domain Event (in-process, synchronous)
Producer:      AuthApplicationService.register()
Consumer:      TaskListApplicationService.onUserRegistered(UserRegisteredEvent)
Payload:       { userId: UUID, email: String }
Side Effect:   Creates default "Inbox" TaskList for the new user
Idempotency:   Check if default list already exists before creating
```

### Integration Contract: Task Management --> Browser Notification

```
Interface:     Angular NotificationService (ACL)
Type:          Anti-Corruption Layer
Direction:     Task Management domain concepts --> Browser API calls
Translation:   ReminderSettings (NONE, AT_DUE, 15MIN, etc.) --> setTimeout + Notification()
Location:      frontend/src/app/core/services/notification.service.ts
Constraint:    No browser API types in domain models or NgRx store
```

---

# PART 2: TACTICAL DESIGN

## 2.1 Aggregate Design

### Aggregate 1: Task (Root: Task)

```
Aggregate Name:    Task
Purpose:           Represents a single unit of work in the user's task management
                   system. Encapsulates the full task lifecycle from creation through
                   completion or deletion. Enforces all business rules related to
                   task state transitions.
Invariants:
  INV-T1: Title must not be blank and must not exceed 255 characters
  INV-T2: Description must not exceed 2000 characters
  INV-T3: Only active (non-deleted) tasks can be completed
  INV-T4: Completed tasks cannot be modified except via uncomplete (undo within 5s)
  INV-T5: A task belongs to exactly one list (listId is non-null)
  INV-T6: Priority defaults to P4 (Low) if not specified
  INV-T7: A task belongs to exactly one user (userId is non-null, immutable after creation)
  INV-T8: Soft-deleted tasks cannot be completed, uncompleted, or modified
  INV-T9: completedAt is set only when isCompleted is true, and cleared when uncompleted
Commands:
  - CreateTask(title, description?, priority?, dueDate?, listId, userId)
  - UpdateTask(title?, description?, priority?, dueDate?, listId?)
  - CompleteTask(taskId)
  - UncompleteTask(taskId)
  - DeleteTask(taskId)
  - ToggleToday(taskId)
Events Emitted:
  - TaskCreatedEvent
  - TaskCompletedEvent
  - TaskUncompletedEvent
  - TaskDeletedEvent
  - TaskMovedToListEvent
  - TaskAddedToTodayEvent
External References:
  - listId: UUID (reference to TaskList aggregate by ID only)
  - userId: UUID (reference to User entity by ID only)
Consistency Boundary:
  - Single Task entity with its embedded value objects (Priority, DueDate, ReminderSettings)
  - Transactional consistency within a single Task row
  - No cross-aggregate transactions (Task does not lock TaskList)
```

#### Task Aggregate Diagram (ASCII)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         TASK AGGREGATE                               │
│                    (Consistency Boundary)                             │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    Task (Aggregate Root)                        │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │  id: UUID (PK)                                                 │  │
│  │  title: String [1..255] NOT NULL                               │  │
│  │  description: String [0..2000] NULLABLE                        │  │
│  │  priority: Priority (VO) -- default P4                         │  │
│  │  dueDate: DueDate (VO) -- NULLABLE                             │  │
│  │  reminderInterval: ReminderSettings (VO) -- default NONE       │  │
│  │  isCompleted: boolean -- default false                         │  │
│  │  isDeleted: boolean -- default false                           │  │
│  │  isToday: boolean -- default false                             │  │
│  │  completedAt: LocalDateTime -- NULLABLE                        │  │
│  │  listId: UUID (FK ref by ID)                                   │  │
│  │  userId: UUID (FK ref by ID)                                   │  │
│  │  sortOrder: int                                                │  │
│  │  createdAt: LocalDateTime                                      │  │
│  │  updatedAt: LocalDateTime                                      │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │  + create(title, listId, userId): Task                         │  │
│  │  + updateTitle(title): void                                    │  │
│  │  + updateDescription(desc): void                               │  │
│  │  + changePriority(priority): void                              │  │
│  │  + assignDueDate(dueDate): void                                │  │
│  │  + removeDueDate(): void                                       │  │
│  │  + setReminderInterval(interval): void                         │  │
│  │  + complete(): TaskCompletedEvent                              │  │
│  │  + uncomplete(): TaskUncompletedEvent                          │  │
│  │  + softDelete(): TaskDeletedEvent                              │  │
│  │  + addToToday(): TaskAddedToTodayEvent                         │  │
│  │  + removeFromToday(): void                                     │  │
│  │  + moveToList(newListId): TaskMovedToListEvent                 │  │
│  │  + isOverdue(): boolean                                        │  │
│  │  + isDueToday(): boolean                                       │  │
│  └─────────────┬──────────────┬───────────────────┬───────────────┘  │
│                │              │                   │                   │
│       ┌────────▼──────┐ ┌────▼──────────┐ ┌──────▼──────────────┐   │
│       │   Priority    │ │   DueDate     │ │ ReminderSettings    │   │
│       │ (Value Object)│ │(Value Object) │ │ (Value Object)      │   │
│       ├───────────────┤ ├──────────────-┤ ├─────────────────────┤   │
│       │ P1 Urgent Red │ │ value: Date   │ │ NONE                │   │
│       │ P2 High Orange│ │               │ │ AT_DUE              │   │
│       │ P3 Medium Blue│ │ isOverdue()   │ │ FIFTEEN_MINUTES     │   │
│       │ P4 Low Gray   │ │ isToday()     │ │ THIRTY_MINUTES      │   │
│       │               │ │ isFuture()    │ │ ONE_HOUR            │   │
│       │ label()       │ │               │ │ ONE_DAY             │   │
│       │ color()       │ │               │ │                     │   │
│       │ sortOrder()   │ │               │ │ getOffsetMinutes()  │   │
│       └───────────────┘ └──────────────-┘ └─────────────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
         │ ref by ID             │ ref by ID
         ▼                       ▼
    ┌──────────┐           ┌──────────┐
    │ TaskList │           │   User   │
    │(separate │           │(separate │
    │aggregate)│           │ context) │
    └──────────┘           └──────────┘
```

### Aggregate 2: TaskList (Root: TaskList)

```
Aggregate Name:    TaskList
Purpose:           Represents a user-created category for organizing tasks. Provides the
                   grouping mechanism that allows users to separate tasks into logical
                   categories (Work, Personal, Errands). Enforces the Inbox invariant.
Invariants:
  INV-L1: Name must not be blank and must not exceed 50 characters
  INV-L2: Default "Inbox" list cannot be deleted
  INV-L3: Default "Inbox" list cannot be renamed
  INV-L4: Each user has exactly one default (Inbox) list
  INV-L5: List name must be unique per user
  INV-L6: Color must be one of 8 preset values
  INV-L7: A list belongs to exactly one user (userId is non-null, immutable)
Commands:
  - CreateTaskList(name, color?, userId)
  - CreateInboxList(userId) -- system command on registration
  - UpdateTaskList(name?, color?)
  - DeleteTaskList(listId)
  - ReorderTaskList(listId, newSortOrder)
Events Emitted:
  - TaskListCreatedEvent
  - TaskListDeletedEvent
External References:
  - userId: UUID (reference to User entity by ID only)
Consistency Boundary:
  - Single TaskList entity with its embedded value object (ListColor)
  - Deletion triggers cross-aggregate side effect: tasks reassigned to Inbox
    (handled by TaskListDeletionService as an application-level operation, not
    within the aggregate's consistency boundary)
```

#### TaskList Aggregate Diagram (ASCII)

```
┌─────────────────────────────────────────────────────────────────────┐
│                       TASKLIST AGGREGATE                             │
│                    (Consistency Boundary)                             │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                 TaskList (Aggregate Root)                       │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │  id: UUID (PK)                                                 │  │
│  │  name: String [1..50] NOT NULL                                 │  │
│  │  color: ListColor (VO) -- default GRAY                         │  │
│  │  sortOrder: int                                                │  │
│  │  isDefault: boolean -- true for Inbox only                     │  │
│  │  userId: UUID (FK ref by ID)                                   │  │
│  │  createdAt: LocalDateTime                                      │  │
│  │  updatedAt: LocalDateTime                                      │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │  + create(name, color, userId): TaskList                       │  │
│  │  + createInbox(userId): TaskList                               │  │
│  │  + rename(name): void                                          │  │
│  │  + changeColor(color): void                                    │  │
│  │  + updateSortOrder(order): void                                │  │
│  │  + delete(): TaskListDeletedEvent                              │  │
│  │  + isInbox(): boolean                                          │  │
│  └─────────────────────┬──────────────────────────────────────────┘  │
│                        │                                             │
│               ┌────────▼──────────┐                                  │
│               │    ListColor      │                                  │
│               │  (Value Object)   │                                  │
│               ├───────────────────┤                                  │
│               │ RED    (#EF4444)  │                                  │
│               │ ORANGE (#F97316)  │                                  │
│               │ YELLOW (#EAB308)  │                                  │
│               │ GREEN  (#22C55E)  │                                  │
│               │ BLUE   (#3B82F6)  │                                  │
│               │ PURPLE (#8B5CF6)  │                                  │
│               │ PINK   (#EC4899)  │                                  │
│               │ GRAY   (#6B7280)  │                                  │
│               │                   │                                  │
│               │ + hexCode(): Str  │                                  │
│               └───────────────────┘                                  │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
         │ ref by ID
         ▼
    ┌──────────┐
    │   User   │
    │(separate │
    │ context) │
    └──────────┘
```

### Aggregate 3: User (Root: User) -- Identity & Access Context

```
Aggregate Name:    User
Purpose:           Represents a registered user of the system. Manages authentication
                   state (password, lockout, reset tokens) and identity (email, display
                   name). Acts as the identity anchor consumed by Task Management.
Invariants:
  INV-U1: Email must be unique across all users
  INV-U2: Email must be a valid email format
  INV-U3: Account locks after 5 consecutive failed login attempts for 15 minutes
  INV-U4: Password must meet strength requirements (8+ chars, 1 uppercase, 1 number)
  INV-U5: Password is stored as BCrypt hash (cost factor 12+), never as plaintext
  INV-U6: Password reset token expires after 1 hour
  INV-U7: Display name must not be blank and must not exceed 100 characters
Commands:
  - RegisterUser(email, password, displayName)
  - LoginUser(email, password)
  - LogoutUser(refreshToken)
  - RefreshToken(refreshToken)
  - RequestPasswordReset(email)
  - ResetPassword(token, newPassword)
Events Emitted:
  - UserRegisteredEvent
  - UserLockedOutEvent
External References:
  - None (User is the root identity entity)
Consistency Boundary:
  - Single User entity with embedded value objects (Email)
  - RefreshToken is a separate entity in the same context but not part of
    the User aggregate (tokens have independent lifecycle)
```

#### User Aggregate Diagram (ASCII)

```
┌─────────────────────────────────────────────────────────────────────┐
│                         USER AGGREGATE                               │
│                    (Consistency Boundary)                             │
│                                                                      │
│  ┌────────────────────────────────────────────────────────────────┐  │
│  │                    User (Aggregate Root)                        │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │  id: UUID (PK)                                                 │  │
│  │  email: Email (VO) -- UNIQUE                                   │  │
│  │  passwordHash: String NOT NULL                                 │  │
│  │  displayName: String [1..100] NOT NULL                         │  │
│  │  isLocked: boolean -- default false                            │  │
│  │  failedLoginAttempts: int -- default 0                         │  │
│  │  lockedUntil: LocalDateTime -- NULLABLE                        │  │
│  │  passwordResetToken: String -- NULLABLE                        │  │
│  │  passwordResetExpiry: LocalDateTime -- NULLABLE                │  │
│  │  createdAt: LocalDateTime                                      │  │
│  │  updatedAt: LocalDateTime                                      │  │
│  ├────────────────────────────────────────────────────────────────┤  │
│  │  + register(email, passwordHash, displayName): User            │  │
│  │  + recordFailedLogin(): void                                   │  │
│  │  + resetFailedLoginAttempts(): void                            │  │
│  │  + lockAccount(): UserLockedOutEvent                           │  │
│  │  + unlockAccount(): void                                       │  │
│  │  + isAccountLocked(): boolean                                  │  │
│  │  + generatePasswordResetToken(): String                        │  │
│  │  + resetPassword(newPasswordHash): void                        │  │
│  │  + clearPasswordResetToken(): void                             │  │
│  └─────────────────────┬──────────────────────────────────────────┘  │
│                        │                                             │
│               ┌────────▼──────────┐                                  │
│               │     Email         │                                  │
│               │  (Value Object)   │                                  │
│               ├───────────────────┤                                  │
│               │ - value: String   │                                  │
│               │                   │                                  │
│               │ + of(str): Email  │                                  │
│               │ + value(): String │                                  │
│               │ + validate(): void│                                  │
│               └───────────────────┘                                  │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘

 Separate entity in same context (NOT part of User aggregate):

┌────────────────────────────────────────────┐
│            RefreshToken (Entity)            │
├────────────────────────────────────────────┤
│  id: UUID (PK)                              │
│  token: String (opaque) UNIQUE              │
│  userId: UUID (FK ref by ID)                │
│  expiresAt: LocalDateTime                   │
│  isRevoked: boolean -- default false        │
│  createdAt: LocalDateTime                   │
├────────────────────────────────────────────┤
│  + create(userId, expiresAt): RefreshToken  │
│  + revoke(): void                           │
│  + isExpired(): boolean                     │
│  + isValid(): boolean                       │
└────────────────────────────────────────────┘
```

## 2.2 Entity vs Value Object Classification

| Concept | Type | Identity | Mutability | Context | Rationale |
|---------|------|----------|------------|---------|-----------|
| Task | Entity (Aggregate Root) | UUID | Mutable (via methods) | Task Management | Has unique identity, state changes over time, lifecycle tracked |
| TaskList | Entity (Aggregate Root) | UUID | Mutable (via methods) | Task Management | Has unique identity, can be renamed/recolored/deleted |
| User | Entity (Aggregate Root) | UUID | Mutable (via methods) | Identity & Access | Has unique identity, password and lockout state change |
| RefreshToken | Entity | UUID | Mutable (revocable) | Identity & Access | Has unique identity, can be revoked, has expiry lifecycle |
| Priority | Value Object (Enum) | None | Immutable | Task Management | Defined by value (P1/P2/P3/P4), no identity, interchangeable |
| DueDate | Value Object | None | Immutable | Task Management | Wraps a date value, equality by date, no identity |
| ListColor | Value Object (Enum) | None | Immutable | Task Management | Defined by color value, no identity |
| ReminderSettings | Value Object (Enum) | None | Immutable | Task Management | Defined by interval value, no identity |
| Email | Value Object | None | Immutable | Identity & Access | Defined by email string value, includes format validation |

## 2.3 Repository Interfaces

### TaskRepository (Task Management Context)

```java
package com.clarity.task.domain.repository;

import com.clarity.task.domain.model.Task;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Port: Repository interface for the Task aggregate.
 * Implementations reside in infrastructure layer.
 * All queries are scoped to the authenticated user (userId).
 */
public interface TaskRepository {

    Task save(Task task);

    Optional<Task> findByIdAndUserId(UUID id, UUID userId);

    Page<Task> findByUserIdAndFilters(
        UUID userId,
        UUID listId,       // nullable -- filter by list
        Boolean completed, // nullable -- filter by completion status
        String priority,   // nullable -- filter by priority level
        Pageable pageable
    );

    /**
     * Tasks for Today View: due today OR is_today = true, not completed, not deleted.
     */
    List<Task> findTodayTasks(UUID userId, LocalDate today);

    /**
     * Overdue tasks: due_date < today, not completed, not deleted.
     */
    List<Task> findOverdueTasks(UUID userId, LocalDate today);

    /**
     * Tasks completed today: completed_at date = today.
     */
    List<Task> findCompletedTodayTasks(UUID userId, LocalDate today);

    /**
     * Reassign all tasks from one list to another (used when deleting a list).
     * Returns the count of reassigned tasks.
     */
    int reassignTasksToList(UUID fromListId, UUID toListId, UUID userId);

    /**
     * Count active (non-completed, non-deleted) tasks per list for sidebar badges.
     */
    List<TaskCountByList> countActiveTasksByList(UUID userId);

    void deleteById(UUID id);

    /**
     * Projection for task counts by list.
     */
    interface TaskCountByList {
        UUID getListId();
        String getListName();
        long getActiveTaskCount();
    }
}
```

### TaskListRepository (Task Management Context)

```java
package com.clarity.task.domain.repository;

import com.clarity.task.domain.model.TaskList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Port: Repository interface for the TaskList aggregate.
 * Implementations reside in infrastructure layer.
 */
public interface TaskListRepository {

    TaskList save(TaskList taskList);

    Optional<TaskList> findByIdAndUserId(UUID id, UUID userId);

    List<TaskList> findAllByUserId(UUID userId);

    Optional<TaskList> findDefaultByUserId(UUID userId);

    boolean existsByNameAndUserId(String name, UUID userId);

    void deleteById(UUID id);
}
```

### UserRepository (Identity & Access Context)

```java
package com.clarity.auth.domain.repository;

import com.clarity.auth.domain.model.User;
import java.util.Optional;
import java.util.UUID;

/**
 * Port: Repository interface for the User aggregate.
 * Implementations reside in infrastructure layer.
 */
public interface UserRepository {

    User save(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByPasswordResetToken(String token);
}
```

### RefreshTokenRepository (Identity & Access Context)

```java
package com.clarity.auth.domain.repository;

import com.clarity.auth.domain.model.RefreshToken;
import java.util.Optional;
import java.util.UUID;

/**
 * Port: Repository interface for RefreshToken entities.
 * Implementations reside in infrastructure layer.
 */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByToken(String token);

    void revokeAllByUserId(UUID userId);

    void deleteExpiredTokens();
}
```

## 2.4 Domain Services

### TaskCompletionService

```
Service Name:      TaskCompletionService
Context:           Task Management (Domain Layer)
Purpose:           Encapsulates the business logic for task completion and undo.
                   The completion logic is simple enough to live on the Task entity,
                   but the undo policy (5-second window) is enforced at the application
                   layer via the frontend timer. This service handles completion state
                   transitions that involve cross-cutting validation.
Package:           com.clarity.task.domain.service
```

**Responsibilities:**
- Validate that a task is not deleted before completing
- Set `isCompleted = true` and `completedAt = now()`
- For uncomplete: clear `isCompleted` and `completedAt`
- The 5-second undo window is a frontend UX concern (the backend accepts uncomplete at any time; the frontend restricts the UI to 5 seconds)

### TodayViewService

```
Service Name:      TodayViewService
Context:           Task Management (Domain Layer)
Purpose:           Assembles the Today View by combining three query results into a
                   single cohesive view model: today's planned tasks, overdue tasks,
                   and tasks completed today.
Package:           com.clarity.task.domain.service
```

**Responsibilities:**
- Query tasks where `dueDate = today OR isToday = true` (active only)
- Query tasks where `dueDate < today` (active only) for overdue section
- Query tasks where `completedAt` is today for done-today section
- Calculate `totalToday` and `completedToday` counts
- Sort today tasks by priority, overdue by due date ascending, done-today by completedAt descending

### TaskListDeletionService

```
Service Name:      TaskListDeletionService
Context:           Task Management (Domain Layer)
Purpose:           Orchestrates the deletion of a TaskList with the side effect of
                   reassigning all contained tasks to the user's Inbox. This crosses
                   the aggregate boundary (TaskList + Task) so it lives as a domain
                   service rather than on the aggregate root.
Package:           com.clarity.task.domain.service
```

**Responsibilities:**
- Validate that the list is not the default Inbox
- Find the user's Inbox list
- Reassign all tasks from the deleted list to Inbox
- Delete the list
- Return the count of reassigned tasks for the event payload

## 2.5 Application Services

### TaskApplicationService

```
Service Name:    TaskApplicationService
Context:         Task Management (Application Layer)
Package:         com.clarity.task.application.service
Purpose:         Orchestrates task use cases. Thin layer that coordinates
                 domain objects, repositories, and event publishing.
```

#### Use Case: Create Task

```
Input (Command):
  CreateTaskCommand {
    title: String          // required, 1-255 chars
    description: String    // optional, max 2000 chars
    priority: String       // optional, defaults to "P4"
    dueDate: LocalDate     // optional
    listId: UUID           // optional, defaults to user's Inbox
    userId: UUID           // from authenticated principal
  }

Output (Response):
  TaskResponse {
    id, title, description, priority, priorityLabel, priorityColor,
    dueDate, isCompleted, isToday, completedAt, listId, listName,
    createdAt, updatedAt
  }

Steps:
  1. Validate command fields (Bean Validation on command DTO)
  2. If listId is null, resolve user's default Inbox list
  3. Verify listId belongs to the authenticated user
  4. Call Task.create(title, listId, userId) factory method
  5. Set optional fields: description, priority, dueDate
  6. Persist via TaskRepository.save(task)
  7. Publish TaskCreatedEvent via ApplicationEventPublisher
  8. Map domain entity to TaskResponse via MapStruct
  9. Return TaskResponse

Events Published:
  - TaskCreatedEvent { taskId, userId, title, listId, timestamp }
```

#### Use Case: Update Task

```
Input (Command):
  UpdateTaskCommand {
    taskId: UUID           // from path parameter
    title: String          // optional
    description: String    // optional
    priority: String       // optional
    dueDate: LocalDate     // optional (null to clear)
    listId: UUID           // optional (move to different list)
    userId: UUID           // from authenticated principal
  }

Output (Response):
  TaskResponse

Steps:
  1. Load task by taskId and userId (404 if not found)
  2. Verify task is not deleted (422 if deleted)
  3. Verify task is not completed or throw if INV-T4 violated
  4. Apply each non-null field update via domain methods
  5. If listId changed, verify new list belongs to user, call task.moveToList(newListId)
  6. Persist via TaskRepository.save(task)
  7. If list changed, publish TaskMovedToListEvent
  8. Map and return TaskResponse

Events Published:
  - TaskMovedToListEvent { taskId, oldListId, newListId } (conditional)
```

#### Use Case: Complete Task

```
Input (Command):
  CompleteTaskCommand {
    taskId: UUID           // from path parameter
    userId: UUID           // from authenticated principal
  }

Output (Response):
  TaskResponse (with isCompleted=true, completedAt set)

Steps:
  1. Load task by taskId and userId (404 if not found)
  2. Verify task is not deleted (422 if deleted)
  3. Call task.complete() -- sets isCompleted=true, completedAt=now()
  4. Persist via TaskRepository.save(task)
  5. Publish TaskCompletedEvent
  6. Map and return TaskResponse

Events Published:
  - TaskCompletedEvent { taskId, userId, completedAt }
```

#### Use Case: Uncomplete Task

```
Input (Command):
  UncompleteTaskCommand {
    taskId: UUID
    userId: UUID
  }

Output (Response):
  TaskResponse (with isCompleted=false, completedAt=null)

Steps:
  1. Load task by taskId and userId (404 if not found)
  2. Call task.uncomplete() -- clears isCompleted and completedAt
  3. Persist via TaskRepository.save(task)
  4. Publish TaskUncompletedEvent
  5. Map and return TaskResponse

Events Published:
  - TaskUncompletedEvent { taskId, userId }
```

#### Use Case: Delete Task

```
Input (Command):
  DeleteTaskCommand {
    taskId: UUID
    userId: UUID
  }

Output (Response):
  void (204 No Content)

Steps:
  1. Load task by taskId and userId (404 if not found)
  2. Call task.softDelete() -- sets isDeleted=true
  3. Persist via TaskRepository.save(task)
  4. Publish TaskDeletedEvent

Events Published:
  - TaskDeletedEvent { taskId, userId }
```

#### Use Case: Toggle Today

```
Input (Command):
  ToggleTodayCommand {
    taskId: UUID
    userId: UUID
  }

Output (Response):
  TaskResponse (with toggled isToday)

Steps:
  1. Load task by taskId and userId (404 if not found)
  2. If isToday is false, call task.addToToday() -- publish TaskAddedToTodayEvent
  3. If isToday is true, call task.removeFromToday()
  4. Persist via TaskRepository.save(task)
  5. Map and return TaskResponse

Events Published:
  - TaskAddedToTodayEvent { taskId, userId } (conditional, only when adding)
```

### TaskListApplicationService

```
Service Name:    TaskListApplicationService
Context:         Task Management (Application Layer)
Package:         com.clarity.task.application.service
Purpose:         Orchestrates task list use cases including CRUD and
                 list deletion with task reassignment.
```

#### Use Case: Create Task List

```
Input (Command):
  CreateTaskListCommand {
    name: String           // required, 1-50 chars
    color: String          // optional, defaults to "GRAY"
    userId: UUID           // from authenticated principal
  }

Output (Response):
  TaskListResponse {
    id, name, color, colorHex, sortOrder, isDefault, createdAt, updatedAt
  }

Steps:
  1. Validate name uniqueness for this user (422 if duplicate)
  2. Call TaskList.create(name, color, userId)
  3. Set sortOrder to max existing + 1
  4. Persist via TaskListRepository.save(list)
  5. Publish TaskListCreatedEvent
  6. Map and return TaskListResponse

Events Published:
  - TaskListCreatedEvent { listId, userId, name }
```

#### Use Case: Update Task List

```
Input (Command):
  UpdateTaskListCommand {
    listId: UUID
    name: String           // optional
    color: String          // optional
    userId: UUID
  }

Output (Response):
  TaskListResponse

Steps:
  1. Load list by listId and userId (404 if not found)
  2. If name provided and list is Inbox, reject (422 -- INV-L3)
  3. If name provided, verify uniqueness, call list.rename(name)
  4. If color provided, call list.changeColor(color)
  5. Persist via TaskListRepository.save(list)
  6. Map and return TaskListResponse

Events Published:
  - None (list updates are not significant domain events in MVP)
```

#### Use Case: Delete Task List

```
Input (Command):
  DeleteTaskListCommand {
    listId: UUID
    userId: UUID
  }

Output (Response):
  void (204 No Content)

Steps:
  1. Load list by listId and userId (404 if not found)
  2. Verify list is not Inbox (422 -- INV-L2)
  3. Inject TaskListDeletionService
  4. Find user's Inbox list
  5. Reassign all tasks in this list to Inbox via TaskRepository.reassignTasksToList()
  6. Delete list via TaskListRepository.deleteById()
  7. Publish TaskListDeletedEvent with reassignment count

Events Published:
  - TaskListDeletedEvent { listId, userId, tasksReassignedToInboxCount }
```

#### Use Case: Handle UserRegisteredEvent (Event Handler)

```
Input (Event):
  UserRegisteredEvent { userId, email }

Output:
  Side effect: default Inbox list created

Steps:
  1. Listen for UserRegisteredEvent via @EventListener
  2. Check if default list already exists (idempotency)
  3. Call TaskList.createInbox(userId)
  4. Persist via TaskListRepository.save(inboxList)

Events Published:
  - TaskListCreatedEvent { listId, userId, name: "Inbox" }
```

### TodayViewApplicationService

```
Service Name:    TodayViewApplicationService
Context:         Task Management (Application Layer)
Package:         com.clarity.task.application.service
Purpose:         Assembles the Today View response by delegating to TodayViewService.
```

#### Use Case: Get Today View

```
Input (Query):
  GetTodayViewQuery {
    userId: UUID           // from authenticated principal
  }

Output (Response):
  TodayViewResponse {
    todayTasks: List<TaskResponse>
    overdueTasks: List<TaskResponse>
    doneTodayTasks: List<TaskResponse>
    totalToday: int
    completedToday: int
  }

Steps:
  1. Get today's date (LocalDate.now())
  2. Query todayTasks via TaskRepository.findTodayTasks(userId, today)
  3. Query overdueTasks via TaskRepository.findOverdueTasks(userId, today)
  4. Query doneTodayTasks via TaskRepository.findCompletedTodayTasks(userId, today)
  5. Calculate totalToday = todayTasks.size() + doneTodayTasks.size()
  6. Calculate completedToday = doneTodayTasks.size()
  7. Map all to TaskResponse via MapStruct
  8. Assemble and return TodayViewResponse

Events Published:
  - None (read-only query)
```

### AuthApplicationService

```
Service Name:    AuthApplicationService
Context:         Identity & Access (Application Layer)
Package:         com.clarity.auth.application.service
Purpose:         Orchestrates authentication use cases: registration, login,
                 logout, token refresh, and password reset.
```

#### Use Case: Register

```
Input (Command):
  RegisterCommand {
    email: String          // required, valid email
    password: String       // required, 8+ chars, 1 uppercase, 1 number
    displayName: String    // required, 1-100 chars
  }

Output (Response):
  AuthResponse {
    accessToken, refreshToken, expiresIn, tokenType
  }

Steps:
  1. Validate email format and password strength
  2. Check email uniqueness via UserRepository.existsByEmail() (409 if exists)
  3. Hash password with BCrypt (cost 12)
  4. Call User.register(email, passwordHash, displayName)
  5. Persist via UserRepository.save(user)
  6. Generate JWT access token (15 min) and refresh token (7 day)
  7. Store refresh token via RefreshTokenRepository.save()
  8. Publish UserRegisteredEvent { userId, email }
  9. Return AuthResponse

Events Published:
  - UserRegisteredEvent { userId, email }
```

#### Use Case: Login

```
Input (Command):
  LoginCommand {
    email: String
    password: String
  }

Output (Response):
  AuthResponse

Steps:
  1. Load user by email (401 if not found -- generic message)
  2. Check if account is locked via user.isAccountLocked() (423 if locked)
  3. Verify password via BCrypt.matches() (401 if wrong)
  4. If password wrong, call user.recordFailedLogin()
     - If attempts >= 5, call user.lockAccount(), publish UserLockedOutEvent
  5. If password correct, call user.resetFailedLoginAttempts()
  6. Generate JWT access token and refresh token
  7. Store refresh token
  8. Return AuthResponse

Events Published:
  - UserLockedOutEvent { userId, lockedUntil } (conditional)
```

#### Use Case: Logout

```
Input (Command):
  LogoutCommand {
    refreshToken: String
    userId: UUID           // from authenticated principal
  }

Output (Response):
  void (204 No Content)

Steps:
  1. Find refresh token via RefreshTokenRepository.findByToken()
  2. Revoke refresh token via token.revoke()
  3. Add access token to Redis blacklist with TTL = remaining expiry
  4. Persist via RefreshTokenRepository.save(token)

Events Published:
  - None
```

#### Use Case: Refresh Token

```
Input (Command):
  RefreshTokenCommand {
    refreshToken: String
  }

Output (Response):
  AuthResponse (with new access + refresh tokens)

Steps:
  1. Find refresh token (401 if not found)
  2. Verify not expired and not revoked (401 if invalid)
  3. Revoke the old refresh token (rotation)
  4. Generate new access token and new refresh token
  5. Store new refresh token
  6. Return AuthResponse

Events Published:
  - None
```

#### Use Case: Forgot Password

```
Input (Command):
  ForgotPasswordCommand {
    email: String
  }

Output (Response):
  MessageResponse { message: "If an account exists..." }

Steps:
  1. Always return success (no email enumeration)
  2. If user exists by email:
     a. Call user.generatePasswordResetToken()
     b. Persist via UserRepository.save(user)
     c. Send reset email (log in dev, SMTP in prod)

Events Published:
  - None
```

#### Use Case: Reset Password

```
Input (Command):
  ResetPasswordCommand {
    token: String
    newPassword: String
  }

Output (Response):
  MessageResponse { message: "Password has been reset..." }

Steps:
  1. Find user by reset token (401 if not found)
  2. Verify token not expired (401 if expired)
  3. Hash new password with BCrypt
  4. Call user.resetPassword(newPasswordHash)
  5. Call user.clearPasswordResetToken()
  6. Revoke all existing refresh tokens for this user
  7. Persist via UserRepository.save(user)

Events Published:
  - None
```

---

# PART 3: DOMAIN EVENTS

## 3.1 Event Catalog

All domain events are published via Spring `ApplicationEventPublisher` (in-process, synchronous in MVP). Events are immutable records representing facts that have occurred.

### Task Management Context Events

#### TaskCreatedEvent

```
Event Name:        TaskCreatedEvent
Triggered By:      TaskApplicationService.createTask()
Payload:
  taskId: UUID             // ID of the created task
  userId: UUID             // Owner of the task
  title: String            // Task title (for logging/notifications)
  listId: UUID             // List the task was created in
  timestamp: Instant       // When the event occurred
Consumers:
  - (Future) Analytics service for task creation metrics
  - (Future) Activity log
Consistency Impact:
  - None in MVP (fire-and-forget logging)
```

#### TaskCompletedEvent

```
Event Name:        TaskCompletedEvent
Triggered By:      TaskApplicationService.completeTask()
Payload:
  taskId: UUID
  userId: UUID
  completedAt: Instant     // When the task was completed
Consumers:
  - (Future) Analytics service for completion metrics
  - (Future) Today View cache invalidation
Consistency Impact:
  - None in MVP
```

#### TaskUncompletedEvent

```
Event Name:        TaskUncompletedEvent
Triggered By:      TaskApplicationService.uncompleteTask()
Payload:
  taskId: UUID
  userId: UUID
  timestamp: Instant
Consumers:
  - (Future) Analytics service (undo tracking)
Consistency Impact:
  - None in MVP
```

#### TaskDeletedEvent

```
Event Name:        TaskDeletedEvent
Triggered By:      TaskApplicationService.deleteTask()
Payload:
  taskId: UUID
  userId: UUID
  timestamp: Instant
Consumers:
  - (Future) Cache invalidation
Consistency Impact:
  - None in MVP
```

#### TaskMovedToListEvent

```
Event Name:        TaskMovedToListEvent
Triggered By:      TaskApplicationService.updateTask() (when listId changes)
Payload:
  taskId: UUID
  oldListId: UUID          // Previous list
  newListId: UUID          // New list
  userId: UUID
  timestamp: Instant
Consumers:
  - (Future) Sidebar count cache invalidation
Consistency Impact:
  - None in MVP (counts recalculated on query)
```

#### TaskAddedToTodayEvent

```
Event Name:        TaskAddedToTodayEvent
Triggered By:      TaskApplicationService.toggleToday() (when adding to today)
Payload:
  taskId: UUID
  userId: UUID
  timestamp: Instant
Consumers:
  - (Future) Today View cache invalidation
Consistency Impact:
  - None in MVP
```

#### TaskListCreatedEvent

```
Event Name:        TaskListCreatedEvent
Triggered By:      TaskListApplicationService.createTaskList()
                   TaskListApplicationService.onUserRegistered() (Inbox creation)
Payload:
  listId: UUID
  userId: UUID
  name: String             // List name
  timestamp: Instant
Consumers:
  - (Future) Sidebar cache invalidation
Consistency Impact:
  - None in MVP
```

#### TaskListDeletedEvent

```
Event Name:        TaskListDeletedEvent
Triggered By:      TaskListApplicationService.deleteTaskList()
Payload:
  listId: UUID
  userId: UUID
  tasksReassignedToInboxCount: int    // How many tasks moved to Inbox
  timestamp: Instant
Consumers:
  - (Future) Sidebar cache invalidation
  - (Future) Notification ("X tasks moved to Inbox")
Consistency Impact:
  - Task reassignment is completed BEFORE event is published
  - Event confirms the completed side effect
```

### Identity & Access Context Events

#### UserRegisteredEvent

```
Event Name:        UserRegisteredEvent
Triggered By:      AuthApplicationService.register()
Payload:
  userId: UUID
  email: String            // For logging (not PII in event -- used only in-process)
  timestamp: Instant
Consumers:
  - TaskListApplicationService.onUserRegistered() -- creates default Inbox list
Consistency Impact:
  - SYNCHRONOUS in MVP: Inbox creation happens in the same transaction
  - If Inbox creation fails, registration rolls back
  - Idempotency: handler checks if Inbox already exists
```

#### UserLockedOutEvent

```
Event Name:        UserLockedOutEvent
Triggered By:      AuthApplicationService.login() (after 5th failed attempt)
Payload:
  userId: UUID
  lockedUntil: Instant     // When the lockout expires
  timestamp: Instant
Consumers:
  - (Future) Security monitoring / alerting
  - (Future) Admin notification
Consistency Impact:
  - None (informational event)
```

## 3.2 Event Flow Diagram (ASCII)

```
 IDENTITY & ACCESS CONTEXT                    TASK MANAGEMENT CONTEXT
 ─────────────────────────                    ─────────────────────────

 ┌───────────────────────┐
 │ AuthApplicationService│
 │   .register()         │
 └───────────┬───────────┘
             │
             │ publishes
             ▼
 ┌───────────────────────┐         ┌───────────────────────────────┐
 │  UserRegisteredEvent  │────────▶│ TaskListApplicationService    │
 │  { userId, email }    │ listens │   .onUserRegistered()         │
 └───────────────────────┘         │                               │
                                   │   1. Create Inbox TaskList    │
 ┌───────────────────────┐         │   2. Publish TaskListCreated  │
 │ AuthApplicationService│         └───────────────────────────────┘
 │   .login() [5 fails]  │
 └───────────┬───────────┘
             │ publishes                    ┌──────────────────────┐
             ▼                              │TaskApplicationService│
 ┌───────────────────────┐                  └──────────┬───────────┘
 │  UserLockedOutEvent   │                             │
 │  { userId, lockedUntil}│                  publishes  │
 └───────────────────────┘                  ┌──────────▼──────────────┐
                                            │                         │
                                            ▼                         ▼
                                ┌─────────────────────┐  ┌─────────────────────┐
                                │  TaskCreatedEvent   │  │  TaskCompletedEvent │
                                │  { taskId, userId,  │  │  { taskId, userId,  │
                                │    title, listId }  │  │    completedAt }    │
                                └─────────────────────┘  └─────────────────────┘

                                ┌─────────────────────┐  ┌─────────────────────┐
                                │ TaskUncompletedEvent│  │  TaskDeletedEvent   │
                                │ { taskId, userId }  │  │  { taskId, userId } │
                                └─────────────────────┘  └─────────────────────┘

                                ┌─────────────────────┐  ┌──────────────────────┐
                                │TaskMovedToListEvent │  │TaskAddedToTodayEvent │
                                │{ taskId, oldListId, │  │ { taskId, userId }   │
                                │  newListId, userId }│  └──────────────────────┘
                                └─────────────────────┘

                                  ┌────────────────────────┐
                                  │TaskListApplicationSvc  │
                                  └──────────┬─────────────┘
                                             │ publishes
                                  ┌──────────▼─────────────┐
                                  │  TaskListCreatedEvent  │
                                  │  { listId, userId,     │
                                  │    name }              │
                                  └────────────────────────┘
                                  ┌────────────────────────┐
                                  │  TaskListDeletedEvent  │
                                  │  { listId, userId,     │
                                  │   reassignedCount }    │
                                  └────────────────────────┘


 ┌─────────────────────────────────────────────────────────────────────────┐
 │                     BROWSER (Angular Frontend)                          │
 │                                                                         │
 │   NotificationService (ACL)                                             │
 │     Polls tasks with ReminderSettings                                   │
 │     Translates to Web Notification API calls                            │
 │     No domain events -- client-side scheduling only                     │
 └─────────────────────────────────────────────────────────────────────────┘
```

### Event Flow Diagram (Mermaid)

See: `docs/ddd/diagrams/event-flow.mmd`

---

# PART 4: UBIQUITOUS LANGUAGE

The following glossary defines the shared vocabulary used by all team members (product, engineering, QA, design) when discussing the Clarity domain. These terms must be used consistently in code, documentation, user stories, and conversation.

| # | Term | Definition | Context |
|---|------|-----------|---------|
| 1 | **Task** | A single unit of work created by a user. Has a title, optional description, priority, optional due date, and belongs to exactly one list. Tasks go through a lifecycle: active, completed, or deleted. | Task Management |
| 2 | **Task List** | A user-created category for organizing tasks. Identified by a name and a color. Every user has a default "Inbox" list. Examples: "Work", "Personal", "Errands". | Task Management |
| 3 | **Inbox** | The default task list created automatically when a user registers. Tasks with no explicit list assignment land in Inbox. The Inbox cannot be deleted or renamed. | Task Management |
| 4 | **Priority** | A four-level urgency classification assigned to every task. P1 (Urgent/red), P2 (High/orange), P3 (Medium/blue), P4 (Low/gray). Defaults to P4. Determines sort order in task views. | Task Management |
| 5 | **Due Date** | An optional date assigned to a task indicating when it should be completed. Stored as a date (not datetime in MVP). Tasks past their due date are considered "overdue". | Task Management |
| 6 | **Overdue** | A task whose due date has passed without being completed. Overdue tasks appear in a separate collapsible section in the Today View and display red date text in all views. | Task Management |
| 7 | **Today View** | The default landing page after login. Shows three sections: tasks planned for today (due today or manually added), overdue tasks (collapsed by default), and tasks completed today. The core differentiating feature. | Task Management |
| 8 | **Today Flag** | A boolean attribute (`isToday`) on a task that marks it for appearance in the Today View regardless of its due date. Users explicitly "add to today" or "remove from today". | Task Management |
| 9 | **Done Today** | The section at the bottom of the Today View showing tasks completed on the current calendar day. Provides a sense of daily accomplishment. Sorted by completion time descending. | Task Management |
| 10 | **Complete** | The action of marking a task as finished. Sets `isCompleted = true` and records `completedAt`. Completed tasks show strikethrough styling and move to a completed section. | Task Management |
| 11 | **Undo** | The ability to revert a task completion within a 5-second window after marking it complete. The frontend shows a toast with an "Undo" button. After 5 seconds, the undo opportunity expires (frontend-enforced). | Task Management |
| 12 | **Soft Delete** | The method of removing a task by setting `isDeleted = true` rather than physically removing the database row. Soft-deleted tasks are excluded from all queries. | Task Management |
| 13 | **List Color** | One of 8 preset colors (red, orange, yellow, green, blue, purple, pink, gray) assigned to a task list for visual identification in the sidebar. | Task Management |
| 14 | **Sort Order** | A numeric position determining the display sequence of task lists in the sidebar or tasks within a view. Lower numbers appear first. | Task Management |
| 15 | **Reminder** | A browser notification scheduled relative to a task's due date. Configurable intervals: none, at due time, 15 minutes before, 30 minutes before, 1 hour before, 1 day before. | Task Management |
| 16 | **Focus Mode** | A synonym for the Today View concept. Emphasizes that the Today View is designed for intentional daily planning, not a comprehensive task dump. | Task Management |
| 17 | **User** | A person who has registered an account in Clarity. Identified by a unique email address and authenticated via password (JWT). Owns all their tasks and lists. | Identity & Access |
| 18 | **Registration** | The process of creating a new user account. Requires email, password (8+ chars, 1 uppercase, 1 number), and display name. Triggers creation of the default Inbox list. | Identity & Access |
| 19 | **Access Token** | A short-lived JWT (15-minute expiry) used to authenticate API requests. Sent in the `Authorization: Bearer` header. Contains userId and expiry claims. | Identity & Access |
| 20 | **Refresh Token** | A longer-lived opaque token (7-day expiry) used to obtain a new access token without re-entering credentials. Rotated on each use (old token revoked, new token issued). | Identity & Access |
| 21 | **Account Lockout** | A security mechanism that disables login after 5 consecutive failed password attempts. The account is locked for 15 minutes. Prevents brute-force attacks. | Identity & Access |
| 22 | **Password Reset** | The flow for recovering access when a user forgets their password. A reset token (1-hour expiry) is emailed. The reset page accepts the token and a new password. | Identity & Access |
| 23 | **Aggregate** | A DDD concept: a cluster of domain objects treated as a single transactional unit. In Clarity, Task and TaskList are separate aggregates. External references between aggregates use IDs only. | Architecture |
| 24 | **Bounded Context** | A DDD concept: a logical boundary within which a particular domain model applies. Clarity has two: Task Management (core) and Identity & Access (supporting). In the modular monolith, these are package boundaries. | Architecture |
| 25 | **Anti-Corruption Layer** | A DDD integration pattern that translates between two different models. In Clarity, the Angular NotificationService translates domain ReminderSettings into browser Notification API calls. | Architecture |
| 26 | **Domain Event** | An immutable record of something significant that happened in the domain. Named in past tense (e.g., TaskCompletedEvent). Published via Spring ApplicationEventPublisher in the MVP. | Architecture |

---

# PART 5: AGGREGATE RULES SUMMARY

## Task Aggregate Invariants

| ID | Rule | Enforcement Location | Error Response |
|----|------|---------------------|----------------|
| INV-T1 | Title must not be blank and must not exceed 255 characters | `Task.updateTitle()` + Bean Validation on `CreateTaskCommand` | 400 VALIDATION_ERROR |
| INV-T2 | Description must not exceed 2000 characters | `Task.updateDescription()` + Bean Validation | 400 VALIDATION_ERROR |
| INV-T3 | Only active (non-deleted) tasks can be completed | `Task.complete()` -- checks `isDeleted == false` | 422 BUSINESS_RULE_VIOLATION |
| INV-T4 | Completed tasks cannot be modified (except undo) | `Task.updateTitle()`, `Task.changePriority()`, etc. -- check `isCompleted == false` | 422 BUSINESS_RULE_VIOLATION |
| INV-T5 | A task belongs to exactly one list (`listId` non-null) | `Task.create()` factory -- requires listId | 400 VALIDATION_ERROR |
| INV-T6 | Priority defaults to P4 if not specified | `Task.create()` factory -- default in constructor | N/A (auto-applied) |
| INV-T7 | A task belongs to exactly one user (`userId` non-null, immutable) | `Task.create()` factory -- set once, no setter | 400 VALIDATION_ERROR |
| INV-T8 | Soft-deleted tasks cannot be completed, uncompleted, or modified | All mutation methods check `isDeleted == false` | 422 BUSINESS_RULE_VIOLATION |
| INV-T9 | `completedAt` is set only when `isCompleted` is true | `Task.complete()` sets both; `Task.uncomplete()` clears both | N/A (internally consistent) |

## TaskList Aggregate Invariants

| ID | Rule | Enforcement Location | Error Response |
|----|------|---------------------|----------------|
| INV-L1 | Name must not be blank and must not exceed 50 characters | `TaskList.create()`, `TaskList.rename()` + Bean Validation | 400 VALIDATION_ERROR |
| INV-L2 | Default "Inbox" list cannot be deleted | `TaskList.delete()` -- checks `isDefault == false` | 422 BUSINESS_RULE_VIOLATION |
| INV-L3 | Default "Inbox" list cannot be renamed | `TaskList.rename()` -- checks `isDefault == false` | 422 BUSINESS_RULE_VIOLATION |
| INV-L4 | Each user has exactly one default (Inbox) list | `TaskList.createInbox()` -- idempotency check in handler | 422 BUSINESS_RULE_VIOLATION |
| INV-L5 | List name must be unique per user | `TaskListApplicationService` -- query before create/rename | 422 BUSINESS_RULE_VIOLATION |
| INV-L6 | Color must be one of 8 preset values | `ListColor` enum -- invalid value rejected at parse time | 400 VALIDATION_ERROR |
| INV-L7 | A list belongs to exactly one user (`userId` non-null, immutable) | `TaskList.create()` factory -- set once, no setter | 400 VALIDATION_ERROR |

## User Aggregate Invariants

| ID | Rule | Enforcement Location | Error Response |
|----|------|---------------------|----------------|
| INV-U1 | Email must be unique across all users | `AuthApplicationService.register()` -- uniqueness check | 409 EMAIL_ALREADY_EXISTS |
| INV-U2 | Email must be a valid email format | `Email` value object constructor + Bean Validation | 400 VALIDATION_ERROR |
| INV-U3 | Account locks after 5 consecutive failed login attempts for 15 minutes | `User.recordFailedLogin()` + `User.lockAccount()` | 423 ACCOUNT_LOCKED |
| INV-U4 | Password must meet strength requirements (8+ chars, 1 uppercase, 1 number) | Bean Validation on `RegisterCommand` and `ResetPasswordCommand` | 400 VALIDATION_ERROR |
| INV-U5 | Password stored as BCrypt hash (cost factor 12+), never plaintext | `AuthApplicationService` -- BCrypt encoding before persistence | N/A (infrastructure concern) |
| INV-U6 | Password reset token expires after 1 hour | `User.generatePasswordResetToken()` sets expiry; validated on use | 401 TOKEN_EXPIRED |
| INV-U7 | Display name must not be blank and must not exceed 100 characters | Bean Validation on `RegisterCommand` | 400 VALIDATION_ERROR |

## Cross-Aggregate Rules

| Rule | Description | Enforcement | Consistency |
|------|-------------|-------------|-------------|
| Inbox Creation on Registration | When a user registers, a default "Inbox" TaskList is created | `@EventListener` on `UserRegisteredEvent` | Synchronous (same transaction in MVP) |
| Task Reassignment on List Deletion | When a TaskList is deleted, all its tasks move to Inbox | `TaskListDeletionService` (domain service) | Synchronous (same transaction) |
| Task Ownership | Tasks can only be created in lists owned by the same user | `TaskApplicationService.createTask()` validates listId ownership | Application layer validation |
| Cascading Soft Delete | Deleting a list does NOT delete its tasks (moves to Inbox) | `TaskListDeletionService` handles reassignment before list deletion | Synchronous (same transaction) |

---

*Generated by SDLC Factory -- Phase 4: Design (DDD)*
*See also: `docs/ddd/diagrams/`, `docs/architecture/hld/system-architecture.md`, `docs/architecture/lld/class-design.md`*
