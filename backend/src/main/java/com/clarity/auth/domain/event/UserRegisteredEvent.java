package com.clarity.auth.domain.event;

import java.time.OffsetDateTime;
import java.util.UUID;

public class UserRegisteredEvent {

    private final UUID eventId;
    private final UUID userId;
    private final String email;
    private final String displayName;
    private final OffsetDateTime occurredAt;

    public UserRegisteredEvent(UUID userId, String email, String displayName) {
        this.eventId = UUID.randomUUID();
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
        this.occurredAt = OffsetDateTime.now();
    }

    public UUID getEventId() { return eventId; }
    public UUID getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
}
