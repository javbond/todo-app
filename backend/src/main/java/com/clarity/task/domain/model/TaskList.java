package com.clarity.task.domain.model;

import com.clarity.task.domain.exception.InboxUndeletableException;

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * TaskList aggregate root.
 * Represents a named collection of tasks owned by a user.
 */
public class TaskList {

    private UUID id;
    private String name;
    private ListColor color;
    private int sortOrder;
    private boolean defaultList;
    private UUID userId;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private int version;

    // For persistence layer reconstruction
    TaskList() {}

    private TaskList(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.color = builder.color;
        this.sortOrder = builder.sortOrder;
        this.defaultList = builder.defaultList;
        this.userId = builder.userId;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
        this.version = builder.version;
    }

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static TaskList create(String name, ListColor color, UUID userId) {
        validateName(name);
        OffsetDateTime now = OffsetDateTime.now();
        return new Builder()
                .id(UUID.randomUUID())
                .name(name.trim())
                .color(color != null ? color : ListColor.GRAY)
                .sortOrder(0)
                .defaultList(false)
                .userId(Objects.requireNonNull(userId, "userId cannot be null"))
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .build();
    }

    public static TaskList createDefault(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        return new Builder()
                .id(UUID.randomUUID())
                .name("Inbox")
                .color(ListColor.GRAY)
                .sortOrder(0)
                .defaultList(true)
                .userId(Objects.requireNonNull(userId, "userId cannot be null"))
                .createdAt(now)
                .updatedAt(now)
                .version(0)
                .build();
    }

    // ─── Business Methods ─────────────────────────────────────────────────────

    public void rename(String newName) {
        validateName(newName);
        this.name = newName.trim();
        this.updatedAt = OffsetDateTime.now();
    }

    public void changeColor(ListColor newColor) {
        this.color = Objects.requireNonNull(newColor, "color cannot be null");
        this.updatedAt = OffsetDateTime.now();
    }

    public void ensureDeletable() {
        if (this.defaultList) {
            throw new InboxUndeletableException("The default Inbox list cannot be deleted");
        }
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("List name cannot be blank");
        }
        if (name.trim().length() > 50) {
            throw new IllegalArgumentException("List name cannot exceed 50 characters");
        }
    }

    // ─── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getName() { return name; }
    public ListColor getColor() { return color; }
    public int getSortOrder() { return sortOrder; }
    public boolean isDefaultList() { return defaultList; }
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
        private String name;
        private ListColor color = ListColor.GRAY;
        private int sortOrder;
        private boolean defaultList;
        private UUID userId;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;
        private int version;

        public Builder id(UUID id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder color(ListColor color) { this.color = color; return this; }
        public Builder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder defaultList(boolean defaultList) { this.defaultList = defaultList; return this; }
        public Builder userId(UUID userId) { this.userId = userId; return this; }
        public Builder createdAt(OffsetDateTime createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; return this; }
        public Builder version(int version) { this.version = version; return this; }

        public TaskList build() {
            return new TaskList(this);
        }
    }
}
