# Clarity (todo-app) -- Low-Level Design: API Contracts

**Project:** todo-app
**Phase:** Design (LLD)
**Created:** 2026-03-07
**Status:** Draft
**API Base URL:** `/api/v1`
**Based on:** docs/architecture/hld/system-architecture.md, docs/prd/prd.md, docs/architecture/lld/class-design.md

---

## Table of Contents

1. [Global Conventions](#1-global-conventions)
2. [Error Response Format](#2-error-response-format)
3. [Auth API](#3-auth-api)
4. [Task API](#4-task-api)
5. [Task List API](#5-task-list-api)
6. [Health API](#6-health-api)

---

## 1. Global Conventions

### Base URL

All API endpoints are prefixed with `/api/v1`.

### Authentication

Unless otherwise noted, all endpoints require a valid JWT access token in the `Authorization` header:

```
Authorization: Bearer <access_token>
```

### Content Type

All request and response bodies use `application/json`.

### Pagination

List endpoints that return collections use Spring's `Pageable` convention:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | int | 0 | Zero-indexed page number |
| `size` | int | 50 | Items per page (max: 200) |
| `sort` | string | `priority,asc` | Sort field and direction |

Paginated responses include:

```json
{
  "content": [...],
  "page": 0,
  "size": 50,
  "totalElements": 142,
  "totalPages": 3
}
```

The `X-Total-Count` header is also set for convenience.

### UUID Format

All entity IDs are UUID v4 strings: `"550e8400-e29b-41d4-a716-446655440000"`.

### Timestamps

All timestamps use ISO 8601 format in UTC: `"2026-03-07T12:00:00Z"`.

### Date Fields

Date-only fields (e.g., `dueDate`) use ISO 8601 date format: `"2026-03-07"`.

---

## 2. Error Response Format

All error responses follow a consistent structure:

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {"field": "title", "message": "must not be blank"}
  ],
  "timestamp": "2026-03-07T12:00:00Z",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `status` | int | HTTP status code |
| `code` | string | Machine-readable error code |
| `message` | string | Human-readable error description |
| `details` | array (nullable) | Field-level validation errors (only for 400) |
| `timestamp` | string | ISO 8601 UTC timestamp |
| `traceId` | string | Correlation ID for log tracing |

### Error Codes

| Code | HTTP Status | When |
|------|-------------|------|
| `VALIDATION_ERROR` | 400 | Bean Validation failure on request body |
| `INVALID_CREDENTIALS` | 401 | Wrong email or password |
| `TOKEN_EXPIRED` | 401 | JWT access token expired |
| `TOKEN_INVALID` | 401 | JWT malformed or signature invalid |
| `ACCESS_DENIED` | 403 | User not authorized to access this resource |
| `RESOURCE_NOT_FOUND` | 404 | Entity not found by ID |
| `EMAIL_ALREADY_EXISTS` | 409 | Registration with existing email (generic message to client) |
| `BUSINESS_RULE_VIOLATION` | 422 | Domain invariant violated |
| `ACCOUNT_LOCKED` | 423 | Account locked after too many failed attempts |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## 3. Auth API

Base path: `/api/v1/auth`

### POST /api/v1/auth/register

Register a new user account.

| Field | Value |
|-------|-------|
| **Auth Required** | No |
| **Rate Limit** | 10 requests/minute per IP |

**Request Body:**

```json
{
  "email": "sam@example.com",
  "password": "SecurePass1",
  "displayName": "Sam Rivera"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `email` | string | Yes | Valid email format, max 255 chars |
| `password` | string | Yes | Min 8 chars, at least 1 uppercase letter, at least 1 digit |
| `displayName` | string | Yes | 1-100 chars |

**Success Response:** `201 Created`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

| Field | Type | Description |
|-------|------|-------------|
| `accessToken` | string | JWT access token (15 min expiry) |
| `refreshToken` | string | Opaque refresh token (7 day expiry) |
| `expiresIn` | long | Seconds until access token expiry |
| `tokenType` | string | Always "Bearer" |

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Missing or invalid fields |
| 409 | `EMAIL_ALREADY_EXISTS` | Email already registered (response uses generic message: "Registration failed. Please try again.") |
| 429 | `RATE_LIMIT_EXCEEDED` | Rate limit exceeded |

**Example Error (400):**

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {"field": "password", "message": "Password must be at least 8 characters"},
    {"field": "password", "message": "Password must contain at least 1 uppercase letter and 1 number"}
  ],
  "timestamp": "2026-03-07T12:00:00Z",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### POST /api/v1/auth/login

Authenticate with email and password.

| Field | Value |
|-------|-------|
| **Auth Required** | No |
| **Rate Limit** | 10 requests/minute per IP |

**Request Body:**

```json
{
  "email": "sam@example.com",
  "password": "SecurePass1"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `email` | string | Yes | Valid email format |
| `password` | string | Yes | Not blank |

**Success Response:** `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "d290f1ee-6c54-4b01-90e6-d701748f0851",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Missing fields |
| 401 | `INVALID_CREDENTIALS` | Wrong email or password (same message for both) |
| 423 | `ACCOUNT_LOCKED` | Account locked after 5 failed attempts |
| 429 | `RATE_LIMIT_EXCEEDED` | Rate limit exceeded |

**Example Error (423):**

```json
{
  "status": 423,
  "code": "ACCOUNT_LOCKED",
  "message": "Account is locked due to too many failed login attempts. Please try again in 14 minutes.",
  "details": null,
  "timestamp": "2026-03-07T12:00:00Z",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### POST /api/v1/auth/logout

Invalidate the current refresh token and blacklist the access token.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Request Body:**

```json
{
  "refreshToken": "d290f1ee-6c54-4b01-90e6-d701748f0851"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `refreshToken` | string | Yes | Not blank |

**Success Response:** `204 No Content`

No response body.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Missing or invalid access token |

---

### POST /api/v1/auth/refresh

Refresh an expired access token using a valid refresh token.

| Field | Value |
|-------|-------|
| **Auth Required** | No (uses refresh token in body) |
| **Rate Limit** | 10 requests/minute per IP |

**Request Body:**

```json
{
  "refreshToken": "d290f1ee-6c54-4b01-90e6-d701748f0851"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `refreshToken` | string | Yes | Not blank |

**Success Response:** `200 OK`

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "f390a2ff-7d65-5c12-a1f7-e812859f1962",
  "expiresIn": 900,
  "tokenType": "Bearer"
}
```

Note: A **new** refresh token is issued (refresh token rotation). The old refresh token is revoked.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Missing refresh token |
| 401 | `TOKEN_EXPIRED` | Refresh token expired (7 day window passed) |
| 401 | `TOKEN_INVALID` | Refresh token not found or already revoked |

---

### POST /api/v1/auth/forgot-password

Request a password reset link. Always returns 200 regardless of whether the email exists (prevents email enumeration).

| Field | Value |
|-------|-------|
| **Auth Required** | No |
| **Rate Limit** | 5 requests/minute per IP |

**Request Body:**

```json
{
  "email": "sam@example.com"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `email` | string | Yes | Valid email format |

**Success Response:** `200 OK`

```json
{
  "message": "If an account exists with this email, a password reset link has been sent."
}
```

This response is returned regardless of whether the email exists in the system.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Invalid email format |
| 429 | `RATE_LIMIT_EXCEEDED` | Rate limit exceeded |

---

### POST /api/v1/auth/reset-password

Reset password using a valid reset token.

| Field | Value |
|-------|-------|
| **Auth Required** | No (uses reset token in body) |
| **Rate Limit** | 5 requests/minute per IP |

**Request Body:**

```json
{
  "token": "a7b8c9d0-e1f2-3456-7890-abcdef123456",
  "newPassword": "NewSecurePass1"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `token` | string | Yes | Not blank |
| `newPassword` | string | Yes | Min 8 chars, at least 1 uppercase, at least 1 digit |

**Success Response:** `200 OK`

```json
{
  "message": "Password has been reset successfully. Please log in with your new password."
}
```

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Missing or invalid fields |
| 401 | `TOKEN_EXPIRED` | Reset token expired (1 hour window passed) |
| 401 | `TOKEN_INVALID` | Reset token not found or already used |
| 429 | `RATE_LIMIT_EXCEEDED` | Rate limit exceeded |

---

## 4. Task API

Base path: `/api/v1/tasks`

All endpoints require authentication. Tasks are scoped to the authenticated user -- a user can only access their own tasks.

### GET /api/v1/tasks

List tasks for the authenticated user with optional filters and pagination.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `listId` | UUID | No | (all lists) | Filter by task list |
| `completed` | boolean | No | `false` | Include completed tasks |
| `priority` | string | No | (all) | Filter by priority: `P1`, `P2`, `P3`, `P4` |
| `sort` | string | No | `priority,asc` | Sort field: `priority`, `dueDate`, `createdAt` with direction `asc` or `desc` |
| `page` | int | No | 0 | Page number (0-indexed) |
| `size` | int | No | 50 | Page size (max 200) |

**Example Request:**

```
GET /api/v1/tasks?listId=550e8400-e29b-41d4-a716-446655440000&sort=priority,asc&page=0&size=50
Authorization: Bearer <token>
```

**Success Response:** `200 OK`

```json
{
  "content": [
    {
      "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "title": "Prepare quarterly report",
      "description": "Compile Q1 data from all departments",
      "priority": "P1",
      "priorityLabel": "Urgent",
      "priorityColor": "#EF4444",
      "dueDate": "2026-03-10",
      "isCompleted": false,
      "isToday": true,
      "completedAt": null,
      "listId": "550e8400-e29b-41d4-a716-446655440000",
      "listName": "Work",
      "createdAt": "2026-03-05T10:30:00Z",
      "updatedAt": "2026-03-07T08:15:00Z"
    },
    {
      "id": "a23bc45d-67ef-8901-2345-6789abcdef01",
      "title": "Buy groceries",
      "description": null,
      "priority": "P3",
      "priorityLabel": "Medium",
      "priorityColor": "#3B82F6",
      "dueDate": null,
      "isCompleted": false,
      "isToday": false,
      "completedAt": null,
      "listId": "660e8400-e29b-41d4-a716-446655440001",
      "listName": "Personal",
      "createdAt": "2026-03-06T14:00:00Z",
      "updatedAt": "2026-03-06T14:00:00Z"
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 2,
  "totalPages": 1
}
```

**Response Fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Task unique identifier |
| `title` | string | Task title |
| `description` | string (nullable) | Task description |
| `priority` | string | Priority level: `P1`, `P2`, `P3`, `P4` |
| `priorityLabel` | string | Human-readable: `Urgent`, `High`, `Medium`, `Low` |
| `priorityColor` | string | Hex color code for the priority |
| `dueDate` | date (nullable) | Due date in ISO 8601 format |
| `isCompleted` | boolean | Whether the task is completed |
| `isToday` | boolean | Whether the task is marked for Today View |
| `completedAt` | datetime (nullable) | When the task was completed |
| `listId` | UUID | ID of the task list |
| `listName` | string | Name of the task list |
| `createdAt` | datetime | Creation timestamp |
| `updatedAt` | datetime | Last update timestamp |

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |

---

### GET /api/v1/tasks/{id}

Get a single task by ID.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Task ID |

**Success Response:** `200 OK`

```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "title": "Prepare quarterly report",
  "description": "Compile Q1 data from all departments",
  "priority": "P1",
  "priorityLabel": "Urgent",
  "priorityColor": "#EF4444",
  "dueDate": "2026-03-10",
  "isCompleted": false,
  "isToday": true,
  "completedAt": null,
  "listId": "550e8400-e29b-41d4-a716-446655440000",
  "listName": "Work",
  "createdAt": "2026-03-05T10:30:00Z",
  "updatedAt": "2026-03-07T08:15:00Z"
}
```

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | Task belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | Task not found or soft-deleted |

---

### POST /api/v1/tasks

Create a new task.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Request Body:**

```json
{
  "title": "Prepare quarterly report",
  "description": "Compile Q1 data from all departments",
  "priority": "P1",
  "dueDate": "2026-03-10",
  "listId": "550e8400-e29b-41d4-a716-446655440000"
}
```

| Field | Type | Required | Default | Validation |
|-------|------|----------|---------|------------|
| `title` | string | Yes | -- | 1-255 chars, not blank |
| `description` | string | No | `null` | Max 2000 chars |
| `priority` | string | No | `"P4"` | One of: `P1`, `P2`, `P3`, `P4` |
| `dueDate` | date | No | `null` | ISO 8601 date (must not be in the past) |
| `listId` | UUID | No | User's Inbox list ID | Valid list owned by the user |

**Success Response:** `201 Created`

```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "title": "Prepare quarterly report",
  "description": "Compile Q1 data from all departments",
  "priority": "P1",
  "priorityLabel": "Urgent",
  "priorityColor": "#EF4444",
  "dueDate": "2026-03-10",
  "isCompleted": false,
  "isToday": false,
  "completedAt": null,
  "listId": "550e8400-e29b-41d4-a716-446655440000",
  "listName": "Work",
  "createdAt": "2026-03-07T12:00:00Z",
  "updatedAt": "2026-03-07T12:00:00Z"
}
```

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Missing title, invalid priority, etc. |
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 404 | `RESOURCE_NOT_FOUND` | Specified `listId` not found |
| 422 | `BUSINESS_RULE_VIOLATION` | List does not belong to user |

**Example Error (400):**

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Validation failed",
  "details": [
    {"field": "title", "message": "must not be blank"},
    {"field": "priority", "message": "must be one of: P1, P2, P3, P4"}
  ],
  "timestamp": "2026-03-07T12:00:00Z",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

### PUT /api/v1/tasks/{id}

Update an existing task. Only provided fields are updated (partial update semantics, despite PUT verb -- all fields are optional except the path ID).

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Task ID |

**Request Body:**

```json
{
  "title": "Prepare quarterly report (updated)",
  "description": "Compile Q1 data from all departments and finance",
  "priority": "P2",
  "dueDate": "2026-03-12",
  "listId": "660e8400-e29b-41d4-a716-446655440001"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `title` | string | No | 1-255 chars if provided |
| `description` | string | No | Max 2000 chars |
| `priority` | string | No | One of: `P1`, `P2`, `P3`, `P4` |
| `dueDate` | date | No | ISO 8601 date, or `null` to clear |
| `listId` | UUID | No | Valid list owned by user |

**Success Response:** `200 OK`

Returns the full updated `TaskResponse` (same schema as GET /api/v1/tasks/{id}).

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Invalid field values |
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | Task belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | Task not found or soft-deleted |
| 422 | `BUSINESS_RULE_VIOLATION` | Domain invariant violation (e.g., empty title) |

---

### DELETE /api/v1/tasks/{id}

Soft-delete a task. Sets `is_deleted = true`. The task is excluded from all queries but remains in the database.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Task ID |

**Success Response:** `204 No Content`

No response body.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | Task belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | Task not found or already deleted |

---

### PATCH /api/v1/tasks/{id}/complete

Mark a task as completed. Sets `is_completed = true` and `completed_at = now()`.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Task ID |

**Request Body:** None required.

**Success Response:** `200 OK`

Returns the full updated `TaskResponse` with `isCompleted: true` and `completedAt` set.

```json
{
  "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "title": "Prepare quarterly report",
  "description": "Compile Q1 data from all departments",
  "priority": "P1",
  "priorityLabel": "Urgent",
  "priorityColor": "#EF4444",
  "dueDate": "2026-03-10",
  "isCompleted": true,
  "isToday": true,
  "completedAt": "2026-03-07T14:30:00Z",
  "listId": "550e8400-e29b-41d4-a716-446655440000",
  "listName": "Work",
  "createdAt": "2026-03-05T10:30:00Z",
  "updatedAt": "2026-03-07T14:30:00Z"
}
```

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | Task belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | Task not found or soft-deleted |
| 422 | `BUSINESS_RULE_VIOLATION` | Task is already deleted |

---

### PATCH /api/v1/tasks/{id}/uncomplete

Revert a completed task to active. Sets `is_completed = false` and clears `completed_at`. Typically called when the user clicks "Undo" within the 5-second window after completing a task.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Task ID |

**Request Body:** None required.

**Success Response:** `200 OK`

Returns the full updated `TaskResponse` with `isCompleted: false` and `completedAt: null`.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | Task belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | Task not found or soft-deleted |

Note: Calling uncomplete on an already-active task is idempotent and returns 200 with the current state.

---

### GET /api/v1/tasks/today

Get the Today View data: today's planned tasks, overdue tasks, and tasks completed today.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Query Parameters:** None.

**Success Response:** `200 OK`

```json
{
  "todayTasks": [
    {
      "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
      "title": "Prepare quarterly report",
      "description": "Compile Q1 data from all departments",
      "priority": "P1",
      "priorityLabel": "Urgent",
      "priorityColor": "#EF4444",
      "dueDate": "2026-03-07",
      "isCompleted": false,
      "isToday": true,
      "completedAt": null,
      "listId": "550e8400-e29b-41d4-a716-446655440000",
      "listName": "Work",
      "createdAt": "2026-03-05T10:30:00Z",
      "updatedAt": "2026-03-07T08:15:00Z"
    }
  ],
  "overdueTasks": [
    {
      "id": "b12cd34e-56fg-7890-1234-567890abcdef",
      "title": "Submit tax documents",
      "description": null,
      "priority": "P2",
      "priorityLabel": "High",
      "priorityColor": "#F97316",
      "dueDate": "2026-03-05",
      "isCompleted": false,
      "isToday": false,
      "completedAt": null,
      "listId": "660e8400-e29b-41d4-a716-446655440001",
      "listName": "Personal",
      "createdAt": "2026-03-01T09:00:00Z",
      "updatedAt": "2026-03-01T09:00:00Z"
    }
  ],
  "doneTodayTasks": [
    {
      "id": "c23de45f-67gh-8901-2345-678901bcdef0",
      "title": "Morning standup",
      "description": null,
      "priority": "P3",
      "priorityLabel": "Medium",
      "priorityColor": "#3B82F6",
      "dueDate": "2026-03-07",
      "isCompleted": true,
      "isToday": true,
      "completedAt": "2026-03-07T09:15:00Z",
      "listId": "550e8400-e29b-41d4-a716-446655440000",
      "listName": "Work",
      "createdAt": "2026-03-06T17:00:00Z",
      "updatedAt": "2026-03-07T09:15:00Z"
    }
  ],
  "totalToday": 4,
  "completedToday": 1
}
```

| Field | Type | Description |
|-------|------|-------------|
| `todayTasks` | array | Active tasks where `due_date = today` OR `is_today = true`, sorted by priority |
| `overdueTasks` | array | Active tasks where `due_date < today`, sorted by due date ascending |
| `doneTodayTasks` | array | Tasks where `completed_at` is today, sorted by completion time descending |
| `totalToday` | int | Count of `todayTasks` + `doneTodayTasks` (today's total scope) |
| `completedToday` | int | Count of `doneTodayTasks` |

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |

---

### PATCH /api/v1/tasks/{id}/today

Toggle the `is_today` flag on a task. If currently `false`, sets to `true` (add to Today View). If currently `true`, sets to `false` (remove from Today View).

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | Task ID |

**Request Body:** None required.

**Success Response:** `200 OK`

Returns the full updated `TaskResponse` with the toggled `isToday` value.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | Task belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | Task not found or soft-deleted |

---

## 5. Task List API

Base path: `/api/v1/lists`

All endpoints require authentication. Lists are scoped to the authenticated user.

### GET /api/v1/lists

Get all task lists for the authenticated user.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Query Parameters:** None.

**Success Response:** `200 OK`

```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Inbox",
    "color": "GRAY",
    "colorHex": "#6B7280",
    "sortOrder": 0,
    "isDefault": true,
    "createdAt": "2026-03-01T10:00:00Z",
    "updatedAt": "2026-03-01T10:00:00Z"
  },
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Work",
    "color": "BLUE",
    "colorHex": "#3B82F6",
    "sortOrder": 1,
    "isDefault": false,
    "createdAt": "2026-03-02T12:00:00Z",
    "updatedAt": "2026-03-05T08:00:00Z"
  },
  {
    "id": "770e8400-e29b-41d4-a716-446655440002",
    "name": "Personal",
    "color": "GREEN",
    "colorHex": "#22C55E",
    "sortOrder": 2,
    "isDefault": false,
    "createdAt": "2026-03-02T12:05:00Z",
    "updatedAt": "2026-03-02T12:05:00Z"
  }
]
```

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | List unique identifier |
| `name` | string | List display name |
| `color` | string | Color enum name: `RED`, `ORANGE`, `YELLOW`, `GREEN`, `BLUE`, `PURPLE`, `PINK`, `GRAY` |
| `colorHex` | string | Hex color code for rendering |
| `sortOrder` | int | Display order in sidebar |
| `isDefault` | boolean | True for the Inbox list (cannot be deleted) |
| `createdAt` | datetime | Creation timestamp |
| `updatedAt` | datetime | Last update timestamp |

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |

---

### GET /api/v1/lists/counts

Get all lists with their active (non-completed, non-deleted) task counts. Used by the sidebar for count badges.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Query Parameters:** None.

**Success Response:** `200 OK`

```json
[
  {
    "listId": "550e8400-e29b-41d4-a716-446655440000",
    "listName": "Inbox",
    "activeTaskCount": 5
  },
  {
    "listId": "660e8400-e29b-41d4-a716-446655440001",
    "listName": "Work",
    "activeTaskCount": 12
  },
  {
    "listId": "770e8400-e29b-41d4-a716-446655440002",
    "listName": "Personal",
    "activeTaskCount": 3
  }
]
```

| Field | Type | Description |
|-------|------|-------------|
| `listId` | UUID | List identifier |
| `listName` | string | List display name |
| `activeTaskCount` | long | Count of tasks where `is_completed = false` AND `is_deleted = false` |

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |

---

### POST /api/v1/lists

Create a new task list.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Request Body:**

```json
{
  "name": "Errands",
  "color": "ORANGE"
}
```

| Field | Type | Required | Default | Validation |
|-------|------|----------|---------|------------|
| `name` | string | Yes | -- | 1-50 chars, not blank |
| `color` | string | No | `"GRAY"` | One of: `RED`, `ORANGE`, `YELLOW`, `GREEN`, `BLUE`, `PURPLE`, `PINK`, `GRAY` |

**Success Response:** `201 Created`

```json
{
  "id": "880e8400-e29b-41d4-a716-446655440003",
  "name": "Errands",
  "color": "ORANGE",
  "colorHex": "#F97316",
  "sortOrder": 3,
  "isDefault": false,
  "createdAt": "2026-03-07T12:00:00Z",
  "updatedAt": "2026-03-07T12:00:00Z"
}
```

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Missing name, invalid color |
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 422 | `BUSINESS_RULE_VIOLATION` | List name already exists for this user |

---

### PUT /api/v1/lists/{id}

Update a task list's name and/or color.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | List ID |

**Request Body:**

```json
{
  "name": "Shopping",
  "color": "YELLOW"
}
```

| Field | Type | Required | Validation |
|-------|------|----------|------------|
| `name` | string | No | 1-50 chars if provided |
| `color` | string | No | One of the 8 preset colors |

**Success Response:** `200 OK`

Returns the full updated `TaskListResponse`.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 400 | `VALIDATION_ERROR` | Invalid field values |
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | List belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | List not found |
| 422 | `BUSINESS_RULE_VIOLATION` | Cannot rename the Inbox list, or name already exists |

---

### DELETE /api/v1/lists/{id}

Delete a task list. All tasks in the list are moved to the user's Inbox. The Inbox list cannot be deleted.

| Field | Value |
|-------|-------|
| **Auth Required** | Yes |
| **Rate Limit** | No |

**Path Parameters:**

| Parameter | Type | Description |
|-----------|------|-------------|
| `id` | UUID | List ID |

**Success Response:** `204 No Content`

No response body. All tasks from the deleted list have been moved to Inbox.

**Error Responses:**

| Status | Code | When |
|--------|------|------|
| 401 | `TOKEN_EXPIRED` / `TOKEN_INVALID` | Invalid or missing access token |
| 403 | `ACCESS_DENIED` | List belongs to a different user |
| 404 | `RESOURCE_NOT_FOUND` | List not found |
| 422 | `BUSINESS_RULE_VIOLATION` | Cannot delete the Inbox list |

**Example Error (422):**

```json
{
  "status": 422,
  "code": "BUSINESS_RULE_VIOLATION",
  "message": "The Inbox list cannot be deleted.",
  "details": null,
  "timestamp": "2026-03-07T12:00:00Z",
  "traceId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

---

## 6. Health API

### GET /actuator/health

Spring Actuator health check endpoint.

| Field | Value |
|-------|-------|
| **Auth Required** | No |
| **Rate Limit** | No |

**Success Response:** `200 OK`

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP",
      "details": {
        "version": "7.0.0"
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 107374182400,
        "free": 53687091200,
        "threshold": 10485760
      }
    }
  }
}
```

**Degraded Response (Redis down):** `200 OK`

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "redis": { "status": "DOWN", "details": { "error": "Connection refused" } },
    "diskSpace": { "status": "UP" }
  }
}
```

Note: The application returns UP even if Redis is down (graceful degradation per NFR-003.3). Only PostgreSQL being down results in a `503 Service Unavailable`.

---

## Appendix: API Endpoint Summary

| Method | Path | Auth | Rate Limited | Description |
|--------|------|------|-------------|-------------|
| POST | `/api/v1/auth/register` | No | 10/min/IP | Register new user |
| POST | `/api/v1/auth/login` | No | 10/min/IP | Login |
| POST | `/api/v1/auth/logout` | Yes | No | Logout |
| POST | `/api/v1/auth/refresh` | No | 10/min/IP | Refresh access token |
| POST | `/api/v1/auth/forgot-password` | No | 5/min/IP | Request password reset |
| POST | `/api/v1/auth/reset-password` | No | 5/min/IP | Reset password |
| GET | `/api/v1/tasks` | Yes | No | List tasks (paginated, filtered) |
| GET | `/api/v1/tasks/{id}` | Yes | No | Get single task |
| POST | `/api/v1/tasks` | Yes | No | Create task |
| PUT | `/api/v1/tasks/{id}` | Yes | No | Update task |
| DELETE | `/api/v1/tasks/{id}` | Yes | No | Soft-delete task |
| PATCH | `/api/v1/tasks/{id}/complete` | Yes | No | Mark task complete |
| PATCH | `/api/v1/tasks/{id}/uncomplete` | Yes | No | Revert task to active |
| GET | `/api/v1/tasks/today` | Yes | No | Get Today View data |
| PATCH | `/api/v1/tasks/{id}/today` | Yes | No | Toggle Today flag |
| GET | `/api/v1/lists` | Yes | No | Get all user lists |
| GET | `/api/v1/lists/counts` | Yes | No | Get list task counts |
| POST | `/api/v1/lists` | Yes | No | Create list |
| PUT | `/api/v1/lists/{id}` | Yes | No | Update list |
| DELETE | `/api/v1/lists/{id}` | Yes | No | Delete list |
| GET | `/actuator/health` | No | No | Health check |

**Total endpoints:** 21

---

*Generated by SDLC Factory -- Phase 4: Design (LLD - API Contracts)*
*See also: `docs/architecture/lld/class-design.md`, `docs/architecture/lld/sequence-diagrams.md`*
