package com.clarity.task.domain.event;

import java.util.UUID;

public class TaskCreatedEvent extends DomainEvent {

    private final UUID taskId;
    private final String title;
    private final UUID listId;
    private final UUID userId;

    public TaskCreatedEvent(UUID taskId, String title, UUID listId, UUID userId) {
        super();
        this.taskId = taskId;
        this.title = title;
        this.listId = listId;
        this.userId = userId;
    }

    @Override
    public String getEventType() {
        return "task.created";
    }

    public UUID getTaskId() { return taskId; }
    public String getTitle() { return title; }
    public UUID getListId() { return listId; }
    public UUID getUserId() { return userId; }
}
