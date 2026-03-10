# Clarity (todo-app) -- Database Schema

**Project:** todo-app
**Phase:** Design (Tech Specs)
**Created:** 2026-03-07
**Status:** Draft
**Based on:** docs/ddd/domain-model.md, docs/architecture/lld/class-design.md, docs/architecture/hld/system-architecture.md

---

## Table of Contents

1. [Schema Overview](#1-schema-overview)
2. [Utility Functions](#2-utility-functions)
3. [Table Definitions](#3-table-definitions)
4. [Indexes](#4-indexes)
5. [Flyway Migration Plan](#5-flyway-migration-plan)
6. [ERD Diagram](#6-erd-diagram)
7. [Redis Key Schema](#7-redis-key-schema)
8. [Data Volume Estimates](#8-data-volume-estimates)

---

## 1. Schema Overview

Clarity uses a **single PostgreSQL database** with no schema namespacing (single bounded context deployed as a modular monolith). All tables use UUIDs as primary keys and include standard audit columns.

### Conventions

| Convention | Value |
|-----------|-------|
| Primary keys | UUID v4 (`gen_random_uuid()`) |
| Table names | `snake_case`, plural |
| Column names | `snake_case` |
| Timestamps | `TIMESTAMP WITH TIME ZONE` (UTC) |
| Soft delete | `is_deleted BOOLEAN DEFAULT FALSE` |
| Optimistic locking | `version INTEGER DEFAULT 0` |
| String encoding | UTF-8 |
| Migrations | Flyway (`V{N}__{description}.sql`) |

### Tables

| Table | Bounded Context | Aggregate Root | Rows (est. 1K users) |
|-------|----------------|----------------|---------------------|
| `users` | Identity & Access | User | 1,000 |
| `refresh_tokens` | Identity & Access | RefreshToken | 2,000 |
| `task_lists` | Task Management | TaskList | 5,000 |
| `tasks` | Task Management | Task | 50,000 |

---

## 2. Utility Functions

These are created in the first migration and used by all tables.

```sql
-- ============================================================
-- Utility: Auto-update updated_at timestamp
-- ============================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

---

## 3. Table Definitions

### 3.1 users

Stores registered user accounts. Aggregate root for the Identity & Access context.

```sql
CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email                   VARCHAR(255) NOT NULL,
    password_hash           VARCHAR(255) NOT NULL,
    display_name            VARCHAR(100) NOT NULL,
    is_locked               BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts   INTEGER NOT NULL DEFAULT 0,
    locked_until            TIMESTAMP WITH TIME ZONE,
    password_reset_token    VARCHAR(255),
    password_reset_expiry   TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_length CHECK (char_length(email) >= 3),
    CONSTRAINT ck_users_display_name_length CHECK (char_length(display_name) >= 1),
    CONSTRAINT ck_users_failed_attempts CHECK (failed_login_attempts >= 0)
);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE users IS 'Registered user accounts (Identity & Access context)';
COMMENT ON COLUMN users.password_hash IS 'BCrypt hash (cost factor 12+)';
COMMENT ON COLUMN users.is_locked IS 'True when account locked after 5 failed login attempts';
COMMENT ON COLUMN users.locked_until IS 'Account lockout expiry (15 minutes from lock)';
COMMENT ON COLUMN users.password_reset_token IS 'One-time token for password reset (expires in 1 hour)';
COMMENT ON COLUMN users.version IS 'Optimistic locking version';
```

### 3.2 refresh_tokens

Stores JWT refresh tokens. Separate entity in the Identity & Access context (not part of User aggregate).

```sql
CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token           VARCHAR(255) NOT NULL,
    user_id         UUID NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

COMMENT ON TABLE refresh_tokens IS 'JWT refresh tokens with rotation support';
COMMENT ON COLUMN refresh_tokens.token IS 'Opaque refresh token string (UUID v4)';
COMMENT ON COLUMN refresh_tokens.is_revoked IS 'Set to true on logout or rotation';
```

### 3.3 task_lists

Stores user-created task lists (categories). Aggregate root for the Task Management context.

```sql
CREATE TABLE task_lists (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) NOT NULL,
    color           VARCHAR(20) NOT NULL DEFAULT 'GRAY',
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    user_id         UUID NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_task_lists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_task_lists_name_user UNIQUE (user_id, name),
    CONSTRAINT ck_task_lists_name_length CHECK (char_length(name) >= 1),
    CONSTRAINT ck_task_lists_color CHECK (color IN ('RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'PURPLE', 'PINK', 'GRAY'))
);

CREATE TRIGGER trg_task_lists_updated_at
    BEFORE UPDATE ON task_lists
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE task_lists IS 'User-created task categories (Task Management context)';
COMMENT ON COLUMN task_lists.is_default IS 'True for the auto-created Inbox list';
COMMENT ON COLUMN task_lists.color IS 'One of 8 preset colors: RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE, PINK, GRAY';
COMMENT ON COLUMN task_lists.sort_order IS 'User-defined display order in sidebar';
```

### 3.4 tasks

Stores individual tasks. Aggregate root for the Task Management context.

```sql
CREATE TABLE tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    priority            VARCHAR(10) NOT NULL DEFAULT 'P4',
    due_date            DATE,
    reminder_interval   VARCHAR(30) NOT NULL DEFAULT 'NONE',
    is_completed        BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    is_today            BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at        TIMESTAMP WITH TIME ZONE,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    list_id             UUID NOT NULL,
    user_id             UUID NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_tasks_list FOREIGN KEY (list_id) REFERENCES task_lists(id),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_tasks_title_length CHECK (char_length(title) >= 1),
    CONSTRAINT ck_tasks_description_length CHECK (description IS NULL OR char_length(description) <= 2000),
    CONSTRAINT ck_tasks_priority CHECK (priority IN ('P1', 'P2', 'P3', 'P4')),
    CONSTRAINT ck_tasks_reminder CHECK (reminder_interval IN ('NONE', 'AT_DUE', 'FIFTEEN_MINUTES', 'THIRTY_MINUTES', 'ONE_HOUR', 'ONE_DAY')),
    CONSTRAINT ck_tasks_completed_at CHECK (
        (is_completed = TRUE AND completed_at IS NOT NULL) OR
        (is_completed = FALSE AND completed_at IS NULL)
    )
);

CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

COMMENT ON TABLE tasks IS 'Individual tasks with lifecycle tracking (Task Management context)';
COMMENT ON COLUMN tasks.priority IS 'P1=Urgent, P2=High, P3=Medium, P4=Low';
COMMENT ON COLUMN tasks.reminder_interval IS 'Browser notification interval before due date';
COMMENT ON COLUMN tasks.is_today IS 'Manually added to Today View (independent of due date)';
COMMENT ON COLUMN tasks.is_deleted IS 'Soft delete flag. Excluded from all queries.';
COMMENT ON COLUMN tasks.sort_order IS 'User-defined display order within a list';
```

---

## 4. Indexes

### Performance Indexes

```sql
-- ============================================================
-- users
-- ============================================================
-- Primary lookup by email (login, registration check)
CREATE INDEX idx_users_email ON users (email);

-- Password reset token lookup
CREATE INDEX idx_users_password_reset_token ON users (password_reset_token)
    WHERE password_reset_token IS NOT NULL;

-- ============================================================
-- refresh_tokens
-- ============================================================
-- Token lookup (refresh flow)
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);

-- Find all tokens for a user (revoke-all on logout)
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

-- Cleanup expired tokens (scheduled job)
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at)
    WHERE is_revoked = FALSE;

-- ============================================================
-- task_lists
-- ============================================================
-- List all lists for a user (sidebar navigation)
CREATE INDEX idx_task_lists_user_id ON task_lists (user_id, sort_order);

-- Find default (Inbox) list for a user
CREATE INDEX idx_task_lists_user_default ON task_lists (user_id)
    WHERE is_default = TRUE;

-- ============================================================
-- tasks
-- ============================================================
-- Primary query: active tasks for a user, sorted by priority (default view)
CREATE INDEX idx_tasks_user_active ON tasks (user_id, priority, sort_order)
    WHERE is_deleted = FALSE AND is_completed = FALSE;

-- Filter by list: tasks in a specific list
CREATE INDEX idx_tasks_list_active ON tasks (list_id, priority, sort_order)
    WHERE is_deleted = FALSE AND is_completed = FALSE;

-- Today View: tasks due today or marked as today
CREATE INDEX idx_tasks_today_view ON tasks (user_id, due_date)
    WHERE is_deleted = FALSE AND is_completed = FALSE AND (is_today = TRUE OR due_date IS NOT NULL);

-- Overdue tasks: past due date, not completed
CREATE INDEX idx_tasks_overdue ON tasks (user_id, due_date)
    WHERE is_deleted = FALSE AND is_completed = FALSE AND due_date IS NOT NULL;

-- Completed today: for "done today" section
CREATE INDEX idx_tasks_completed_today ON tasks (user_id, completed_at)
    WHERE is_deleted = FALSE AND is_completed = TRUE;

-- Task count by list: sidebar badge counts
CREATE INDEX idx_tasks_count_by_list ON tasks (list_id, user_id)
    WHERE is_deleted = FALSE AND is_completed = FALSE;

-- Full-text search on title (future: FR-009)
CREATE INDEX idx_tasks_title_search ON tasks USING gin(to_tsvector('english', title));
```

### Index Strategy Summary

| Query Pattern | Index Used | Expected Rows |
|--------------|-----------|---------------|
| Login by email | `idx_users_email` | 1 |
| Active tasks (default view) | `idx_tasks_user_active` | ~30 per user |
| Tasks in a list | `idx_tasks_list_active` | ~10 per list |
| Today View | `idx_tasks_today_view` | ~5-10 per user |
| Overdue tasks | `idx_tasks_overdue` | ~3-5 per user |
| Completed today | `idx_tasks_completed_today` | ~5-10 per user |
| Sidebar list counts | `idx_tasks_count_by_list` | ~5 lists per user |
| Token refresh | `idx_refresh_tokens_token` | 1 |

---

## 5. Flyway Migration Plan

All migrations are stored in `backend/src/main/resources/db/migration/`.

| Version | Filename | Description | Sprint |
|---------|----------|-------------|--------|
| V1 | `V1__create_utility_functions.sql` | `update_updated_at_column()` function | Sprint 1 |
| V2 | `V2__create_users_table.sql` | `users` table + indexes | Sprint 1 |
| V3 | `V3__create_refresh_tokens_table.sql` | `refresh_tokens` table + indexes | Sprint 1 |
| V4 | `V4__create_task_lists_table.sql` | `task_lists` table + indexes | Sprint 1 |
| V5 | `V5__create_tasks_table.sql` | `tasks` table + indexes | Sprint 1 |
| V6 | `V6__add_full_text_search_index.sql` | GIN index for title search | Sprint 2 |

### V1: Utility Functions

```sql
-- V1__create_utility_functions.sql

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;
```

### V2: Users Table

```sql
-- V2__create_users_table.sql

CREATE TABLE users (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email                   VARCHAR(255) NOT NULL,
    password_hash           VARCHAR(255) NOT NULL,
    display_name            VARCHAR(100) NOT NULL,
    is_locked               BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_attempts   INTEGER NOT NULL DEFAULT 0,
    locked_until            TIMESTAMP WITH TIME ZONE,
    password_reset_token    VARCHAR(255),
    password_reset_expiry   TIMESTAMP WITH TIME ZONE,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version                 INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_email_length CHECK (char_length(email) >= 3),
    CONSTRAINT ck_users_display_name_length CHECK (char_length(display_name) >= 1),
    CONSTRAINT ck_users_failed_attempts CHECK (failed_login_attempts >= 0)
);

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_password_reset_token ON users (password_reset_token)
    WHERE password_reset_token IS NOT NULL;
```

### V3: Refresh Tokens Table

```sql
-- V3__create_refresh_tokens_table.sql

CREATE TABLE refresh_tokens (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token           VARCHAR(255) NOT NULL,
    user_id         UUID NOT NULL,
    expires_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at)
    WHERE is_revoked = FALSE;
```

### V4: Task Lists Table

```sql
-- V4__create_task_lists_table.sql

CREATE TABLE task_lists (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(50) NOT NULL,
    color           VARCHAR(20) NOT NULL DEFAULT 'GRAY',
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    user_id         UUID NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version         INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_task_lists_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_task_lists_name_user UNIQUE (user_id, name),
    CONSTRAINT ck_task_lists_name_length CHECK (char_length(name) >= 1),
    CONSTRAINT ck_task_lists_color CHECK (color IN ('RED', 'ORANGE', 'YELLOW', 'GREEN', 'BLUE', 'PURPLE', 'PINK', 'GRAY'))
);

CREATE TRIGGER trg_task_lists_updated_at
    BEFORE UPDATE ON task_lists
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE INDEX idx_task_lists_user_id ON task_lists (user_id, sort_order);
CREATE INDEX idx_task_lists_user_default ON task_lists (user_id)
    WHERE is_default = TRUE;
```

### V5: Tasks Table

```sql
-- V5__create_tasks_table.sql

CREATE TABLE tasks (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title               VARCHAR(255) NOT NULL,
    description         TEXT,
    priority            VARCHAR(10) NOT NULL DEFAULT 'P4',
    due_date            DATE,
    reminder_interval   VARCHAR(30) NOT NULL DEFAULT 'NONE',
    is_completed        BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted          BOOLEAN NOT NULL DEFAULT FALSE,
    is_today            BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at        TIMESTAMP WITH TIME ZONE,
    sort_order          INTEGER NOT NULL DEFAULT 0,
    list_id             UUID NOT NULL,
    user_id             UUID NOT NULL,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version             INTEGER NOT NULL DEFAULT 0,

    CONSTRAINT fk_tasks_list FOREIGN KEY (list_id) REFERENCES task_lists(id),
    CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT ck_tasks_title_length CHECK (char_length(title) >= 1),
    CONSTRAINT ck_tasks_description_length CHECK (description IS NULL OR char_length(description) <= 2000),
    CONSTRAINT ck_tasks_priority CHECK (priority IN ('P1', 'P2', 'P3', 'P4')),
    CONSTRAINT ck_tasks_reminder CHECK (reminder_interval IN ('NONE', 'AT_DUE', 'FIFTEEN_MINUTES', 'THIRTY_MINUTES', 'ONE_HOUR', 'ONE_DAY')),
    CONSTRAINT ck_tasks_completed_at CHECK (
        (is_completed = TRUE AND completed_at IS NOT NULL) OR
        (is_completed = FALSE AND completed_at IS NULL)
    )
);

CREATE TRIGGER trg_tasks_updated_at
    BEFORE UPDATE ON tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Active tasks (default view)
CREATE INDEX idx_tasks_user_active ON tasks (user_id, priority, sort_order)
    WHERE is_deleted = FALSE AND is_completed = FALSE;

-- Filter by list
CREATE INDEX idx_tasks_list_active ON tasks (list_id, priority, sort_order)
    WHERE is_deleted = FALSE AND is_completed = FALSE;

-- Today View
CREATE INDEX idx_tasks_today_view ON tasks (user_id, due_date)
    WHERE is_deleted = FALSE AND is_completed = FALSE AND (is_today = TRUE OR due_date IS NOT NULL);

-- Overdue tasks
CREATE INDEX idx_tasks_overdue ON tasks (user_id, due_date)
    WHERE is_deleted = FALSE AND is_completed = FALSE AND due_date IS NOT NULL;

-- Completed today
CREATE INDEX idx_tasks_completed_today ON tasks (user_id, completed_at)
    WHERE is_deleted = FALSE AND is_completed = TRUE;

-- Task count by list (sidebar badges)
CREATE INDEX idx_tasks_count_by_list ON tasks (list_id, user_id)
    WHERE is_deleted = FALSE AND is_completed = FALSE;
```

### V6: Full-Text Search Index

```sql
-- V6__add_full_text_search_index.sql

CREATE INDEX idx_tasks_title_search ON tasks USING gin(to_tsvector('english', title));
```

---

## 6. ERD Diagram

### ASCII ERD

```
┌──────────────────────────────────┐
│              users               │
├──────────────────────────────────┤
│ PK  id: UUID                     │
│     email: VARCHAR(255) UNIQUE   │
│     password_hash: VARCHAR(255)  │
│     display_name: VARCHAR(100)   │
│     is_locked: BOOLEAN           │
│     failed_login_attempts: INT   │
│     locked_until: TIMESTAMPTZ    │
│     password_reset_token: VARCHAR│
│     password_reset_expiry: TSTZ  │
│     created_at: TIMESTAMPTZ      │
│     updated_at: TIMESTAMPTZ      │
│     version: INT                 │
└───────────┬──────────┬───────────┘
            │          │
            │ 1:N      │ 1:N
            │          │
┌───────────▼──────┐   │   ┌──────────────────────────────────┐
│ refresh_tokens   │   │   │           task_lists              │
├──────────────────┤   │   ├──────────────────────────────────┤
│ PK  id: UUID     │   │   │ PK  id: UUID                     │
│     token: VARCHAR│   │   │     name: VARCHAR(50)             │
│ FK  user_id: UUID│   │   │     color: VARCHAR(20)            │
│     expires_at   │   │   │     sort_order: INT               │
│     is_revoked   │   │   │     is_default: BOOLEAN           │
│     created_at   │   │   │ FK  user_id: UUID                 │
└──────────────────┘   │   │     created_at: TIMESTAMPTZ       │
                       │   │     updated_at: TIMESTAMPTZ       │
                       │   │     version: INT                  │
                       │   └──────────┬───────────────────────┘
                       │              │
                       │ 1:N          │ 1:N
                       │              │
                       │   ┌──────────▼───────────────────────┐
                       │   │              tasks                │
                       │   ├──────────────────────────────────┤
                       │   │ PK  id: UUID                     │
                       │   │     title: VARCHAR(255)           │
                       │   │     description: TEXT             │
                       │   │     priority: VARCHAR(10)         │
                       │   │     due_date: DATE                │
                       │   │     reminder_interval: VARCHAR(30)│
                       │   │     is_completed: BOOLEAN         │
                       │   │     is_deleted: BOOLEAN           │
                       │   │     is_today: BOOLEAN             │
                       │   │     completed_at: TIMESTAMPTZ     │
                       │   │     sort_order: INT               │
                       └───│ FK  list_id: UUID                 │
                           │ FK  user_id: UUID                 │
                           │     created_at: TIMESTAMPTZ       │
                           │     updated_at: TIMESTAMPTZ       │
                           │     version: INT                  │
                           └──────────────────────────────────┘
```

### Mermaid ERD

See: `docs/tech-specs/diagrams/erd.mmd`

---

## 7. Redis Key Schema

Redis is used for caching and token management. All keys use a structured namespace.

### Key Patterns

| Pattern | Type | TTL | Description |
|---------|------|-----|-------------|
| `token:blacklist:{jti}` | STRING | 15 min | Blacklisted JWT access token (logout) |
| `cache:tasks:user:{userId}:list:{listId}` | STRING (JSON) | 5 min | Cached task list query result |
| `cache:today:user:{userId}` | STRING (JSON) | 2 min | Cached Today View response |
| `cache:lists:user:{userId}` | STRING (JSON) | 10 min | Cached sidebar list with counts |
| `rate:auth:{ip}` | STRING (counter) | 1 min | Auth endpoint rate limit counter |
| `rate:forgot:{ip}` | STRING (counter) | 1 min | Forgot password rate limit counter |

### Cache Invalidation Rules

| Event | Keys Invalidated |
|-------|-----------------|
| Task created | `cache:tasks:user:{userId}:*`, `cache:today:user:{userId}`, `cache:lists:user:{userId}` |
| Task updated | `cache:tasks:user:{userId}:*`, `cache:today:user:{userId}` |
| Task completed | `cache:tasks:user:{userId}:*`, `cache:today:user:{userId}`, `cache:lists:user:{userId}` |
| Task deleted | `cache:tasks:user:{userId}:*`, `cache:today:user:{userId}`, `cache:lists:user:{userId}` |
| Task moved to list | `cache:tasks:user:{userId}:*`, `cache:lists:user:{userId}` |
| Task toggled today | `cache:today:user:{userId}` |
| List created | `cache:lists:user:{userId}` |
| List updated | `cache:lists:user:{userId}` |
| List deleted | `cache:tasks:user:{userId}:*`, `cache:today:user:{userId}`, `cache:lists:user:{userId}` |
| User logout | `token:blacklist:{jti}` added |

### Graceful Degradation

If Redis is unavailable, the application falls back to database queries. Cache misses are logged at WARN level. No cache writes are attempted during Redis outage (circuit breaker pattern).

---

## 8. Data Volume Estimates

### Per-User Averages (based on Todoist benchmarks)

| Metric | Estimate |
|--------|----------|
| Active tasks per user | 30 |
| Completed tasks per user | 200 (lifetime) |
| Task lists per user | 5 |
| Tasks per list | 6-10 |
| Today View tasks | 5-10 |
| Overdue tasks | 3-5 |

### At MVP Scale (1,000 users)

| Table | Rows | Avg Row Size | Total Size |
|-------|------|-------------|------------|
| `users` | 1,000 | 500 bytes | ~500 KB |
| `refresh_tokens` | 2,000 | 200 bytes | ~400 KB |
| `task_lists` | 5,000 | 200 bytes | ~1 MB |
| `tasks` | 230,000 | 400 bytes | ~92 MB |
| **Total** | **238,000** | | **~94 MB** |

PostgreSQL connection pool: HikariCP with `maximumPoolSize: 10` (sufficient for 1K users).

---

*Generated by SDLC Factory -- Phase 4: Design (Tech Specs)*
