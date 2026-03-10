package com.clarity.task.domain.event;

import java.util.UUID;

public class TaskDeletedEvent extends DomainEvent {

    private final UUID taskId;
    private final UUID userId;

    public TaskDeletedEvent(UUID taskId, UUID userId) {
        super();
        this.taskId = taskId;
        this.userId = userId;
    }

    @Override
    public String getEventType() {
        return "task.deleted";
    }

    public UUID getTaskId() { return taskId; }
    public UUID getUserId() { return userId; }
}
