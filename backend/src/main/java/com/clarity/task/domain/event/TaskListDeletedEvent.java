package com.clarity.task.domain.event;

import java.util.UUID;

public class TaskListDeletedEvent extends DomainEvent {

    private final UUID listId;
    private final UUID userId;

    public TaskListDeletedEvent(UUID listId, UUID userId) {
        super();
        this.listId = listId;
        this.userId = userId;
    }

    @Override
    public String getEventType() {
        return "tasklist.deleted";
    }

    public UUID getListId() { return listId; }
    public UUID getUserId() { return userId; }
}
