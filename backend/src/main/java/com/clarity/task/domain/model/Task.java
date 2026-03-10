package com.clarity.task.domain.model;

import com.clarity.task.domain.exception.TaskTitleRequiredException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Task aggregate root.
 * Encapsulates all task lifecycle business rules.
 */
public class Task {

    private UUID id;
    private String title;
    private String description;
    private Priority priority;
    private DueDate dueDate;
    private ReminderInterval reminderInterval;
    private boolean completed;
    private boolean deleted;
    private boolean today;
    private OffsetDateTime completedAt;
    private int sortOrder;
    private UUID listId;
    private UUID userId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int version;

    // For persistence layer reconstruction
    Task() {}

    private Task(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.priority = builder.priority;
        this.dueDate = builder.dueDate;
        this.reminderInterval = builder.reminderInterval;
        this.completed = builder.completed;
        this.deleted = builder.deleted;
        this.today = builder.today;
        this.completedAt = builder.completedAt;
        this.sortOrder = builder.sortOrder;
        this.listId = builder.listId;
        this.userId = builder.userId;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.version = builder.version;
    }

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static Task create(String title, UUID listId, UUID userId) {
        if (title == null || title.isBlank()) {
            throw new TaskTitleRequiredException("Task title cannot be blank");
        }
        OffsetDateTime now = OffsetDateTime.now();
        return new Builder()
                .id(UUID.randomUUID())
                .title(title.trim())
                .priority(Priority.P4)
                .reminderInterval(ReminderInterval.NONE)
                .completed(false)
                .deleted(false)
                .today(false)
                .sortOrder(0)
                .listId(Objects.requireNonNull(listId, "listId cannot be null"))
                .userId(Objects.requireNonNull(userId, "userId cannot be null"))
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .build();
    }

    // ─── Business Methods ─────────────────────────────────────────────────────

    public void complete() {
        if (this.completed) return;
        this.completed = true;
        this.completedAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
    }

    public void uncomplete() {
        this.completed = false;
        this.completedAt = null;
        this.updatedAt = OffsetDateTime.now();
    }

    public void softDelete() {
        this.deleted = true;
        this.updatedAt = OffsetDateTime.now();
    }

    public void toggleToday() {
        this.today = !this.today;
        this.updatedAt = OffsetDateTime.now();
    }

    public void updateDetails(String title, String description, Priority priority,
                              DueDate dueDate, ReminderInterval reminderInterval) {
        if (title == null || title.isBlank()) {
            throw new TaskTitleRequiredException("Task title cannot be blank");
        }
        this.title = title.trim();
        this.description = description;
        this.priority = priority != null ? priority : Priority.P4;
        this.dueDate = dueDate;
        this.reminderInterval = reminderInterval != null ? reminderInterval : ReminderInterval.NONE;
        this.updatedAt = OffsetDateTime.now();
    }

    public void moveToList(UUID newListId) {
        this.listId = Objects.requireNonNull(newListId, "listId cannot be null");
        this.updatedAt = OffsetDateTime.now();
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public DueDate getDueDate() { return dueDate; }
    public ReminderInterval getReminderInterval() { return reminderInterval; }
    public boolean isCompleted() { return completed; }
    public boolean isDeleted() { return deleted; }
    public boolean isToday() { return today; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
    public int getSortOrder() { return sortOrder; }
    public UUID getListId() { return listId; }
    public UUID getUserId() { return userId; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public int getVersion() { return version; }

    // ─── Builder ──────────────────────────────────────────────────────────────

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private UUID id;
        private String title;
        private String description;
        private Priority priority = Priority.P4;
        private DueDate dueDate;
        private ReminderInterval reminderInterval = ReminderInterval.NONE;
        private boolean completed;
        private boolean deleted;
        private boolean today;
        private OffsetDateTime completedAt;
        private int sortOrder;
        private UUID listId;
        private UUID userId;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private int version;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder priority(Priority priority) { this.priority = priority; return this; }
        public Builder dueDate(DueDate dueDate) { this.dueDate = dueDate; return this; }
        public Builder reminderInterval(ReminderInterval r) { this.reminderInterval = r; return this; }
        public Builder completed(boolean completed) { this.completed = completed; return this; }
        public Builder deleted(boolean deleted) { this.deleted = deleted; return this; }
        public Builder today(boolean today) { this.today = today; return this; }
        public Builder completedAt(OffsetDateTime completedAt) { this.completedAt = completedAt; return this; }
        public Builder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder listId(UUID listId) { this.listId = listId; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder version(int version) { this.version = version; return this; }

        public Task build() {
            return new Task(this);
        }
    }
}
