package com.clarity.task.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public class TaskCompletedEvent extends DomainEvent {

    private final UUID taskId;
    private final UUID userId;
    private final OffsetDateTime completedAt;

    public TaskCompletedEvent(UUID taskId, UUID userId, OffsetDateTime completedAt) {
        super();
        this.taskId = taskId;
        this.userId = userId;
        this.completedAt = completedAt;
    }

    @Override
    public String getEventType() {
        return "task.completed";
    }

    public UUID getTaskId() { return taskId; }
    public UUID getUserId() { return userId; }
    public OffsetDateTime getCompletedAt() { return completedAt; }
}
