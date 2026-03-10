%% Clarity (todo-app) -- Domain Model Class Diagram
%% Generated: 2026-03-07

classDiagram
    direction TB

    class Task {
        <<Aggregate Root>>
        -UUID id
        -String title
        -String description
        -Priority priority
        -DueDate dueDate
        -boolean isCompleted
        -boolean isDeleted
        -boolean isToday
        -LocalDateTime completedAt
        -UUID listId
        -UUID userId
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -UUID createdBy
        -UUID updatedBy
        +create(String title, UUID listId, UUID userId)$ Task
        +updateTitle(String title) void
        +updateDescription(String desc) void
        +changePriority(Priority priority) void
        +assignDueDate(DueDate dueDate) void
        +removeDueDate() void
        +complete() TaskCompletedEvent
        +uncomplete() void
        +softDelete() TaskDeletedEvent
        +addToToday() void
        +removeFromToday() void
        +moveToList(UUID listId) void
        +isOverdue() boolean
        +isDueToday() boolean
    }

    class TaskList {
        <<Aggregate Root>>
        -UUID id
        -String name
        -ListColor color
        -int sortOrder
        -boolean isDefault
        -UUID userId
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        -UUID createdBy
        -UUID updatedBy
        +create(String name, ListColor color, UUID userId)$ TaskList
        +createInbox(UUID userId)$ TaskList
        +rename(String name) void
        +changeColor(ListColor color) void
        +updateSortOrder(int order) void
        +delete() TaskListDeletedEvent
        +isInbox() boolean
    }

    class User {
        <<Entity>>
        -UUID id
        -Email email
        -String passwordHash
        -String displayName
        -boolean isLocked
        -int failedLoginAttempts
        -LocalDateTime lockedUntil
        -String passwordResetToken
        -LocalDateTime passwordResetExpiry
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
        +register(Email email, String hash, String name)$ User
        +recordFailedLogin() void
        +resetFailedLoginAttempts() void
        +lockAccount() void
        +unlockAccount() void
        +isAccountLocked() boolean
        +generatePasswordResetToken() String
        +resetPassword(String newHash) void
        +isResetTokenValid(String token) boolean
    }

    class RefreshToken {
        <<Entity>>
        -UUID id
        -String token
        -UUID userId
        -LocalDateTime expiresAt
        -boolean isRevoked
        -LocalDateTime createdAt
        +create(UUID userId, int expiryDays)$ RefreshToken
        +revoke() void
        +isExpired() boolean
        +isValid() boolean
    }

    class Priority {
        <<Value Object / Enum>>
        P1_URGENT
        P2_HIGH
        P3_MEDIUM
        P4_LOW
        +label() String
        +color() String
        +sortOrder() int
    }

    class DueDate {
        <<Value Object>>
        -LocalDate value
        +of(LocalDate date)$ DueDate
        +isOverdue() boolean
        +isToday() boolean
        +isFuture() boolean
        +value() LocalDate
    }

    class ListColor {
        <<Value Object / Enum>>
        RED
        ORANGE
        YELLOW
        GREEN
        BLUE
        PURPLE
        PINK
        GRAY
        +hexCode() String
    }

    class Email {
        <<Value Object>>
        -String value
        +of(String email)$ Email
        +value() String
        +isValid() boolean
    }

    class DomainEvent {
        <<Abstract>>
        -UUID eventId
        -LocalDateTime occurredAt
        +getEventId() UUID
        +getOccurredAt() LocalDateTime
    }

    class TaskCreatedEvent {
        -UUID taskId
        -UUID userId
        -UUID listId
    }

    class TaskCompletedEvent {
        -UUID taskId
        -UUID userId
    }

    class TaskDeletedEvent {
        -UUID taskId
        -UUID userId
        -UUID listId
    }

    class TaskListDeletedEvent {
        -UUID listId
        -UUID userId
    }

    class UserRegisteredEvent {
        -UUID userId
        -String email
    }

    %% Relationships
    Task --> Priority : has
    Task --> DueDate : has (optional)
    Task ..> TaskList : references by listId
    Task ..> User : references by userId

    TaskList --> ListColor : has
    TaskList ..> User : references by userId

    User --> Email : has
    User "1" --> "*" RefreshToken : owns

    DomainEvent <|-- TaskCreatedEvent
    DomainEvent <|-- TaskCompletedEvent
    DomainEvent <|-- TaskDeletedEvent
    DomainEvent <|-- TaskListDeletedEvent
    DomainEvent <|-- UserRegisteredEvent

    Task ..> TaskCreatedEvent : emits
    Task ..> TaskCompletedEvent : emits
    Task ..> TaskDeletedEvent : emits
    TaskList ..> TaskListDeletedEvent : emits
    User ..> UserRegisteredEvent : emits
