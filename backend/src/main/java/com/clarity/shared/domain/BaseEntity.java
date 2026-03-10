package com.clarity.shared.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base class for domain models (not JPA entities).
 * Holds common fields that all aggregates share.
 */
public abstract class BaseEntity {

    protected UUID id;
    protected OffsetDateTime createdAt;
    protected OffsetDateTime updatedAt;
    protected int version;

    public UUID getId() {
        return id;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public int getVersion() {
        return version;
    }
}
