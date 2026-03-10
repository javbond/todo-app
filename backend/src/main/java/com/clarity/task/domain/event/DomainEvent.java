package com.clarity.task.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public abstract class DomainEvent {

    private final UUID eventId;
    private final OffsetDateTime occurredAt;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.occurredAt = OffsetDateTime.now();
    }

    public UUID getEventId() {
        return eventId;
    }

    public OffsetDateTime getOccurredAt() {
        return occurredAt;
    }

    public abstract String getEventType();
}
